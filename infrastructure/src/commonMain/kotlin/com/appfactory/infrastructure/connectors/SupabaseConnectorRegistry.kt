package com.appfactory.infrastructure.connectors

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp
import com.appfactory.domain.port.ConfiguredConnector
import com.appfactory.domain.port.ConnectorConfig
import com.appfactory.domain.port.ConnectorDescriptor
import com.appfactory.domain.port.ConnectorRegistry
import com.appfactory.domain.port.ConnectorStatus
import com.appfactory.domain.model.TeamId
import com.appfactory.domain.port.TestResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SupabaseConnectorRegistry(
    private val supabaseClient: SupabaseClient,
    private val availableConnectors: List<ConnectorDescriptor>,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : ConnectorRegistry {
    override fun available(): List<ConnectorDescriptor> = availableConnectors

    override fun configured(teamId: TeamId): List<ConfiguredConnector> = emptyList()

    suspend fun fetchConfigured(teamId: TeamId): List<ConfiguredConnector> {
        return try {
            val result = supabaseClient.from("configured_connector").select {
                filter { eq("team_id", teamId.value) }
            }
            result.decodeList<ConfiguredConnectorDto>().mapNotNull { mapToDomain(teamId, it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun observeConfigured(teamId: TeamId): Flow<List<ConfiguredConnector>> = emptyFlow()

    override suspend fun configure(
        teamId: TeamId,
        descriptor: ConnectorDescriptor,
        config: ConnectorConfig,
    ): DomainResult<ConfiguredConnector> {
        return try {
            val serializedConfig = SerializedConnectorConfig(
                credentials = config.credentials,
                fieldMappings = config.fieldMappings,
            )

            val payload = ConfiguredConnectorDto(
                id = "conn_${descriptor.id.value}",
                teamId = teamId.value,
                descriptorId = descriptor.id.value,
                configJson = json.encodeToString(serializedConfig),
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            )

            supabaseClient.from("configured_connector").upsert(payload)

            mapToDomain(teamId, payload)?.let { DomainResult.success(it) }
                ?: DomainResult.failure(
                    DomainError.Unknown("Failed parsing domain model post-upsert")
                )
        } catch (e: Exception) {
            DomainResult.failure(
                DomainError.ExternalServiceError(
                    e.message ?: "Failed configuring connector in Supabase"
                )
            )
        }
    }

    override suspend fun remove(teamId: TeamId, connectorId: com.appfactory.domain.port.ConnectorId): DomainResult<Unit> {
        return try {
            supabaseClient.from("configured_connector").delete {
                filter {
                    eq("descriptor_id", connectorId.value)
                    eq("team_id", teamId.value)
                }
            }
            DomainResult.success(Unit)
        } catch (e: Exception) {
            DomainResult.failure(
                DomainError.ExternalServiceError(
                    e.message ?: "Failed to remove connector from Supabase"
                )
            )
        }
    }

    override suspend fun test(teamId: TeamId, connectorId: com.appfactory.domain.port.ConnectorId): DomainResult<TestResult> {
        return DomainResult.failure(
            DomainError.Unknown(
                "Test execution is handled by core network components, not Remote registry storage"
            )
        )
    }

    private fun mapToDomain(teamId: TeamId, dto: ConfiguredConnectorDto): ConfiguredConnector? {
        val descriptor = availableConnectors.firstOrNull { it.id.value == dto.descriptorId } ?: return null

        return try {
            val configJson = json.decodeFromString<SerializedConnectorConfig>(dto.configJson)
            val connectorConfig = ConnectorConfig(
                connectorId = descriptor.id,
                credentials = configJson.credentials,
                fieldMappings = configJson.fieldMappings,
            )

            ConfiguredConnector(
                id = EntityId.of(dto.id),
                teamId = teamId,
                descriptor = descriptor,
                config = connectorConfig,
                lastModifiedAt = Timestamp(Instant.fromEpochMilliseconds(dto.updatedAt)),
                status = ConnectorStatus.Unknown,
            )
        } catch (_: Exception) {
            null
        }
    }
}

@Serializable
private data class ConfiguredConnectorDto(
    @SerialName("id")
    val id: String,
    @SerialName("descriptor_id")
    val descriptorId: String,
    @SerialName("config_json")
    val configJson: String,
    @SerialName("updated_at")
    val updatedAt: Long,
    @SerialName("team_id")
    val teamId: String,
)

@Serializable
private data class SerializedConnectorConfig(
    val credentials: Map<String, String>,
    val fieldMappings: Map<String, String>,
)
