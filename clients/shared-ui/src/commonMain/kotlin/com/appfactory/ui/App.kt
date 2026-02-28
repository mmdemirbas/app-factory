package com.appfactory.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appfactory.application.sync.ObserveSyncStateUseCase
import com.appfactory.application.sync.TriggerSyncUseCase
import com.appfactory.application.teams.ObserveActiveTeamUseCase
import com.appfactory.application.teams.SwitchActiveTeamUseCase
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.model.TeamId
import com.appfactory.domain.port.ActiveTeamRepository
import com.appfactory.domain.port.SyncEngine
import com.appfactory.domain.port.SyncResult
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    var showDashboard by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(120)
        showDashboard = true
    }

    MaterialTheme(
        colorScheme = AppFactoryColors,
        typography = AppFactoryTypography,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF3EEE6),
                            Color(0xFFEAE0D3),
                            Color(0xFFD7E5E0),
                        )
                    )
                )
        ) {
            val compactLayout = maxWidth < 760.dp
            val horizontalPadding = if (compactLayout) 16.dp else 28.dp
            val verticalPadding = if (compactLayout) 18.dp else 28.dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                DecorativeBackground()

                AnimatedVisibility(
                    visible = showDashboard,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 }),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        HeaderBanner(activeTeamId = activeTeamId, syncState = syncState)

                        if (compactLayout) {
                            TeamCard(
                                activeTeamId = activeTeamId,
                                onSelectTeam = { teamId -> switchActiveTeam(teamId) }
                            )
                            SyncCard(
                                syncState = syncState,
                                lastSyncResult = lastSyncResult,
                                lastError = lastError,
                                onSyncNow = {
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
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    TeamCard(
                                        activeTeamId = activeTeamId,
                                        onSelectTeam = { teamId -> switchActiveTeam(teamId) }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    SyncCard(
                                        syncState = syncState,
                                        lastSyncResult = lastSyncResult,
                                        lastError = lastError,
                                        onSyncNow = {
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
                                    )
                                }
                            }
                        }

                        InsightStrip(syncState = syncState, lastSyncResult = lastSyncResult)
                    }
                }
            }
        }
    }
}

@Composable
private fun DecorativeBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0x33B15A1A), Color.Transparent),
                ),
                RoundedCornerShape(36.dp)
            )
    )
}

@Composable
private fun HeaderBanner(
    activeTeamId: TeamId?,
    syncState: SyncState,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF8EF)),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Control Center",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                ),
                color = Color(0xFF16324A),
            )
            Text(
                text = "Team-first sync operations with explicit scope enforcement.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2F4858),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Active: ${activeTeamId?.value ?: "not selected"}  ·  Sync: ${syncState.toLabel()}",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF486372),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeamCard(
    activeTeamId: TeamId?,
    onSelectTeam: (TeamId) -> Unit,
) {
    val teams = remember {
        listOf(
            TeamOption(id = "team_alpha", label = "Team Alpha"),
            TeamOption(id = "team_beta", label = "Team Beta"),
            TeamOption(id = "team_charlie", label = "Team Charlie"),
            TeamOption(id = "team_delta", label = "Team Delta"),
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F2E8)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Workspace",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF16324A),
            )
            Text(
                text = "Choose the team context used by the current session.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF486372),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                teams.forEach { option ->
                    val selected = activeTeamId?.value == option.id
                    AssistChip(
                        onClick = { onSelectTeam(EntityId(option.id)) },
                        label = {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selected) Color(0xFF16324A) else Color(0xFFE3D9CB),
                            labelColor = if (selected) Color(0xFFFFF7EB) else Color(0xFF2F4858),
                        ),
                        border = null,
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncCard(
    syncState: SyncState,
    lastSyncResult: SyncResult?,
    lastError: String?,
    onSyncNow: () -> Unit,
) {
    val statusColor = statusTone(syncState)
    val statusLabel = syncState.toLabel()
    val statusText = when (syncState) {
        SyncState.Idle -> "Ready to sync changes."
        SyncState.Syncing -> "Synchronization is currently running."
        is SyncState.Synced -> "Last run completed successfully."
        is SyncState.Error -> "Sync pipeline returned an error."
        SyncState.Offline -> "Waiting for connectivity."
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F4ED)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(10.dp)
                        .background(statusColor, RoundedCornerShape(99.dp))
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF16324A),
                )
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF486372),
            )

            lastSyncResult?.let { result ->
                Text(
                    text = "Last result: ${result.recordsSynced} synced · ${result.conflictsResolved} conflicts",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF2F4858),
                )
            }

            lastError?.let { message ->
                Text(
                    text = "Error: $message",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF9C2A2A),
                )
            }

            Button(onClick = onSyncNow, shape = RoundedCornerShape(14.dp)) {
                Text(text = "Run Sync", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InsightStrip(
    syncState: SyncState,
    lastSyncResult: SyncResult?,
) {
    val records = lastSyncResult?.recordsSynced ?: 0
    val conflicts = lastSyncResult?.conflictsResolved ?: 0
    val trend = if (records > 0 && conflicts == 0) "Clean Run" else if (conflicts > 0) "Needs Review" else "Awaiting Data"

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0EB)),
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricColumn(title = "State", value = syncState.toLabel())
            MetricColumn(title = "Records", value = records.toString())
            MetricColumn(title = "Conflicts", value = conflicts.toString())
            MetricColumn(title = "Trend", value = trend)
        }
    }
}

@Composable
private fun MetricColumn(title: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF486372),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF16324A),
        )
    }
}

private fun statusTone(syncState: SyncState): Color = when (syncState) {
    SyncState.Idle -> Color(0xFF6B7280)
    SyncState.Syncing -> Color(0xFFCC7A00)
    is SyncState.Synced -> Color(0xFF2C8A4A)
    is SyncState.Error -> Color(0xFFB33636)
    SyncState.Offline -> Color(0xFF4C5D73)
}

private fun SyncState.toLabel(): String = when (this) {
    SyncState.Idle -> "Idle"
    SyncState.Syncing -> "Syncing"
    is SyncState.Synced -> "Synced"
    is SyncState.Error -> "Error"
    SyncState.Offline -> "Offline"
}

private data class TeamOption(
    val id: String,
    val label: String,
)

private val AppFactoryColors = lightColorScheme(
    primary = Color(0xFF16324A),
    onPrimary = Color(0xFFFFF7EB),
    secondary = Color(0xFF2C8A4A),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFB15A1A),
    background = Color(0xFFF3EEE6),
    onBackground = Color(0xFF16324A),
    surface = Color(0xFFFFF9F1),
    onSurface = Color(0xFF1F3A52),
    error = Color(0xFFB33636),
    onError = Color(0xFFFFFFFF),
)

private val AppFactoryTypography = Typography().copy(
    headlineMedium = Typography().headlineMedium.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = Typography().titleLarge.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
    ),
    titleMedium = Typography().titleMedium.copy(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
    ),
    bodyMedium = Typography().bodyMedium.copy(
        fontFamily = FontFamily.Monospace,
    ),
    labelLarge = Typography().labelLarge.copy(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
    ),
)

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
    private val state = MutableStateFlow<TeamId?>(EntityId("team_alpha"))

    override fun setActiveTeam(teamId: TeamId) {
        state.value = teamId
    }

    override fun observeActiveTeam(): Flow<TeamId?> = state.asStateFlow()

    override suspend fun getActiveTeam(): TeamId? = state.value
}
