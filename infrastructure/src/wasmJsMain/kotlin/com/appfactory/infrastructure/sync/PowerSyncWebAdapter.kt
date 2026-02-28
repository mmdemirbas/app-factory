package com.appfactory.infrastructure.sync

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.SyncEngine
import com.appfactory.domain.port.SyncResult
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Phase 5 foundation adapter for Web/Wasm.
 */
class PowerSyncWebAdapter(
    private val isNetworkAvailable: () -> Boolean = { window.navigator.onLine },
) : SyncEngine {
    private val scopeStates = mutableMapOf<SyncScope, MutableStateFlow<SyncState>>()
    private val pendingScopes = linkedSetOf<SyncScope>()

    override suspend fun syncNow(scope: SyncScope): DomainResult<SyncResult> {
        val state = stateFor(scope)
        if (!isNetworkAvailable()) {
            pendingScopes += scope
            state.value = SyncState.Offline
            return DomainResult.failure(
                DomainError.ExternalServiceError("Browser is offline; sync skipped")
            )
        }

        val scopesToSync = linkedSetOf<SyncScope>().apply {
            addAll(pendingScopes)
            add(scope)
        }

        scopesToSync.forEach { queuedScope ->
            stateFor(queuedScope).value = SyncState.Syncing
        }
        scopesToSync.forEach { queuedScope ->
            stateFor(queuedScope).value = SyncState.Synced(
                SyncResult(
                    scope = queuedScope,
                    recordsSynced = 1,
                    conflictsResolved = 0,
                )
            )
        }
        pendingScopes.clear()

        return DomainResult.success(
            SyncResult(
                scope = scope,
                recordsSynced = scopesToSync.size,
                conflictsResolved = 0,
            )
        )
    }

    override fun observeSyncState(scope: SyncScope): Flow<SyncState> {
        val state = stateFor(scope)
        if (!isNetworkAvailable()) {
            state.value = SyncState.Offline
        } else if (state.value == SyncState.Offline && scope !in pendingScopes) {
            state.value = SyncState.Idle
        }
        return state.asStateFlow()
    }

    private fun stateFor(scope: SyncScope): MutableStateFlow<SyncState> =
        scopeStates.getOrPut(scope) { MutableStateFlow(SyncState.Idle) }
}
