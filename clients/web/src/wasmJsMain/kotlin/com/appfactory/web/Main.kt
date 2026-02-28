package com.appfactory.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.compose.runtime.remember
import com.appfactory.infrastructure.sync.SyncEngineMode
import com.appfactory.infrastructure.sync.createPlatformSyncEngine
import com.appfactory.ui.App
import kotlinx.browser.document

private val syncMode: SyncEngineMode = SyncEngineMode.PowerSync

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: error("No document body found")
    ComposeViewport(body) {
        val syncEngine = remember { createPlatformSyncEngine(syncMode) }
        App(syncEngine = syncEngine)
    }
}
