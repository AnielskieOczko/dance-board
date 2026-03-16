package com.danceboard.backend
import com.danceboard.backend.config.configureDatabases
import com.danceboard.backend.repository.DanceMaterialRepository
import com.danceboard.backend.routes.danceMaterialRoutes
import com.danceboard.backend.service.DanceMaterialService
import com.danceboard.backend.service.GoogleDriveService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Application")

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                encodeDefaults = true
            }
        )
    }

    install(CORS) {
        val frontendUrl = System.getenv("FRONTEND_URL") ?: "localhost:8081"
        allowHost(frontendUrl, schemes = listOf("http", "https"))
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }

    configureDatabases()

    val danceMaterialRepository = DanceMaterialRepository()
    val googleDriveService = GoogleDriveService(
        folderId = environment.config.property("google.drive.folderId").getString(),
        clientId = environment.config.property("google.drive.clientId").getString(),
        clientSecret = environment.config.property("google.drive.clientSecret").getString(),
        refreshToken = environment.config.property("google.drive.refreshToken").getString(),
    )
    val danceMaterialService = DanceMaterialService(
        danceMaterialRepository,
        googleDriveService = googleDriveService
    )

import io.ktor.server.http.content.singlePageApplication

// ... existing code ...

    routing {
        danceMaterialRoutes(danceMaterialService)

        // Serwowanie plików po kompilacji z wewnątrz pliku *.JAR (/resources/static)
        singlePageApplication {
            useResources = true
            filesPath = "static"
            defaultPage = "index.html"
            ignoreFiles { it.endsWith(".txt") }
        }
    }
}