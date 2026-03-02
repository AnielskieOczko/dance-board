plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

group = "com.danceboard"
version = "0.0.1"

kotlin {
    // Kompiluj na JVM (backend go używa)
    jvm()

    // Kompiluj na JS (frontend go używa)
    js(IR) {
        browser()
    }

    // Wspólne dependencies — dostępne na OBU platformach
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
            }
        }
    }
}
