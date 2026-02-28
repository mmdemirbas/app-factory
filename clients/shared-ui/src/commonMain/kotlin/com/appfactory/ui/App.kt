package com.appfactory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appfactory.application.sync.ObserveSyncStateUseCase
import com.appfactory.application.sync.TriggerSyncUseCase
import com.appfactory.application.teams.ObserveActiveTeamUseCase
import com.appfactory.application.teams.SwitchActiveTeamUseCase
import com.appfactory.domain.model.TeamId
import com.appfactory.domain.port.ActiveTeamRepository
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
fun App(
    syncEngine: SyncEngine = PreviewSyncEngine,
    activeTeamRepository: ActiveTeamRepository = PreviewActiveTeamRepository,
) {
    val scope = remember { SyncScope.All }
    val observeSyncState = remember(syncEngine) { ObserveSyncStateUseCase(syncEngine) }
    val triggerSync = remember(syncEngine) { TriggerSyncUseCase(syncEngine) }
    val observeActiveTeam = remember(activeTeamRepository) { ObserveActiveTeamUseCase(activeTeamRepository) }
    val switchActiveTeam = remember(activeTeamRepository) { SwitchActiveTeamUseCase(activeTeamRepository) }

    val syncState by observeSyncState(scope).collectAsState(initial = SyncState.Idle)
    val activeTeamId by observeActiveTeam().collectAsState(initial = null)

    var lastSyncResult by remember { mutableStateOf<SyncResult?>(null) }
    var lastError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isHeightUnbounded = maxHeight == Dp.Infinity
            val viewportHeight = if (isHeightUnbounded) 320.dp else maxHeight
            val layoutMode = when {
                viewportHeight < 180.dp -> DashboardLayoutMode.Micro
                viewportHeight < 320.dp -> DashboardLayoutMode.Compact
                else -> DashboardLayoutMode.Regular
            }
            val outerPadding = when (layoutMode) {
                DashboardLayoutMode.Micro -> 8.dp
                DashboardLayoutMode.Compact -> 12.dp
                DashboardLayoutMode.Regular -> 24.dp
            }
            val cardPadding = when (layoutMode) {
                DashboardLayoutMode.Micro -> 8.dp
                DashboardLayoutMode.Compact -> 12.dp
                DashboardLayoutMode.Regular -> 16.dp
            }
            val cardSpacing = when (layoutMode) {
                DashboardLayoutMode.Micro -> 6.dp
                DashboardLayoutMode.Compact -> 8.dp
                DashboardLayoutMode.Regular -> 10.dp
            }
            val showHeader = layoutMode != DashboardLayoutMode.Micro
            val showSubtitle = layoutMode == DashboardLayoutMode.Regular
            val showHistory = layoutMode == DashboardLayoutMode.Regular

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(outerPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = if (layoutMode == DashboardLayoutMode.Regular && !isHeightUnbounded) {
                    Arrangement.Center
                } else {
                    Arrangement.Top
                },
            ) {
                if (showHeader) {
                    Text(
                        text = "App Factory",
                        fontSize = if (layoutMode == DashboardLayoutMode.Compact) 24.sp else 32.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (showSubtitle) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Phase 5 — Sync dashboard",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                    Spacer(modifier = Modifier.height(if (layoutMode == DashboardLayoutMode.Compact) 12.dp else 20.dp))
                }

                Card {
                    Column(
                        modifier = Modifier.padding(cardPadding),
                        verticalArrangement = Arrangement.spacedBy(cardSpacing),
                    ) {
                        Text(
                            text = "Active Team: ${activeTeamId?.value ?: "None"}",
                            fontWeight = FontWeight.Bold,
                        )
                        androidx.compose.foundation.layout.Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Team Alpha", "Team Beta", "Team Charlie").forEach { teamName ->
                                Button(onClick = { switchActiveTeam(com.appfactory.domain.common.EntityId(teamName)) }) {
                                    Text(teamName)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card {
                    Column(
                        modifier = Modifier.padding(cardPadding),
                        verticalArrangement = Arrangement.spacedBy(cardSpacing),
                    ) {
                        Text(
                            text = "Current state: ${syncState.toLabel()}",
                            fontWeight = FontWeight.Medium,
                        )
                        if (showHistory) {
                            lastSyncResult?.let { result ->
                                Text(
                                    text = "Last result: ${result.recordsSynced} synced, ${result.conflictsResolved} conflicts resolved",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                )
                            }
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
                if (layoutMode != DashboardLayoutMode.Regular) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private enum class DashboardLayoutMode {
    Micro,
    Compact,
    Regular,
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

private object PreviewActiveTeamRepository : ActiveTeamRepository {
    private val state = MutableStateFlow<TeamId?>(com.appfactory.domain.common.EntityId("default_team"))

    override fun setActiveTeam(teamId: TeamId) {
        state.value = teamId
    }

    override fun observeActiveTeam(): Flow<TeamId?> = state.asStateFlow()

    override suspend fun getActiveTeam(): TeamId? = state.value
}
