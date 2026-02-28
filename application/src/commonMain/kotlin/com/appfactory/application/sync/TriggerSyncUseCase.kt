package com.appfactory.application.sync

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.SyncEngine
import com.appfactory.domain.port.SyncResult
import com.appfactory.domain.port.SyncScope

class TriggerSyncUseCase(
    private val syncEngine: SyncEngine
) {
    suspend operator fun invoke(scope: SyncScope): DomainResult<SyncResult> {
        return syncEngine.syncNow(scope)
    }
}
