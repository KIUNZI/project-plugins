package uk.co.jasonmarston.liquibase

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.create
import uk.co.jasonmarston.bootstrap.withSettingsPluginBootstrapCheck
import uk.co.jasonmarston.build.utility.configString

@Suppress("unused")
class LiquibaseProjectPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        withSettingsPluginBootstrapCheck {
            val ext = extensions.create<LiquibaseConfigMapExtension>("liquibaseConfigMap").apply {
                changelogDir.convention(
                    project.providers
                        .configString("liquibase.configmap.changelogDir", true)
                        .orElse("src/main/resources/db/changelog")
                        .map { path: String ->
                            project.layout.projectDirectory.dir(path)
                        }
                )

                configMapName.convention(
                    project.providers
                        .configString("liquibase.configMap.name", true)
                        .orElse("liquibase-changesets")
                )

                warnKb.convention(
                    project.providers
                        .configString("liquibase.configmap.warnKb", true)
                        .map { it.toLongOrNull() ?: 700L }
                        .orElse(700L)
                )

                failKb.convention(
                    project.providers
                        .configString("liquibase.configmap.failKb", true)
                        .map { it.toLongOrNull() ?: 900L }
                        .orElse(900L)
                )

                kubectlContext.convention(
                    project.providers.configString("kubectl.context", true)
                )

                kubectlNamespace.convention(
                    project.providers.configString("quarkus.kubernetes.namespace", true)
                )
            }

            val output = layout
                .buildDirectory
                .file("kubernetes/liquibase-changesets/configMap.yaml")

            val generate = tasks.register<GenerateLiquibaseConfigMap>("generateLiquibaseConfigMap") {
                changelogDir.set(ext.changelogDir)
                configMapName.set(ext.configMapName)
                namespace.set(ext.kubectlNamespace)
                warnKb.set(ext.warnKb)
                failKb.set(ext.failKb)
                outputFile.set(output)
            }

            val apply = tasks.register<ApplyLiquibaseConfigMap>("applyLiquibaseConfigMap") {
                dependsOn(generate)
                configMapYaml.set(output)
                kubectlContext.set(ext.kubectlContext)
            }

            tasks.matching { it.name == "quarkusBuild" }
                .configureEach { dependsOn(generate) }

            tasks.matching { it.name == "quarkusDeploy" }
                .configureEach { dependsOn(apply) }
        }
    }
}