package com.appfactory.backend.health

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

fun Route.healthRoutes() {
    get("/health") {
        call.respond(HttpStatusCode.OK, HealthResponse(status = "ok", phase = "1"))
    }
}

@Serializable
data class HealthResponse(
    val status: String,
    val phase: String,
)
