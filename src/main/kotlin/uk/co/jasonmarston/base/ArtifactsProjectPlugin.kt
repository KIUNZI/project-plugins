package uk.co.jasonmarston.base

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.authentication.http.BasicAuthentication
import uk.co.jasonmarston.build.utility.configString

@Suppress("unused")
class ArtifactsProjectPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            val repoUrl = providers.configString("artifacts.repo.url")
            val repoUser = providers.configString("artifacts.repo.user")
            val repoToken = providers.configString("artifacts.repo.token")

            pluginManager.withPlugin("maven-publish") {
                extensions.configure(PublishingExtension::class.java) {
                    repositories {
                        maven {
                            name = "Artifacts"
                            url = uri(repoUrl.get())
                            credentials {
                                username = repoUser.get()
                                password = repoToken.get()
                            }
                            authentication {
                                create("basic", BasicAuthentication::class.java)
                            }
                        }
                    }
                }
            }

            tasks.withType(GenerateModuleMetadata::class.java).configureEach {
                enabled = false
            }

            tasks.withType(Test::class.java).configureEach {
                useJUnitPlatform()
            }

            tasks.withType(JavaCompile::class.java).configureEach {
                val moduleInfo = layout.projectDirectory.file("src/main/java/module-info.java").asFile
                if (moduleInfo.exists()) {
                    modularity.inferModulePath.set(true)
                }
            }
        }
    }
}
