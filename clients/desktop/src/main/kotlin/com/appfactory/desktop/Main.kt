package com.appfactory.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.remember
import com.appfactory.infrastructure.sync.SyncEngineMode
import com.appfactory.infrastructure.sync.createPlatformSyncEngine
import com.appfactory.ui.App

private val syncMode: SyncEngineMode = SyncEngineMode.PowerSync

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "App Factory",
    ) {
        val syncEngine = remember { createPlatformSyncEngine(syncMode) }
        App(syncEngine = syncEngine)
    }
}
