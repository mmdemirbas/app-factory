package com.appfactory.infrastructure.sync

import com.appfactory.domain.port.SyncEngine

expect fun createPlatformSyncEngine(): SyncEngine
