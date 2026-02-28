package com.appfactory.infrastructure.sync

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.SyncEngine
import com.appfactory.domain.port.SyncResult
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Phase 5 foundation adapter.
 *
 * This is a platform-safe SyncEngine implementation for Android/iOS/Desktop
 * until full PowerSync SDK integration and replication wiring are completed.
 */
class PowerSyncKotlinAdapter(
    private val isNetworkAvailable: () -> Boolean = { true },
) : SyncEngine {
    private val scopeStates = mutableMapOf<SyncScope, MutableStateFlow<SyncState>>()

    override suspend fun syncNow(scope: SyncScope): DomainResult<SyncResult> {
        val state = stateFor(scope)
        if (!isNetworkAvailable()) {
            state.value = SyncState.Offline
            return DomainResult.failure(
                DomainError.ExternalServiceError("Device is offline; sync skipped")
            )
        }

        state.value = SyncState.Syncing
        val result = SyncResult(
            scope = scope,
            recordsSynced = 0,
            conflictsResolved = 0,
        )
        state.value = SyncState.Synced(result)
        return DomainResult.success(result)
    }

    override fun observeSyncState(scope: SyncScope): Flow<SyncState> {
        val state = stateFor(scope)
        if (!isNetworkAvailable()) {
            state.value = SyncState.Offline
        }
        return state.asStateFlow()
    }

    private fun stateFor(scope: SyncScope): MutableStateFlow<SyncState> =
        scopeStates.getOrPut(scope) { MutableStateFlow(SyncState.Idle) }
}
