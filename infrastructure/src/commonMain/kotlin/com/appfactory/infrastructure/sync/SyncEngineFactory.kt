package com.appfactory.infrastructure.sync

import com.appfactory.domain.port.SyncEngine

sealed interface SyncEngineMode {
    data object PowerSync : SyncEngineMode
    data class BackendTransport(val baseUrl: String) : SyncEngineMode
}

expect fun createPlatformSyncEngine(mode: SyncEngineMode): SyncEngine
