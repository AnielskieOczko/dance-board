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
import io.ktor.server.http.content.ignoreFiles
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Application")

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, configure = {
        // Włączamy obsługę HTTP/2 (h2c) dla Cloud Run, aby ominąć limit 32MB dla HTTP/1.1
        enableHttp2 = true
        enableH2c = true
    }) {
        module()
    }.start(wait = true)
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
        // Pozwalamy na dowolny host w produkcji (w monolicie origin i tak jest ten sam, 
        // ale Ktor CORS potrzebuje dopasowania jeśli Origin header jest obecny)
        anyHost() 
        
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
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