package uk.co.jasonmarston.quarkuslibrary

import org.gradle.api.Plugin
import org.gradle.api.Project
import uk.co.jasonmarston.versions.KiunziVersions
import uk.co.jasonmarston.versions.registerKiunziVersionsIfAbsent

@Suppress("unused")
class QuarkusLibraryProjectPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            registerKiunziVersionsIfAbsent()
            val versions = extensions.getByName("kiunziVersions") as KiunziVersions

            pluginManager.apply("org.kordamp.gradle.jandex")
            pluginManager.apply("uk.co.jasonmarston.project.standards.java-library")

            pluginManager.withPlugin("org.kordamp.gradle.jandex") {
                tasks.matching { it.name == "javadoc" }.configureEach {
                    dependsOn("jandex")
                }
            }

            dependencies.apply {
                constraints.apply {
                    add("implementation",
                        "org.modelmapper:modelmapper:${versions.modelMapperVersion}"
                    )
                    add(
                        "implementation",
                        "org.modelmapper:modelmapper-module-record:${versions.modelMapperModuleRecordVersion}"
                    )
                    add(
                        "testImplementation",
                        "org.modelmapper:modelmapper:${versions.modelMapperVersion}"
                    )
                    add(
                        "testImplementation",
                        "org.modelmapper:modelmapper-module-record:${versions.modelMapperModuleRecordVersion}"
                    )
                }

                add(
                    "implementation",
                    enforcedPlatform("io.quarkus.platform:quarkus-bom:${versions.quarkusPlatformVersion}")
                )
            }
        }
    }
}