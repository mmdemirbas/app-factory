package com.appfactory.application.sync

import com.appfactory.domain.port.SyncEngine
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import kotlinx.coroutines.flow.Flow

class ObserveSyncStateUseCase(
    private val syncEngine: SyncEngine
) {
    operator fun invoke(scope: SyncScope): Flow<SyncState> {
        return syncEngine.observeSyncState(scope)
    }
}
