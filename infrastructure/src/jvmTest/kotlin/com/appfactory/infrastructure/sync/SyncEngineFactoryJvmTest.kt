package com.appfactory.infrastructure.sync

import kotlin.test.Test
import kotlin.test.assertTrue

class SyncEngineFactoryJvmTest {

    @Test
    fun createPlatformSyncEngine_returnsKotlinAdapterOnJvm() {
        assertTrue(createPlatformSyncEngine() is PowerSyncKotlinAdapter)
    }
}
