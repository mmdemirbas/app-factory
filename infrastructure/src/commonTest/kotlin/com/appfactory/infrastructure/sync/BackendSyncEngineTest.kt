package com.appfactory.infrastructure.sync

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.SyncResult
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class BackendSyncEngineTest {

    @Test
    fun syncNow_success_mapsTransportSuccessToDomainSuccessAndSyncedState() = runBlocking {
        val transport = FakeBackendSyncTransport(
            triggerResponse = BackendSyncTriggerResponse.Success(
                scope = "feature_flag",
                recordsSynced = 3,
                conflictsResolved = 1,
            ),
            observedState = SyncState.Idle,
        )
        val engine = BackendSyncEngine(transport = transport, pollIntervalMs = 10)
        val scope = SyncScope(entityType = "feature_flag")

        val result = engine.syncNow(scope)

        assertTrue(result is DomainResult.Success<SyncResult>)
        val success = result as DomainResult.Success<SyncResult>
        assertEquals(3, success.value.recordsSynced)
        assertEquals(1, success.value.conflictsResolved)
        assertEquals(scope, transport.lastTriggerScope)
        assertEquals(SyncState.Synced(success.value), engine.observeSyncState(scope).first())
    }

    @Test
    fun syncNow_failure_mapsTransportFailureToDomainFailureAndErrorState() = runBlocking {
        val transport = FakeBackendSyncTransport(
            triggerResponse = BackendSyncTriggerResponse.Failure("backend unavailable"),
            observedState = SyncState.Idle,
        )
        val engine = BackendSyncEngine(transport = transport, pollIntervalMs = 10)
        val scope = SyncScope.All

        val result = engine.syncNow(scope)

        assertTrue(result is DomainResult.Failure)
        val state = engine.observeSyncState(scope).first()
        assertTrue(state is SyncState.Error)
        assertEquals("backend unavailable", (state as SyncState.Error).message)
    }

    @Test
    fun observeSyncState_pollsBackendAndEmitsMappedStates() = runBlocking {
        val transport = FakeBackendSyncTransport(
            triggerResponse = BackendSyncTriggerResponse.Success(SyncScope.All.entityType, 0, 0),
            observedState = SyncState.Syncing,
        )
        val engine = BackendSyncEngine(transport = transport, pollIntervalMs = 10)

        val emissions = withTimeout(500) {
            engine.observeSyncState(SyncScope.All).take(2).toList()
        }

        assertEquals(SyncState.Idle, emissions[0])
        assertEquals(SyncState.Syncing, emissions[1])
    }
}

private class FakeBackendSyncTransport(
    private val triggerResponse: BackendSyncTriggerResponse,
    private val observedState: SyncState,
) : BackendSyncTransport {
    var lastTriggerScope: SyncScope? = null
        private set

    override suspend fun trigger(scope: SyncScope): BackendSyncTriggerResponse {
        lastTriggerScope = scope
        return triggerResponse
    }

    override suspend fun state(scope: SyncScope): SyncState = observedState
}
