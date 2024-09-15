import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "2.0.0"
  id("org.jetbrains.intellij") version "1.17.3"
}

group = "be.bruyere.romain"
version = "2021.1.0"

repositories {
  mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2021.1") // Lowest target IDE Version
  type.set("IC") // Target IDE Platform

  plugins.set(listOf("java", "com.intellij.platform.images"))
}

dependencies {
  implementation("org.locationtech.jts:jts-core:1.19.0")
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  patchPluginXml {
    sinceBuild.set("211")
    untilBuild.set("213.*")
    changeNotes.set("Changes the dialog window for it to be modal")
  }

  signPlugin {
    certificateChainFile.set(file("certificate/chain.crt"))
    privateKeyFile.set(file("certificate/private.pem"))
    password.set(System.getenv("IDEA_PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("IDEA_PUBLISH_TOKEN"))
  }
}
