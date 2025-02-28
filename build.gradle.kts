import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "2.1.0"
  id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()


repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
    jetbrainsRuntime() // EAP
  }
}

val jtsVersion = "1.19.0"
dependencies {
  intellijPlatform {
//    create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
    intellijIdeaCommunity(providers.gradleProperty("platformVersion"), useInstaller = false) // EAP
    plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
    bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
    jetbrainsRuntime() // EAP
  }
  implementation("org.locationtech.jts:jts-core:$jtsVersion")
}

intellijPlatform {
  pluginConfiguration {
    version = providers.gradleProperty("pluginVersion")
    changeNotes = providers.gradleProperty("changeNotes")
    ideaVersion {
      sinceBuild = providers.gradleProperty("pluginSinceBuild")
      untilBuild = providers.gradleProperty("pluginUntilBuild")
    }
  }

  signing {
    certificateChain = providers.environmentVariable("IDEA_CERTIFICATE_CHAIN")
    privateKey = providers.environmentVariable("IDEA_PRIVATE_KEY")
    password = providers.environmentVariable("IDEA_PRIVATE_KEY_PASSWORD")
  }

  publishing {
    token = providers.environmentVariable("IDEA_PUBLISH_TOKEN")
    channels = providers.gradleProperty("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
  }
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_21)
    }
  }
}
