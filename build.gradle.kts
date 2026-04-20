@file:Suppress("SpellCheckingInspection")

import uk.co.jasonmarston.build.utility.configString

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
}

group = "uk.co.jasonmarston"
version = providers.gradleProperty("buildStandardsVersion")
  .orElse("1.0.0-SNAPSHOT")
  .get()

val repoUrl = providers.configString("artifacts.repo.url")
val repoUser = providers.configString("artifacts.repo.user")
val repoToken = providers.configString("artifacts.repo.token")

publishing {
  repositories {
    maven {
      name = "Artifacts"
      url = uri(repoUrl.get())
      credentials {
        username = repoUser.get()
        password = repoToken.get()
      }
      authentication {
        create("basic", BasicAuthentication::class.java)
      }
    }
  }
}

dependencies {
  implementation("org.gradlex.extra-java-module-info:org.gradlex.extra-java-module-info.gradle.plugin:1.14")
  implementation("io.quarkus:io.quarkus.gradle.plugin:3.31.3")
  implementation("org.kordamp.gradle.jandex:org.kordamp.gradle.jandex.gradle.plugin:1.0.0")
  compileOnly("uk.co.jasonmarston.kiunzi:conventions-support:1.0-SNAPSHOT")
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
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(17)
}
