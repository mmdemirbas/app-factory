package com.appfactory.application.sync

import com.appfactory.domain.fake.FakeSyncEngine
import com.appfactory.domain.port.SyncResult
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class SyncUseCasesTest : StringSpec({

    "TriggerSyncUseCase returns sync result from engine" {
        val fakeEngine = FakeSyncEngine()
        val triggerSync = TriggerSyncUseCase(fakeEngine)

        val result = triggerSync(SyncScope.All)
        result.isSuccess shouldBe true
        result.getOrNull()?.isSuccess shouldBe true
        
        fakeEngine.syncNowCallCount shouldBe 1
    }

    "ObserveSyncStateUseCase exposes the current sync state" {
        val fakeEngine = FakeSyncEngine()
        val observeState = ObserveSyncStateUseCase(fakeEngine)

        val state = observeState(SyncScope.All).first()
        state shouldBe SyncState.Idle
    }
})
