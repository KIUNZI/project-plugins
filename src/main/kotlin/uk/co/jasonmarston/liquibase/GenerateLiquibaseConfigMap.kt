package uk.co.jasonmarston.liquibase

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.nio.charset.StandardCharsets

@Suppress("unused")
@CacheableTask
abstract class GenerateLiquibaseConfigMap : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val changelogDir: DirectoryProperty

    @get:Input
    abstract val configMapName: Property<String>

    @get:Input
    @get:Optional
    abstract val namespace: Property<String>

    /** Warn threshold in KB */
    @get:Input
    abstract val warnKb: Property<Long>

    /** Fail threshold in KB */
    @get:Input
    abstract val failKb: Property<Long>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        group = "liquibase"
        description = "Generates a Kubernetes ConfigMap YAML containing Liquibase changelog files."
    }

    @TaskAction
    fun generate() {
        val root = changelogDir.asFile.get()

        if (!root.exists()) {
            throw GradleException("Liquibase changelog dir not found: ${root.absolutePath}")
        }

        val files = root
            .walkTopDown()
            .filter { it.isFile }
            .toList()
            .sortedBy { f ->
                root
                    .toPath()
                    .relativize(f.toPath())
                    .toString()
                    .replace("\\", "/")
            }

        val cmName = configMapName.get().trim()
        require(cmName.isNotEmpty()) {
            "configMapName must not be blank"
        }

        val ns = namespace.orNull?.takeIf { it.isNotBlank() }

        val yaml = buildString {
            appendLine("apiVersion: v1")
            appendLine("kind: ConfigMap")
            appendLine("metadata:")
            appendLine("  name: $cmName")
            ns?.let {
                appendLine("  namespace: $it")
            }
            appendLine("data:")

            files.forEach { f ->
                val rel = root.toPath()
                    .relativize(f.toPath())
                    .toString()
                    .replace("\\", "/")

                // Quote keys to avoid YAML surprises for special chars/spaces.
                appendLine("  \"${rel}\": |-")
                f.forEachLine { line ->
                    appendLine("    $line")
                }
            }
        }

        val yamlBytes = yaml
            .toByteArray(StandardCharsets.UTF_8)
            .size
            .toLong()
        val warnBytes = warnKb
            .get()
            .coerceAtLeast(0L) * 1024L
        val failBytes = failKb
            .get()
            .coerceAtLeast(0L) * 1024L

        @Suppress("ConvertTwoComparisonsToRangeCheck")
        when {
            failBytes > 0 && yamlBytes >= failBytes ->
                throw GradleException(
                    "ConfigMap too large (rendered YAML ~$yamlBytes bytes, limit $failBytes bytes)."
                )

            warnBytes > 0 && yamlBytes >= warnBytes ->
                logger.warn(
                    "ConfigMap approaching size limit (rendered YAML ~$yamlBytes bytes; warn at $warnBytes)."
                )

            else ->
                logger.lifecycle("ConfigMap size OK (rendered YAML ~$yamlBytes bytes).")
        }

        val out = outputFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(yaml, Charsets.UTF_8)
    }
}