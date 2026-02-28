package com.appfactory.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.remember
import com.appfactory.infrastructure.sync.createPlatformSyncEngine
import com.appfactory.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "App Factory",
    ) {
        val syncEngine = remember { createPlatformSyncEngine() }
        App(syncEngine = syncEngine)
    }
}
