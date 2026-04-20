package uk.co.jasonmarston.gitops

import org.gradle.api.*
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.io.File

@Suppress("unused")
class GitOpsPromoteProjectPlugin : Plugin<Project> {
    private enum class Env(val display: String) { DEV("Dev"), PREPROD("PreProd"), PROD("Prod") }
    private enum class VersionKind { SNAPSHOT, RC, FINAL }
    private val rcRegex = Regex(""".*-rc\.\d+$""")

    override fun apply(project: Project) {
        val ext = project.extensions.create("gitOpsPromote", GitOpsPromoteExtension::class.java).apply {
            appPath.convention("apps/my-service")
            devValuesPath.convention("apps/my-service/env/Dev/values.yaml")
            preProdValuesPath.convention("apps/my-service/env/PreProd/values.yaml")
            prodValuesPath.convention("apps/my-service/env/Prod/values.yaml")
            imageTagKey.convention("tag")
            imageDigestKey.convention("digest")
            snapshotDigestFile.convention("build/jib-image.digest")
            gitPush.convention(true)
        }

        fun execGit(repo: File, args: Array<out String>, ignoreExit: Boolean) =
            project.providers.exec {
                workingDir = repo
                commandLine(listOf("git") + args)
                isIgnoreExitValue = ignoreExit
            }

        fun git(repo: File, vararg args: String, ignoreExit: Boolean = false): Int =
            execGit(repo, args, ignoreExit).result.get().exitValue

        fun gitOut(repo: File, vararg args: String, ignoreExit: Boolean = false): Pair<Int, String> {
            val execOut = execGit(repo, args, ignoreExit)
            val code = execOut.result.get().exitValue
            val out = listOfNotNull(
                execOut.standardOutput.asText.orNull,
                execOut.standardError.asText.orNull
            ).joinToString("\n").trim()
            return code to out
        }

        fun commitAndPush(repo: File, message: String) {
            git(repo, "add", ".")

            val (c, cout) = gitOut(repo, "commit", "-m", message, ignoreExit = true)
            val committed = (c == 0)
            if (!committed && !cout.contains("nothing to commit", ignoreCase = true)) {
                throw GradleException("git commit failed:\n$cout")
            }

            if (ext.gitPush.get()) {
                val (p, pout) = gitOut(repo, "push", ignoreExit = true)
                if (p != 0) throw GradleException("git push failed:\n$pout")
            }
        }

        fun classify(version: String): VersionKind = when {
            version.endsWith("-SNAPSHOT") -> VersionKind.SNAPSHOT
            rcRegex.matches(version) -> VersionKind.RC
            else -> VersionKind.FINAL
        }

        fun allowed(env: Env, kind: VersionKind) = when (env) {
            Env.DEV -> true
            Env.PREPROD -> kind != VersionKind.SNAPSHOT
            Env.PROD -> kind == VersionKind.FINAL
        }

        fun upsertYamlScalarUnderBlock(
            valuesFile: File,
            blockKey: String,
            key: String,
            value: String,
            afterKey: String? = null
        ) {
            require(valuesFile.exists()) { "Missing values file: ${valuesFile.absolutePath}" }
            val lines = valuesFile.readLines().toMutableList()

            fun indentOf(line: String) = line.takeWhile { it.isWhitespace() }.length

            val blockIdx = lines.indexOfFirst { it.trimStart() == "$blockKey:" }
            require(blockIdx != -1) { "No '$blockKey:' block found in ${valuesFile.absolutePath}" }

            val blockIndent = indentOf(lines[blockIdx])
            val childIndent = (blockIdx + 1 until lines.size).firstOrNull { i ->
                val t = lines[i].trim()
                t.isNotEmpty() && !t.startsWith("#") && indentOf(lines[i]) > blockIndent
            }?.let { indentOf(lines[it]) } ?: (blockIndent + 2)

            // Find end of this block (next line with indent <= blockIndent, ignoring blanks/comments)
            val endIdxExclusive = (blockIdx + 1 until lines.size).firstOrNull { i ->
                val t = lines[i].trim()
                t.isNotEmpty() && !t.startsWith("#") && indentOf(lines[i]) <= blockIndent
            } ?: lines.size

            fun findKeyLineInBlock(k: String): Int =
                (blockIdx + 1 until endIdxExclusive).firstOrNull { i ->
                    indentOf(lines[i]) == childIndent && lines[i].trimStart().startsWith("$k:")
                } ?: -1

            val idx = findKeyLineInBlock(key)
            if (idx != -1) {
                val indent = lines[idx].takeWhile { it.isWhitespace() }
                lines[idx] = "${indent}$key: $value"
                valuesFile.writeText(lines.joinToString("\n") + "\n")
                return
            }

            val insertAt = afterKey?.let { ak ->
                val aIdx = findKeyLineInBlock(ak)
                if (aIdx != -1) aIdx + 1 else endIdxExclusive
            } ?: endIdxExclusive

            val indent = " ".repeat(childIndent)
            lines.add(insertAt, "${indent}$key: $value")
            valuesFile.writeText(lines.joinToString("\n") + "\n")
        }
        fun readSnapshotDigestFromFile(project: Project, digestPath: String): String {
            val f = project.rootProject.file(digestPath)
            require(f.exists()) { "SNAPSHOT digest file not found: ${f.absolutePath}" }

            val raw = f.readText().trim()
            val m = Regex("""sha256:[0-9a-fA-F]{64}""").find(raw)
            require(m != null) { "Digest file did not contain sha256 digest: '${raw}' (${f.absolutePath})" }
            return m.value
        }

        fun registerPromote(env: Env, valuesPath: Provider<String>): TaskProvider<Task> =
            project.tasks.register("promote${env.display}") {
                group = "gitops"
                description = "Update GitOps values for ${env.display} to deploy current version; commit & push."

                doLast {
                    val gitOps = ext.gitOpsRepoDir.get().asFile

                    require(gitOps.exists()) { "gitOpsRepoDir does not exist: ${gitOps.absolutePath}" }

                    val version = project.version.toString()
                    val kind = classify(version)
                    if (!allowed(env, kind)) {
                        throw GradleException("Version $version is not allowed in ${env.display} (policy: snapshot→dev; rc→dev+preprod; final→all).")
                    }

                    val valuesFile = File(gitOps, valuesPath.get())

                    upsertYamlScalarUnderBlock(valuesFile, "image", ext.imageTagKey.get(), version)

                    if (kind == VersionKind.SNAPSHOT) {
                        val digest = readSnapshotDigestFromFile(project, ext.snapshotDigestFile.get())
                        upsertYamlScalarUnderBlock(valuesFile, "image", ext.imageDigestKey.get(), digest, afterKey = ext.imageTagKey.get())
                        commitAndPush(gitOps, "Promote ${ext.appPath.get()} to ${env.display}: $version ($digest)")
                    } else {
                        commitAndPush(gitOps, "Promote ${ext.appPath.get()} to ${env.display}: $version")
                    }
                }
            }

        registerPromote(Env.DEV, ext.devValuesPath)
        registerPromote(Env.PREPROD, ext.preProdValuesPath)
        registerPromote(Env.PROD, ext.prodValuesPath)
    }
}