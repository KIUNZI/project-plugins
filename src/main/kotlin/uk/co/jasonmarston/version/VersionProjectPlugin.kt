package uk.co.jasonmarston.version

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

@Suppress("unused")
class VersionProjectPlugin : Plugin<Project> {
    @Suppress("ObjectLiteralToLambda")
    override fun apply(project: Project) {
        if (project != project.rootProject) return
        val computedVersion = project.providers.provider {
            project.computeVersionFromGitTags()
        }
        project.gradle.beforeProject(object : Action<Project> {
            override fun execute(p: Project) {
                val root = project.rootProject
                if (root.version.toString() == "unspecified") {
                    root.version = computedVersion.get()
                }
                p.version = root.version
                p.tasks.withType(Jar::class.java).configureEach {
                    archiveVersion.set(p.provider { p.version.toString() })
                }
            }
        })
    }
    private fun Project.execAndCapture(vararg args: String): Pair<Int, String> {
        val execOut = providers.exec {
            commandLine(args.toList())
            isIgnoreExitValue = true
        }
        val exitCode = execOut.result.get().exitValue
        val stdout = execOut.standardOutput.asText.get().trim()
        return exitCode to stdout
    }
    private fun Project.computeVersionFromGitTags(): String {
        val matchPattern = "v[0-9]*.[0-9]*.[0-9]*"
        val (lastCode, lastOut) = execAndCapture(
            "git", "describe", "--tags", "--match", matchPattern, "--abbrev=0"
        )
        val lastTag = if (lastCode == 0 && lastOut.isNotBlank()) lastOut else null
        if (lastTag == null) return "0.0.0-SNAPSHOT"
        val (exactCode, exactOut) = execAndCapture(
            "git", "describe", "--tags", "--match", matchPattern, "--exact-match"
        )
        val exactTag = if (exactCode == 0 && exactOut.isNotBlank()) exactOut else null
        fun stripV(tag: String) = tag.removePrefix("v")
        return if (exactTag != null) stripV(exactTag)
        else stripV(lastTag).removeSuffix("-SNAPSHOT") + "-SNAPSHOT"
    }
}

