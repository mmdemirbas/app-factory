package com.appfactory.infrastructure.sync

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.SyncResult
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class PowerSyncKotlinAdapterTest {

    @Test
    fun observeSyncState_startsIdle() = runTest {
        val adapter = PowerSyncKotlinAdapter()
        val state = adapter.observeSyncState(SyncScope.All).first()
        assertEquals(SyncState.Idle, state)
    }

    @Test
    fun syncNow_online_returnsSuccessAndSyncedState() = runTest {
        val scope = SyncScope(entityType = "app_settings")
        val adapter = PowerSyncKotlinAdapter(isNetworkAvailable = { true })

        val result = adapter.syncNow(scope)
        assertTrue(result is DomainResult.Success<SyncResult>)
        val success = result as DomainResult.Success<SyncResult>
        assertEquals(1, success.value.recordsSynced)
        assertEquals(
            SyncState.Synced(SyncResult(scope = scope, recordsSynced = 1, conflictsResolved = 0)),
            adapter.observeSyncState(scope).first()
        )
    }

    @Test
    fun syncNow_offline_returnsFailureAndOfflineState() = runTest {
        val scope = SyncScope(entityType = "feature_flag")
        val adapter = PowerSyncKotlinAdapter(isNetworkAvailable = { false })

        val result = adapter.syncNow(scope)
        assertTrue(result is DomainResult.Failure)
        assertEquals(SyncState.Offline, adapter.observeSyncState(scope).first())
    }

    @Test
    fun syncNow_whenConnectivityReturns_flushesPreviouslyQueuedScope() = runTest {
        var online = false
        val scope = SyncScope(entityType = "feature_flag")
        val adapter = PowerSyncKotlinAdapter(isNetworkAvailable = { online })

        val offlineResult = adapter.syncNow(scope)
        assertTrue(offlineResult is DomainResult.Failure)
        assertEquals(SyncState.Offline, adapter.observeSyncState(scope).first())

        online = true
        val recoveredResult = adapter.syncNow(scope)

        assertTrue(recoveredResult is DomainResult.Success<SyncResult>)
        val success = recoveredResult as DomainResult.Success<SyncResult>
        assertEquals(1, success.value.recordsSynced)
        assertEquals(SyncState.Synced(success.value), adapter.observeSyncState(scope).first())
    }

    @Test
    fun syncNow_online_flushesAllQueuedScopesInSingleCycle() = runTest {
        var online = false
        val settingsScope = SyncScope(entityType = "app_settings")
        val flagsScope = SyncScope(entityType = "feature_flag")
        val adapter = PowerSyncKotlinAdapter(isNetworkAvailable = { online })

        adapter.syncNow(settingsScope)
        adapter.syncNow(flagsScope)

        online = true
        val result = adapter.syncNow(settingsScope)

        assertTrue(result is DomainResult.Success<SyncResult>)
        val success = result as DomainResult.Success<SyncResult>
        assertEquals(2, success.value.recordsSynced)
    }
}
