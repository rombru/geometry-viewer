import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "2.0.0"
  id("org.jetbrains.intellij") version "1.17.3"
}

group = "be.bruyere.romain"
version = "2024.0.0"

repositories {
  mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2024.1") // Lowest target IDE Version
  type.set("IC") // Target IDE Platform

  plugins.set(listOf("java", "com.intellij.platform.images"))
}

dependencies {
  implementation("org.locationtech.jts:jts-core:1.19.0")
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  patchPluginXml {
    sinceBuild.set("241")
    untilBuild.set("")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}
