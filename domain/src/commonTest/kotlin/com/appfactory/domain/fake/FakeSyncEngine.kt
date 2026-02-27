package com.appfactory.domain.fake

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.SyncEngine
import com.appfactory.domain.port.SyncResult
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSyncEngine : SyncEngine {
    var syncResultOverride: SyncResult? = null
    var syncStateFlow = MutableStateFlow<SyncState>(SyncState.Idle)
    
    val syncNowCallCount get() = _syncNowCallCount
    private var _syncNowCallCount = 0

    override suspend fun syncNow(scope: SyncScope): DomainResult<SyncResult> {
        _syncNowCallCount++
        return DomainResult.Success(syncResultOverride ?: SyncResult(scope, 0, 0))
    }

    override fun observeSyncState(scope: SyncScope): Flow<SyncState> = syncStateFlow.asStateFlow()
}
