pluginManagement {
  includeBuild("../settings-plugin")
}

includeBuild("../settings-plugin")

plugins {
  id("uk.co.jasonmarston.standards.settings")
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name="project-plugins"
