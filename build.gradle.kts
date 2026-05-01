@file:Suppress("SpellCheckingInspection")

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  id("org.jetbrains.dokka") version "1.9.20" // Dokka for KDoc HTML
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion")
  .orElse("1.0.0-SNAPSHOT")
  .get()

dependencies {
  implementation("org.gradlex.extra-java-module-info:org.gradlex.extra-java-module-info.gradle.plugin:1.14")
  implementation("io.quarkus:io.quarkus.gradle.plugin:3.31.3")
  implementation("org.kordamp.gradle.jandex:org.kordamp.gradle.jandex.gradle.plugin:1.0.0")
  compileOnly("uk.co.jasonmarston.kiunzi:conventions-support:1.0.0")

  // Kotlin unit testing dependencies
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")

  // Dokka for KDoc HTML documentation
  dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.20")
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
  plugins {
    create("Base") {
      id = "uk.co.jasonmarston.project.standards.base"
      implementationClass = "uk.co.jasonmarston.base.ArtifactsProjectPlugin"
    }
    create("JavaLibrary") {
      id = "uk.co.jasonmarston.project.standards.java-library"
      implementationClass = "uk.co.jasonmarston.javalibrary.JavaLibraryProjectPlugin"
    }
    create("QuarkusLibrary") {
      id = "uk.co.jasonmarston.project.standards.quarkus-library"
      implementationClass = "uk.co.jasonmarston.quarkuslibrary.QuarkusLibraryProjectPlugin"
    }
    create("QuarkusApp") {
      id = "uk.co.jasonmarston.project.standards.quarkus-app"
      implementationClass = "uk.co.jasonmarston.quarkusapp.QuarkusAppProjectPlugin"
    }
    create("Liquibase") {
      id = "uk.co.jasonmarston.project.standards.liquibase"
      implementationClass = "uk.co.jasonmarston.liquibase.LiquibaseProjectPlugin"
    }
    create("Version") {
      id = "uk.co.jasonmarston.project.standards.version"
      implementationClass = "uk.co.jasonmarston.version.VersionProjectPlugin"
    }
    create("GitOps") {
      id = "uk.co.jasonmarston.project.standards.gitops"
      implementationClass = "uk.co.jasonmarston.gitops.GitOpsPromoteProjectPlugin"
    }
  }
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

kotlin {
  jvmToolchain(17)
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(17)
}

// Dokka HTML JAR for publishing documentation
tasks.register<Jar>("dokkaHtmlJar") {
    group = "documentation"
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

publishing {
    publications {
        // java-gradle-plugin already creates pluginMaven; add docs there to avoid duplicate coordinates.
        withType<MavenPublication>().matching { it.name == "pluginMaven" }.configureEach {
            artifact(tasks.named("dokkaHtmlJar"))
        }
    }
}

// Workaround: Dokka does not support Gradle configuration cache yet
// See: https://github.com/Kotlin/dokka/issues/1217
if (project.tasks.findByName("dokkaHtml") != null) {
    tasks.named("dokkaHtml", org.jetbrains.dokka.gradle.DokkaTask::class) {
        @Suppress("DEPRECATION")
        outputDirectory.set(buildDir.resolve("dokka"))
        notCompatibleWithConfigurationCache("Dokka does not support configuration cache yet")
    }
}
