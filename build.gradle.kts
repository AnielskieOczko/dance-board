plugins {
    kotlin("jvm") version "2.3.10" apply false
    kotlin("multiplatform") version "2.3.10" apply false
    kotlin("plugin.serialization") version "2.3.10" apply false
    kotlin("plugin.compose") version "2.3.10" apply false
    id("io.ktor.plugin") version "3.4.0" apply false
    id("org.jetbrains.compose") version "1.10.1" apply false
    id("org.sonarqube") version "5.1.0.4882"
}

sonar {
    properties {
        property("sonar.projectKey", "dance-board")
        property("sonar.organization", "anielskieoczko")
        property("sonar.host.url", "https://sonarcloud.io")
        // Optional: help Sonar find binaries if it struggles with KMP layout
        property("sonar.kotlin.binaries", "**/build/classes/kotlin/**")
    }
}
