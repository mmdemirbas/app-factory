package com.appfactory.infrastructure.sync

import com.appfactory.domain.port.SyncEngine

actual fun createPlatformSyncEngine(mode: SyncEngineMode): SyncEngine = when (mode) {
    SyncEngineMode.PowerSync -> PowerSyncKotlinAdapter()
    is SyncEngineMode.BackendTransport -> error(
        "SyncEngineMode.BackendTransport is not supported on Android. Use SyncEngineMode.PowerSync."
    )
}
