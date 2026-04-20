package uk.co.jasonmarston.liquibase

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@Suppress("unused")
@DisableCachingByDefault(because = "Applies resources to a cluster; side-effecting Exec task")
abstract class ApplyLiquibaseConfigMap : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val configMapYaml: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val kubectlContext: Property<String>

    @get:Inject
    abstract val execOps: ExecOperations

    init {
        group = "liquibase"
        description = "Applies the generated ConfigMap YAML using kubectl."
        // Always run, because cluster state is external to Gradle
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun apply() {
        val yamlFile = configMapYaml.asFile.get()
        if (!yamlFile.exists()) {
            throw IllegalStateException("ConfigMap YAML does not exist: ${yamlFile.absolutePath}")
        }

        val args = buildList {
            add("apply")
            kubectlContext.orNull
                ?.takeIf { it.isNotBlank() }
                ?.let { addAll(listOf("--context", it)) }

            addAll(listOf("-f", yamlFile.absolutePath))
        }

        execOps.exec {
            executable = "kubectl"
            args(args)
        }
    }
}