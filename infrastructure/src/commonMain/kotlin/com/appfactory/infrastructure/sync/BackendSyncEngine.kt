package com.appfactory.infrastructure.sync

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.SyncEngine
import com.appfactory.domain.port.SyncResult
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Real transport-backed SyncEngine for local/dev end-to-end validation.
 *
 * It delegates trigger/state operations to a backend transport implementation
 * while keeping domain-facing state semantics in one place.
 */
class BackendSyncEngine(
    private val transport: BackendSyncTransport,
    private val pollIntervalMs: Long = 1000L,
) : SyncEngine {
    private val scopeStates = mutableMapOf<SyncScope, SyncState>()

    override suspend fun syncNow(scope: SyncScope): DomainResult<SyncResult> {
        updateScopeState(scope, SyncState.Syncing)

        val response = runCatching { transport.trigger(scope) }.getOrElse { error ->
            val message = error.message ?: "Failed to trigger sync"
            updateScopeState(scope, SyncState.Error(message))
            return DomainResult.failure(DomainError.ExternalServiceError(message))
        }

        return when (response) {
            is BackendSyncTriggerResponse.Success -> {
                val resultScope = response.scope.toSyncScopeOrFallback(scope)
                val result = SyncResult(
                    scope = resultScope,
                    recordsSynced = response.recordsSynced,
                    conflictsResolved = response.conflictsResolved,
                )
                updateScopeState(scope, SyncState.Synced(result))
                DomainResult.success(result)
            }

            is BackendSyncTriggerResponse.Failure -> {
                val message = response.message.ifBlank { "Sync trigger failed" }
                updateScopeState(scope, SyncState.Error(message))
                DomainResult.failure(DomainError.ExternalServiceError(message))
            }
        }
    }

    override fun observeSyncState(scope: SyncScope): Flow<SyncState> = flow {
        emit(stateFor(scope))

        while (currentCoroutineContext().isActive) {
            val nextState = runCatching { transport.state(scope) }
                .getOrElse { SyncState.Error("Failed to poll sync state: ${it.message ?: "unknown"}") }
            updateScopeState(scope, nextState)
            emit(nextState)
            delay(pollIntervalMs)
        }
    }.distinctUntilChanged()

    private fun stateFor(scope: SyncScope): SyncState =
        scopeStates.getOrPut(scope) { SyncState.Idle }

    private fun updateScopeState(scope: SyncScope, state: SyncState) {
        scopeStates[scope] = state
    }
}

interface BackendSyncTransport {
    suspend fun trigger(scope: SyncScope): BackendSyncTriggerResponse
    suspend fun state(scope: SyncScope): SyncState
}

sealed class BackendSyncTriggerResponse {
    data class Success(
        val scope: String,
        val recordsSynced: Int,
        val conflictsResolved: Int,
    ) : BackendSyncTriggerResponse()

    data class Failure(
        val message: String,
    ) : BackendSyncTriggerResponse()
}

internal object BackendSyncHttpContract {
    private val json = Json { ignoreUnknownKeys = true }

    fun triggerRequestBody(scope: SyncScope): String =
        if (scope == SyncScope.All) "{}" else """{"entityType":"${scope.entityType}"}"""

    fun parseTriggerResponse(statusCode: Int, body: String): BackendSyncTriggerResponse {
        if (statusCode in 200..299) {
            val root = runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull()
            val scope = root?.get("scope")?.jsonPrimitive?.contentOrNull ?: SyncScope.All.entityType
            val recordsSynced = root?.get("recordsSynced")?.jsonPrimitive?.intOrNull ?: 0
            val conflictsResolved = root?.get("conflictsResolved")?.jsonPrimitive?.intOrNull ?: 0
            return BackendSyncTriggerResponse.Success(
                scope = scope,
                recordsSynced = recordsSynced,
                conflictsResolved = conflictsResolved,
            )
        }

        val message = runCatching {
            json.parseToJsonElement(body).jsonObject["message"]?.jsonPrimitive?.contentOrNull
        }.getOrNull() ?: "HTTP $statusCode"

        return BackendSyncTriggerResponse.Failure(message)
    }

    fun parseStateResponse(statusCode: Int, body: String, scope: SyncScope): SyncState {
        if (statusCode !in 200..299) {
            return SyncState.Error("HTTP $statusCode")
        }

        val label = runCatching {
            json.parseToJsonElement(body).jsonObject["state"]?.jsonPrimitive?.contentOrNull
        }.getOrNull() ?: "Error"

        return when (label) {
            "Idle" -> SyncState.Idle
            "Syncing" -> SyncState.Syncing
            "Synced" -> SyncState.Synced(SyncResult(scope = scope, recordsSynced = 0, conflictsResolved = 0))
            "Offline" -> SyncState.Offline
            else -> SyncState.Error("Backend reported state=$label")
        }
    }
}

private fun String.toSyncScopeOrFallback(fallback: SyncScope): SyncScope =
    when {
        isBlank() -> fallback
        this == SyncScope.All.entityType -> SyncScope.All
        else -> SyncScope(entityType = this)
    }
