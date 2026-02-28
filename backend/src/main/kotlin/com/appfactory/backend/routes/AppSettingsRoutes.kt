package com.appfactory.backend.routes

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.model.AppEnvironment
import com.appfactory.domain.model.AppSettings
import com.appfactory.domain.port.AppSettingsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

fun Route.appSettingsRoutes(settingsRepo: AppSettingsRepository) {
    route("/api/settings") {
        get {
            when (val result = settingsRepo.getSettings()) {
                is DomainResult.Success -> {
                    val settings = result.value
                    call.respond(HttpStatusCode.OK, settings.toResponse())
                }

                is DomainResult.Failure -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to retrieve settings")
                    )
                }
            }
        }

        put {
            val request = call.receive<UpdateSettingsRequest>()
            when (settingsRepo.updateSettings(request.toDomain())) {
                is DomainResult.Success -> {
                    call.respond(HttpStatusCode.OK, mapOf("status" to "updated"))
                }

                is DomainResult.Failure -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to set settings")
                    )
                }
            }
        }
    }
}

@Serializable
private data class UpdateSettingsRequest(
    val environment: String,
    val isAutoSyncEnabled: Boolean,
)

@Serializable
private data class AppSettingsResponse(
    val environment: String,
    val isAutoSyncEnabled: Boolean,
)

private fun UpdateSettingsRequest.toDomain(): AppSettings {
    val env = AppEnvironment.entries.firstOrNull { it.name == environment } ?: AppEnvironment.PRODUCTION
    return AppSettings(environment = env, isAutoSyncEnabled = isAutoSyncEnabled)
}

private fun AppSettings.toResponse(): AppSettingsResponse = AppSettingsResponse(
    environment = environment.name,
    isAutoSyncEnabled = isAutoSyncEnabled
)
