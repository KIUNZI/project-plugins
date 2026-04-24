package uk.co.jasonmarston.base

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.external.javadoc.StandardJavadocDocletOptions

@Suppress("unused")
class ArtifactsProjectPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            tasks.withType(GenerateModuleMetadata::class.java).configureEach {
                enabled = false
            }

            tasks.withType(Test::class.java).configureEach {
                useJUnitPlatform()
            }

            pluginManager.withPlugin("java") {
                tasks.withType(Javadoc::class.java).configureEach {
                    (options as StandardJavadocDocletOptions).memberLevel = JavadocMemberLevel.PUBLIC
                }
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
