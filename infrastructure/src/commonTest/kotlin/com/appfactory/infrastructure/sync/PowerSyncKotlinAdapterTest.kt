package com.appfactory.infrastructure.sync

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.SyncResult
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class PowerSyncKotlinAdapterTest {

    @Test
    fun observeSyncState_startsIdle() = runBlocking {
        val adapter = PowerSyncKotlinAdapter()
        val state = adapter.observeSyncState(SyncScope.All).first()
        assertEquals(SyncState.Idle, state)
    }

    @Test
    fun syncNow_online_returnsSuccessAndSyncedState() = runBlocking {
        val scope = SyncScope(entityType = "app_settings")
        val adapter = PowerSyncKotlinAdapter(isNetworkAvailable = { true })

        val result = adapter.syncNow(scope)
        assertTrue(result is DomainResult.Success<SyncResult>)
        assertEquals(
            SyncState.Synced(SyncResult(scope = scope, recordsSynced = 0, conflictsResolved = 0)),
            adapter.observeSyncState(scope).first()
        )
    }

    @Test
    fun syncNow_offline_returnsFailureAndOfflineState() = runBlocking {
        val scope = SyncScope(entityType = "feature_flag")
        val adapter = PowerSyncKotlinAdapter(isNetworkAvailable = { false })

        val result = adapter.syncNow(scope)
        assertTrue(result is DomainResult.Failure)
        assertEquals(SyncState.Offline, adapter.observeSyncState(scope).first())
    }
}
