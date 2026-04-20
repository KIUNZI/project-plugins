package uk.co.jasonmarston.quarkusapp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import uk.co.jasonmarston.versions.KiunziVersions
import uk.co.jasonmarston.versions.registerKiunziVersionsIfAbsent

@Suppress("unused")
class QuarkusAppProjectPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            registerKiunziVersionsIfAbsent()
            val versions = extensions.getByName("kiunziVersions") as KiunziVersions

            pluginManager.apply("io.quarkus")
            pluginManager.apply("uk.co.jasonmarston.project.standards.base")

            dependencies.apply {
                add(
                    "implementation",
                    enforcedPlatform("io.quarkus.platform:quarkus-bom:${versions.quarkusPlatformVersion}")
                )

                add("testImplementation", "io.quarkus:quarkus-junit5")
                add("testImplementation", "io.rest-assured:rest-assured")
            }

            val quarkusAppZipProvider = tasks.register("quarkusAppZip", Zip::class.java) {
                dependsOn(tasks.named("quarkusBuild"))
                from(layout.buildDirectory.dir("quarkus-app"))
                archiveClassifier.set("quarkus-app")
            }

            val uberJarProvider = providers.provider {
                val candidates = layout.buildDirectory.asFileTree.matching {
                    include("*-runner.jar")
                }.files

                if (candidates.size == 1) candidates.first() else null
            }

            extensions.configure(JavaPluginExtension::class.java) {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(versions.javaSourceVersion))
                }
            }

            tasks.withType(JavaCompile::class.java).configureEach {
                options.release.set(versions.javaByteCodeVersion)
            }

            // Note: quarkusAppZipProvider and uberJarProvider are defined as in the script.
            // If you need them exported/consumed elsewhere, you'd typically wire them into
            // configurations/artifacts or expose via an extension.
            @Suppress("UNUSED_VARIABLE", "LocalVariableName")
            val _keep = Pair(quarkusAppZipProvider, uberJarProvider)
        }
    }
}