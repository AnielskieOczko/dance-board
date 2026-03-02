plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    kotlin("plugin.compose")
    id("org.jetbrains.compose") version "1.7.3"
}

group = "com.danceboard"
version = "0.0.1"

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport { enabled.set(true) }
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain {
            dependencies {
                // Shared module (DTOs, enumy)
                implementation(project(":shared"))

                // Compose HTML
                implementation(compose.html.core)
                implementation(compose.runtime)

                // Ktor Client (HTTP requests do backendu)
                implementation("io.ktor:ktor-client-js:3.4.0")
                implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
            }
        }
    }
}
