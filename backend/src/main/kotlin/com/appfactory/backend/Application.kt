package com.appfactory.backend

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import com.appfactory.backend.health.healthRoutes
import kotlinx.serialization.json.Json

fun main() {
    val port = System.getenv("BACKEND_PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CallLogging)
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    routing {
        healthRoutes()
        // Feature routes are registered here as phases progress:
        // connectorRoutes(...)
        // featureFlagRoutes(...)
        // syncRoutes(...)
        // authRoutes(...)
    }
}
