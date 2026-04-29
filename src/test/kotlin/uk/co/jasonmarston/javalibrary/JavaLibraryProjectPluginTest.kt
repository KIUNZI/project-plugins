package uk.co.jasonmarston.javalibrary

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test as KTest
import kotlin.test.fail

class JavaLibraryProjectPluginTest {
    @KTest
    fun `throws expected error if settings plugin support is missing`() {
        val project = ProjectBuilder.builder().build()
        try {
            project.pluginManager.apply("uk.co.jasonmarston.project.standards.java-library")
            fail("Expected exception was not thrown")
        } catch (ex: Throwable) {
            var found = false
            var current: Throwable? = ex
            while (current != null) {
                val msg = current.message ?: ""
                if (msg.contains("requires the settings plugin") || msg.contains("ConfigStringKt")) {
                    found = true
                    break
                }
                current = current.cause
            }
            assert(found) { "Expected error message not found in exception chain" }
        }
    }
}



