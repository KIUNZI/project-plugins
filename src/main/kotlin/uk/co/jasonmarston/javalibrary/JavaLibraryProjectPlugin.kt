@file:Suppress("unused")

package uk.co.jasonmarston.javalibrary

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradlex.javamodule.moduleinfo.ExtraJavaModuleInfoPluginExtension
import uk.co.jasonmarston.versions.KiunziVersions
import uk.co.jasonmarston.versions.registerKiunziVersionsIfAbsent

class JavaLibraryProjectPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            project.registerKiunziVersionsIfAbsent()
            val versions = extensions.getByName("kiunziVersions") as KiunziVersions

            pluginManager.apply("java-library")
            pluginManager.apply("org.gradlex.extra-java-module-info")
            pluginManager.apply("uk.co.jasonmarston.project.standards.base")

            dependencies.apply {
                add(
                    "testImplementation",
                    platform("org.junit:junit-bom:${versions.junitVersion}")
                )
                add(
                    "testImplementation",
                    "org.junit.jupiter:junit-jupiter"
                )
                add(
                    "testRuntimeOnly",
                    "org.junit.platform:junit-platform-launcher"
                )
            }

            extensions.configure(ExtraJavaModuleInfoPluginExtension::class.java) {
                deriveAutomaticModuleNamesFromFileNames.set(true)
                automaticModule(
                    "modelmapper-${versions.modelMapperVersion}.jar",
                    "org.modelmapper"
                )
                automaticModule(
                    "modelmapper-module-record-${versions.modelMapperModuleRecordVersion}.jar",
                    "org.modelmapper.module.record"
                )
            }

            extensions.configure(JavaPluginExtension::class.java) {
                toolchain {
                    languageVersion
                        .set(JavaLanguageVersion.of(versions.javaSourceVersion))
                }
            }

            tasks.withType(JavaCompile::class.java).configureEach {
                options.release.set(versions.javaByteCodeVersion)
            }

            pluginManager.withPlugin("maven-publish") {
                extensions.configure(PublishingExtension::class.java) {
                    publications {
                        create("mavenJava", MavenPublication::class.java) {
                            val javaComponent = components.findByName("java")
                            if (javaComponent != null) {
                                from(javaComponent)
                            }
                            artifactId = project.name
                        }
                    }
                }
            }
        }
    }
}