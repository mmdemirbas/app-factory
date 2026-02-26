package com.appfactory.domain.port

import com.appfactory.domain.common.DomainResult
import kotlinx.coroutines.flow.Flow

/**
 * Port: SyncEngine
 *
 * Responsible for replication between local and remote stores.
 * NOT responsible for deciding whether to read local or remote —
 * that is a use-case decision made by selecting the appropriate repository.
 *
 * Implementations:
 *   - infrastructure/sync/PowerSyncKotlinAdapter (Android, iOS, Desktop)
 *   - infrastructure/sync/PowerSyncWebAdapter (Web, via JS interop)
 */
interface SyncEngine {
    /**
     * Trigger an immediate sync cycle for the given scope.
     * Returns when the sync attempt completes (success or error).
     */
    suspend fun syncNow(scope: SyncScope): DomainResult<SyncResult>

    /**
     * Observe the live sync state for a given scope.
     * Emits updates as sync progresses or connectivity changes.
     */
    fun observeSyncState(scope: SyncScope): Flow<SyncState>
}

/**
 * Identifies which data to sync. entityType maps to a table/collection name.
 * filter can restrict sync to a subset (e.g., by user ID for partial replication).
 */
data class SyncScope(
    val entityType: String,
    val filter: SyncFilter? = null,
)

data class SyncFilter(val field: String, val value: String)

data class SyncResult(
    val scope: SyncScope,
    val recordsSynced: Int,
    val conflictsResolved: Int,
)

sealed class SyncState {
    data object Idle : SyncState()
    data object Syncing : SyncState()
    data class Synced(val result: SyncResult) : SyncState()
    data class Error(val message: String) : SyncState()
    data object Offline : SyncState()
}
