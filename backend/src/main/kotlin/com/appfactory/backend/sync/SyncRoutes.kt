package com.appfactory.backend.sync

import com.appfactory.application.sync.ObserveSyncStateUseCase
import com.appfactory.application.sync.TriggerSyncUseCase
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.SyncEngine
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

fun Route.syncRoutes(syncEngine: SyncEngine) {
    val triggerSync = TriggerSyncUseCase(syncEngine)
    val observeSyncState = ObserveSyncStateUseCase(syncEngine)

    route("/api/sync") {
        post("/trigger") {
            val request = runCatching { call.receiveNullable<SyncScopeRequest>() }.getOrNull()
            val scope = request?.toScope() ?: SyncScope.All

            when (val result = triggerSync(scope)) {
                is DomainResult.Success -> {
                    call.respond(
                        HttpStatusCode.OK,
                        TriggerSyncResponse(
                            status = "ok",
                            scope = result.value.scope.entityType,
                            recordsSynced = result.value.recordsSynced,
                            conflictsResolved = result.value.conflictsResolved,
                        )
                    )
                }

                is DomainResult.Failure -> {
                    call.respond(
                        HttpStatusCode.BadGateway,
                        mapOf("status" to "error", "message" to result.error.message)
                    )
                }
            }
        }

        get("/state") {
            val entityType = call.queryParameters["entityType"]
            val scope = if (entityType.isNullOrBlank()) SyncScope.All else SyncScope(entityType = entityType)
            val state = observeSyncState(scope).first()
            call.respond(HttpStatusCode.OK, SyncStateResponse(state = state.toLabel()))
        }
    }
}

@Serializable
private data class SyncScopeRequest(
    val entityType: String? = null,
)

@Serializable
private data class TriggerSyncResponse(
    val status: String,
    val scope: String,
    val recordsSynced: Int,
    val conflictsResolved: Int,
)

@Serializable
private data class SyncStateResponse(
    val state: String,
)

private fun SyncScopeRequest.toScope(): SyncScope =
    if (entityType.isNullOrBlank()) SyncScope.All else SyncScope(entityType = entityType)

private fun SyncState.toLabel(): String = when (this) {
    SyncState.Idle -> "Idle"
    SyncState.Syncing -> "Syncing"
    is SyncState.Synced -> "Synced"
    is SyncState.Error -> "Error"
    SyncState.Offline -> "Offline"
}
