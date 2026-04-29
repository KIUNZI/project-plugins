package uk.co.jasonmarston.quarkuslibrary

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test as KTest
import kotlin.test.fail

/**
 * Unit tests for [uk.co.jasonmarston.quarkuslibrary.QuarkusLibraryProjectPlugin].
 * <p>
 * This test verifies that applying the Quarkus library convention plugin without the required settings plugin
 * results in a clear and actionable error, ensuring that consumers are guided to proper project setup.
 * </p>
 *
 * @author Kiunzi Project Contributors
 */
class QuarkusLibraryProjectPluginTest {
    /**
     * Tests that applying the Quarkus library convention plugin without the required settings plugin support
     * throws an exception with a clear error message. The test traverses the exception cause chain to ensure
     * the expected message is present, either indicating the missing settings plugin or missing ConfigStringKt support.
     * <p>
     * This ensures that consumers receive actionable feedback if the project is misconfigured.
     * </p>
     */
    @KTest
    fun `throws expected error if settings plugin support is missing`() {
        val project = ProjectBuilder.builder().build()
        try {
            project.pluginManager.apply("uk.co.jasonmarston.project.standards.quarkus-library")
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
