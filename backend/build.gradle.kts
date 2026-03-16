val exposedVersion: String by project
val postgresVersion: String by project
val hikariVersion: String by project
val logbackVersion: String by project
val kotlinVersion: String by project

// ============================================================
// PLUGINS — tools/capabilities used by this module
// ============================================================
// In multi-module: plugin versions are defined in ROOT build.gradle.kts
// Here we apply plugins WITHOUT versions — root manages them
plugins {
    kotlin("jvm") // Compile Kotlin → JVM bytecode
    id("io.ktor.plugin") // Ktor: fat JAR, Docker, run task
    kotlin("plugin.serialization") // @Serializable annotation processing
}

// ============================================================
// METADATA — module identity (like groupId/artifactId in Maven)
// ============================================================
group = "com.danceboard"

version = "0.0.1"

// ============================================================
// KTOR PLUGIN CONFIG — Ktor-specific configuration
// ============================================================
application {
    // Class containing fun main() — application entry point
    mainClass.set("com.danceboard.backend.ApplicationKt")
    // Note: "ApplicationKt" is the JVM class Kotlin generates from Application.kt
}

// ============================================================
// JVM TOOLCHAIN — which JDK version to use
// ============================================================
kotlin { jvmToolchain(21) }

// ============================================================
// MONOLITH BUILD — Package frontend assets into backend
// ============================================================
tasks.register<Copy>("copyFrontendResources") {
    dependsOn(":frontend:jsBrowserProductionWebpack")
    from(project(":frontend").layout.buildDirectory.dir("dist/js/productionExecutable"))
    into("src/main/resources/static")
}

tasks.named("processResources") {
    dependsOn("copyFrontendResources")
}


// ============================================================
// DEPENDENCIES — libraries (like <dependencies> in pom.xml)
// ============================================================
// Note: Ktor dependencies have NO versions — the io.ktor.plugin
// manages them automatically (like Spring Boot BOM)
dependencies {
    // Shared module (DTOs, enums)
    implementation(project(":shared"))

    // --- Ktor Server Core ---
    implementation("io.ktor:ktor-server-core") // Ktor core framework
    implementation("io.ktor:ktor-server-netty") // HTTP engine (like Tomcat in Spring)
    implementation("io.ktor:ktor-server-config-yaml") // Read application.yaml config

    // --- Ktor Server Plugins ---
    implementation("io.ktor:ktor-server-content-negotiation") // Auto JSON ↔ Object conversion
    implementation("io.ktor:ktor-serialization-kotlinx-json") // JSON serializer (kotlinx)
    implementation("io.ktor:ktor-server-auth") // Authentication (Basic Auth MVP)
    implementation("io.ktor:ktor-server-cors") // CORS (frontend on different port)
    implementation("io.ktor:ktor-server-status-pages") // Global error handling
    implementation("io.ktor:ktor-server-call-logging") // Request/response logging

    // --- Exposed ORM ---
    // Explicit versions — Exposed is NOT managed by the Ktor BOM
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")

    // --- Database ---
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")

    // --- Logging ---
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // --- Google Drive API ---
    implementation("com.google.api-client:google-api-client:2.7.2")
    implementation("com.google.apis:google-api-services-drive:v3-rev20241027-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.30.1")

    // --- Testing ---
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")


}
