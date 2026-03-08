package com.danceboard.backend
import com.danceboard.backend.config.configureDatabases
import com.danceboard.backend.repository.DanceMaterialRepository
import com.danceboard.backend.routes.danceMaterialRoutes
import com.danceboard.backend.service.DanceMaterialService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
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

    configureDatabases()

    val danceMaterialRepository = DanceMaterialRepository()
    val danceMaterialService = DanceMaterialService(danceMaterialRepository)

    routing {
        danceMaterialRoutes(danceMaterialService)
    }
}