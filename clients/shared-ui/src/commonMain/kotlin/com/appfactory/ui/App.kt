package com.appfactory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appfactory.application.sync.ObserveSyncStateUseCase
import com.appfactory.application.sync.TriggerSyncUseCase
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.SyncEngine
import com.appfactory.domain.port.SyncResult
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Root composable for all platforms.
 *
 * Phase 5: Sync status dashboard.
 *
 * Platform entry points (android/desktop/web) call this function.
 */
@Composable
fun App(syncEngine: SyncEngine = PreviewSyncEngine) {
    val scope = remember { SyncScope.All }
    val observeSyncState = remember(syncEngine) { ObserveSyncStateUseCase(syncEngine) }
    val triggerSync = remember(syncEngine) { TriggerSyncUseCase(syncEngine) }
    val syncState by observeSyncState(scope).collectAsState(initial = SyncState.Idle)
    var lastSyncResult by remember { mutableStateOf<SyncResult?>(null) }
    var lastError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "App Factory",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Phase 5 — Sync dashboard",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(20.dp))

            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Current state: ${syncState.toLabel()}",
                        fontWeight = FontWeight.Medium,
                    )
                    lastSyncResult?.let { result ->
                        Text(
                            text = "Last result: ${result.recordsSynced} synced, ${result.conflictsResolved} conflicts resolved",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        )
                    }
                    lastError?.let { message ->
                        Text(
                            text = "Last error: $message",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                when (val result = triggerSync(scope)) {
                                    is DomainResult.Success -> {
                                        lastSyncResult = result.value
                                        lastError = null
                                    }

                                    is DomainResult.Failure -> {
                                        lastError = result.error.message
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Sync now")
                    }
                }
            }
        }
    }
}

private fun SyncState.toLabel(): String = when (this) {
    SyncState.Idle -> "Idle"
    SyncState.Syncing -> "Syncing"
    is SyncState.Synced -> "Synced"
    is SyncState.Error -> "Error"
    SyncState.Offline -> "Offline"
}

private object PreviewSyncEngine : SyncEngine {
    private val state = MutableStateFlow<SyncState>(SyncState.Idle)

    override suspend fun syncNow(scope: SyncScope): DomainResult<SyncResult> {
        state.value = SyncState.Syncing
        val result = SyncResult(scope = scope, recordsSynced = 0, conflictsResolved = 0)
        state.value = SyncState.Synced(result)
        return DomainResult.success(result)
    }

    override fun observeSyncState(scope: SyncScope): Flow<SyncState> = state.asStateFlow()
}
