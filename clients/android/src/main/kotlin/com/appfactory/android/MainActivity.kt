package com.appfactory.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.appfactory.infrastructure.sync.createPlatformSyncEngine
import com.appfactory.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val syncEngine = remember { createPlatformSyncEngine() }
            App(syncEngine = syncEngine)
        }
    }
}
