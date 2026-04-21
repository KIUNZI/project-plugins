pluginManagement {
  includeBuild("../kiunzi-settings-plugin")
}

includeBuild("../kiunzi-settings-plugin")

plugins {
  id("uk.co.jasonmarston.standards.settings")
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name="kiunzi-project-plugins"
