package com.appfactory.domain.port

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp
import com.appfactory.domain.model.TeamId
import kotlin.jvm.JvmInline
import kotlinx.coroutines.flow.Flow

/**
 * Port: ConnectorRegistry
 *
 * Manages the lifecycle of external system integrations.
 * A connector is domain-agnostic: it knows about the external system's
 * data model but nothing about this app's domain.
 *
 * See docs/connector-guide.md for how to add a new connector.
 */
interface ConnectorRegistry {
    /** All connectors this build supports, configured or not. */
    fun available(): List<ConnectorDescriptor>

    /** Only connectors that have been configured by the user internally. */
    fun configured(teamId: TeamId): List<ConfiguredConnector>

    /** Observe live updates to configured connectors. */
    fun observeConfigured(teamId: TeamId): Flow<List<ConfiguredConnector>>

    /** Persist a connector's configuration (credentials, field mappings, schedule). */
    suspend fun configure(
        teamId: TeamId,
        descriptor: ConnectorDescriptor,
        config: ConnectorConfig,
    ): DomainResult<ConfiguredConnector>

    /** Remove a connector configuration. Does not revoke OAuth tokens. */
    suspend fun remove(teamId: TeamId, connectorId: ConnectorId): DomainResult<Unit>

    /** Test whether a configured connector can reach its external system. */
    suspend fun test(teamId: TeamId, connectorId: ConnectorId): DomainResult<TestResult>
}

@JvmInline
value class ConnectorId(val value: String)

data class ConnectorDescriptor(
    val id: ConnectorId,
    val name: String,
    val description: String,
    val oauthService: OAuthService?,       // null for connectors that don't use OAuth
    val capabilities: Set<ConnectorCapability>,
)

enum class ConnectorCapability { PULL, PUSH, REALTIME }

data class ConnectorConfig(
    val connectorId: ConnectorId,
    val credentials: Map<String, String>,
    val fieldMappings: Map<String, String> = emptyMap(),
    val syncSchedule: SyncSchedule = SyncSchedule.Manual,
) {
    fun validate(): DomainResult<ConnectorConfig> {
        if (credentials.isEmpty()) {
            return DomainResult.failure(
                com.appfactory.domain.common.DomainError.ValidationFailed(
                    "Connector config must have at least one credential"
                )
            )
        }
        return DomainResult.success(this)
    }

    fun withCredentials(newCredentials: Map<String, String>): ConnectorConfig =
        copy(credentials = newCredentials)
}

sealed class SyncSchedule {
    data object Manual : SyncSchedule()
    data object Immediate : SyncSchedule()
    data class Hourly(val minuteOffset: Int = 0) : SyncSchedule()
    data class Daily(val hourUtc: Int = 0) : SyncSchedule()
}

data class ConfiguredConnector(
    val id: EntityId,
    val teamId: TeamId,
    val descriptor: ConnectorDescriptor,
    val config: ConnectorConfig,
    val lastModifiedAt: Timestamp,
    val lastSyncedAt: Timestamp? = null,
    val status: ConnectorStatus = ConnectorStatus.Unknown,
)

sealed class ConnectorStatus {
    data object Unknown : ConnectorStatus()
    data object Connected : ConnectorStatus()
    data class Error(val message: String) : ConnectorStatus()
    data object Syncing : ConnectorStatus()
}

data class TestResult(
    val success: Boolean,
    val message: String,
    val latencyMs: Long? = null,
)
