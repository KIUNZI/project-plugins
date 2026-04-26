pluginManagement {
  repositories {
    maven {
      fun ProviderFactory.requiredEnv(name: String): Provider<String> {
        return environmentVariable(name).orElse(provider { error("$name must be set") })
      }

      val repoUser = providers.requiredEnv("ARTIFACTS_REPO_USER")
      val repoToken = providers.requiredEnv("ARTIFACTS_REPO_TOKEN")

      name = "Artifacts"
      url = uri("https://pkgs.dev.azure.com/jamarston/762ffd9e-ca64-466d-84e9-7a0e42e5d89a/_packaging/BuildArtifacts/maven/v1")
      credentials {
        username = repoUser.get()
        password = repoToken.get()
      }
      authentication {
        create<BasicAuthentication>("basic")
      }
    }
  }
}

plugins {
  id("uk.co.jasonmarston.standards.settings") version "1.0.3"
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name="project-plugins"
