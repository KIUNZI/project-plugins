package uk.co.jasonmarston.bootstrap

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test as KTest
import kotlin.test.assertFailsWith
import org.gradle.api.GradleException

class SettingsPluginBootstrapGuardTest {
    @KTest
    fun `throws GradleException if ConfigStringKt is missing`() {
        val project = ProjectBuilder.builder().build()
        val error = NoClassDefFoundError("uk/co/jasonmarston/build/utility/ConfigStringKt")
        val ex = assertFailsWith<GradleException> {
            project.withSettingsPluginBootstrapCheck { throw error }
        }
        assert(ex.message!!.contains("requires the settings plugin"))
        assert(ex.cause === error)
    }
}

