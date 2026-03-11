package com.danceboard.backend
import com.danceboard.backend.config.configureDatabases
import com.danceboard.backend.repository.DanceMaterialRepository
import com.danceboard.backend.routes.danceMaterialRoutes
import com.danceboard.backend.service.DanceMaterialService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
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
    val danceMaterialService = DanceMaterialService(danceMaterialRepository)

    routing {
        danceMaterialRoutes(danceMaterialService)
    }
}