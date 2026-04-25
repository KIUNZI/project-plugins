package uk.co.jasonmarston.bootstrap

import org.gradle.api.GradleException
import org.gradle.api.Project

internal inline fun <T> Project.withSettingsPluginBootstrapCheck(action: Project.() -> T): T {
    try {
        return action()
    } catch (exception: NoClassDefFoundError) {
        if (exception.isMissingConfigStringSupport()) {
            throw GradleException(
                "Project '$path' requires the settings plugin `uk.co.jasonmarston.standards.settings` to be applied in `settings.gradle.kts` before applying Kiunzi project convention plugins. That settings plugin provides the `configString(...)` runtime support expected by this plugin.",
                exception
            )
        }
        throw exception
    }
}

private fun NoClassDefFoundError.isMissingConfigStringSupport(): Boolean {
    return message?.contains("uk/co/jasonmarston/build/utility/ConfigStringKt") == true
}

