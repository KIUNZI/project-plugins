package uk.co.jasonmarston.base

// ...existing code...
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test as KTest
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import java.io.File

class ArtifactsProjectPluginTest {
    @KTest
    fun `disables GenerateModuleMetadata tasks`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("uk.co.jasonmarston.project.standards.base")
        val tasks = project.tasks.filter { it.name.contains("generateModuleMetadata", ignoreCase = true) }
        tasks.forEach { assertTrue(!it.enabled) }
    }

    @KTest
    fun `all Test tasks use JUnit Platform`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("uk.co.jasonmarston.project.standards.base")
        project.tasks.withType(Test::class.java).forEach {
            assertTrue(it.options is org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions || it.extensions.findByName("useJUnitPlatform") != null)
        }
    }

    @KTest
    fun `Javadoc member level is set to PUBLIC when Java plugin is applied`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("java")
        project.pluginManager.apply("uk.co.jasonmarston.project.standards.base")
        project.tasks.withType(Javadoc::class.java).forEach {
            val options = it.options as StandardJavadocDocletOptions
            assertEquals(JavadocMemberLevel.PUBLIC, options.memberLevel)
        }
    }

    @KTest
    fun module_path_inference_enabled_if_module_info_exists() {
        val projectDir = kotlin.io.path.createTempDirectory().toFile()
        val srcDir = File(projectDir, "src/main/java").apply { mkdirs() }
        File(srcDir, "module-info.java").writeText("module test {}\n")
        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        project.pluginManager.apply("uk.co.jasonmarston.project.standards.base")
        project.tasks.withType(org.gradle.api.tasks.compile.JavaCompile::class.java).forEach {
            assertTrue(it.modularity.inferModulePath.get())
        }
        projectDir.deleteRecursively()
    }
}


