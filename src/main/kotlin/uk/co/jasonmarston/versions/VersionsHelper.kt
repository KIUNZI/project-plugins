package uk.co.jasonmarston.versions

import org.gradle.api.Project
import uk.co.jasonmarston.bootstrap.withSettingsPluginBootstrapCheck
import uk.co.jasonmarston.build.utility.configString

fun Project.registerKiunziVersionsIfAbsent() {
    withSettingsPluginBootstrapCheck {
        if (extensions.findByName("kiunziVersions") != null) return@withSettingsPluginBootstrapCheck

        val javaVersion =
            providers.configString("version.java", true)
                .orElse("21")
                .get()
                .toInt()

        val kiunziVersions = KiunziVersions(
            javaVersion = javaVersion,
            javaSourceVersion =
                providers.configString("version.java.source",true)
                    .getOrNull()
                    ?.toInt()
                    ?: javaVersion,

            javaByteCodeVersion =
                providers.configString("version.java.bytecode", true)
                    .getOrNull()
                    ?.toInt()
                    ?: javaVersion,

            junitVersion =
                providers.configString("version.junit", true)
                    .orElse("6.0.2")
                    .get(),

            modelMapperVersion =
                providers.configString("version.modelmapper", true)
                    .orElse("3.2.6")
                    .get(),

            modelMapperModuleRecordVersion =
                providers.configString("version.modelmapper.modulerecord", true)
                    .orElse("1.0.1")
                    .get(),

            quarkusPlatformVersion =
                providers.configString("version.quarkus.platform", true)
                    .orElse("3.32.3")
                    .get()
        )

        extensions.add(
            KiunziVersions::class.java,
            "kiunziVersions",
            kiunziVersions
        )
    }
}