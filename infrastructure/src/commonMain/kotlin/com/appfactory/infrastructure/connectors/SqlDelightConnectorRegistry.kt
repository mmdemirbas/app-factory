package com.appfactory.infrastructure.connectors

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.appfactory.domain.AppDatabase
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp
import com.appfactory.domain.port.ConfiguredConnector
import com.appfactory.domain.port.ConnectorConfig
import com.appfactory.domain.port.ConnectorDescriptor
import com.appfactory.domain.port.ConnectorId
import com.appfactory.domain.port.ConnectorRegistry
import com.appfactory.domain.port.ConnectorStatus
import com.appfactory.domain.port.SyncSchedule
import com.appfactory.domain.port.TestResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class SqlDelightConnectorRegistry(
    db: AppDatabase,
    private val availableDescriptors: List<ConnectorDescriptor>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ConnectorRegistry {
    
    private val queries = db.connectorQueries
    private val json = Json { ignoreUnknownKeys = true }

    override fun available(): List<ConnectorDescriptor> = availableDescriptors

    override fun configured(): List<ConfiguredConnector> {
        return queries.selectAll().executeAsList().mapNotNull { entity ->
            mapToDomain(
                id = entity.id,
                descriptorId = entity.descriptor_id,
                configJson = entity.config_json,
                updatedAt = entity.updated_at
            )
        }
    }

    override fun observeConfigured(): Flow<List<ConfiguredConnector>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.mapNotNull { entity ->
                    mapToDomain(
                        id = entity.id,
                        descriptorId = entity.descriptor_id,
                        configJson = entity.config_json,
                        updatedAt = entity.updated_at
                    )
                }
            }
    }

    override suspend fun configure(
        descriptor: ConnectorDescriptor,
        config: ConnectorConfig
    ): DomainResult<ConfiguredConnector> = withContext(ioDispatcher) {
        val validationResult = config.validate()
        if (validationResult.isFailure) {
            val error = (validationResult as com.appfactory.domain.common.DomainResult.Failure).error
            return@withContext DomainResult.failure(error)
        }

        try {
            val entityId = uuid4().toString()
            val configData = ConnectorConfigData(
                credentials = config.credentials,
                fieldMappings = config.fieldMappings,
                syncSchedule = when(config.syncSchedule) {
                    is SyncSchedule.Manual -> "Manual"
                    is SyncSchedule.Immediate -> "Immediate"
                    is SyncSchedule.Hourly -> "Hourly|${(config.syncSchedule as SyncSchedule.Hourly).minuteOffset}"
                    is SyncSchedule.Daily -> "Daily|${(config.syncSchedule as SyncSchedule.Daily).hourUtc}"
                }
            )
            
            val configJson = json.encodeToString(configData)
            val nowTime = Clock.System.now().toEpochMilliseconds()
            
            queries.upsert(
                id = entityId,
                descriptor_id = descriptor.id.value,
                config_json = configJson,
                updated_at = nowTime
            )

            DomainResult.success(
                ConfiguredConnector(
                    id = EntityId(entityId),
                    descriptor = descriptor,
                    config = config.copy(connectorId = config.connectorId),
                    lastModifiedAt = Timestamp(kotlinx.datetime.Instant.fromEpochMilliseconds(nowTime))
                )
            )
        } catch (e: Exception) {
            DomainResult.failure(com.appfactory.domain.common.DomainError.Unknown(e.message ?: "Database error"))
        }
    }

    override suspend fun remove(connectorId: ConnectorId): DomainResult<Unit> = withContext(ioDispatcher) {
        // Here, connectorId from the domain maps loosely to our stored ID 
        // We will remove by the primary assigned ID
        try {
            queries.deleteById(connectorId.value)
            DomainResult.success(Unit)
        } catch(e: Exception) {
             DomainResult.failure(com.appfactory.domain.common.DomainError.Unknown(e.message ?: "Failed to remove connector"))
        }
    }

    override suspend fun test(connectorId: ConnectorId): DomainResult<TestResult> = withContext(ioDispatcher) {
        DomainResult.success(TestResult(success = true, message = "Database mapping mock test successful"))
    }
    
    // Helper to join SQLite row back to Domain entity
    private fun mapToDomain(id: String, descriptorId: String, configJson: String, updatedAt: Long): ConfiguredConnector? {
        val descriptor = availableDescriptors.find { it.id.value == descriptorId } ?: return null
        
        val configData: ConnectorConfigData = try {
            json.decodeFromString(configJson)
        } catch(e: Exception) {
            return null
        }
        
        val schedule = when {
            configData.syncSchedule == "Manual" -> SyncSchedule.Manual
            configData.syncSchedule == "Immediate" -> SyncSchedule.Immediate
            configData.syncSchedule.startsWith("Hourly|") -> SyncSchedule.Hourly(configData.syncSchedule.substringAfter("|").toInt())
            configData.syncSchedule.startsWith("Daily|") -> SyncSchedule.Daily(configData.syncSchedule.substringAfter("|").toInt())
            else -> SyncSchedule.Manual
        }
        
        return ConfiguredConnector(
            id = EntityId(id),
            descriptor = descriptor,
            config = ConnectorConfig(
               connectorId = ConnectorId(id),
               credentials = configData.credentials,
               fieldMappings = configData.fieldMappings,
               syncSchedule = schedule
            ),
            lastModifiedAt = Timestamp(kotlinx.datetime.Instant.fromEpochMilliseconds(updatedAt)),
            status = ConnectorStatus.Unknown
        )
    }
    
    @Serializable
    private data class ConnectorConfigData(
        val credentials: Map<String, String>,
        val fieldMappings: Map<String, String>,
        val syncSchedule: String
    )
}
