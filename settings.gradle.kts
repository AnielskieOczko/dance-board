rootProject.name = "DanceBoard"

include(":shared")

include(":backend")

include(":frontend")

dependencyResolutionManagement { repositories { mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }
}
