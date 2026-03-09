plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    kotlin("plugin.compose")
    id("org.jetbrains.compose") version "1.10.1"
}

group = "com.danceboard"
version = "0.0.1"

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport { enabled.set(true) }
                outputFileName = "DanceBoard-frontend.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain {
            dependencies {
                // Shared module (DTOs, enums)
                implementation(project(":shared"))

                // Compose HTML
                implementation("org.jetbrains.compose.html:html-core:1.10.1")
                implementation("org.jetbrains.compose.runtime:runtime:1.10.1")

                // Ktor Client (HTTP requests to backend)
                implementation("io.ktor:ktor-client-js:3.4.0")
                implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
            }
        }
    }
}
