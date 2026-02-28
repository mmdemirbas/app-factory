package com.appfactory.infrastructure.sync

import kotlin.test.Test
import kotlin.test.assertTrue

class SyncEngineFactoryJvmTest {

    @Test
    fun createPlatformSyncEngine_returnsKotlinAdapterOnJvm() {
        assertTrue(createPlatformSyncEngine(SyncEngineMode.PowerSync) is PowerSyncKotlinAdapter)
    }

    @Test
    fun createPlatformSyncEngine_backendMode_returnsBackendSyncEngineOnJvm() {
        val engine = createPlatformSyncEngine(
            mode = SyncEngineMode.BackendTransport(baseUrl = "http://localhost:8081")
        )
        assertTrue(engine is BackendSyncEngine)
    }
}
