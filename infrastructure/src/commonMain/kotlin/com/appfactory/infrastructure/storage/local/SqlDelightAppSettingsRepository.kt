package com.appfactory.infrastructure.storage.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.appfactory.domain.AppDatabase
import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.model.AppEnvironment
import com.appfactory.domain.model.AppSettings
import com.appfactory.domain.port.AppSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import com.appfactory.domain.model.TeamId

class SqlDelightAppSettingsRepository(
    db: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default // Common fallback
) : AppSettingsRepository {
    
    // We expect AppSettings.sq to generate AppSettingsQueries
    private val queries = db.appSettingsQueries

    override suspend fun getSettings(teamId: TeamId): DomainResult<AppSettings> = withContext(ioDispatcher) {
        val result = queries.getSettings(teamId.value).executeAsOneOrNull()
        if (result != null) {
            DomainResult.success(
                AppSettings(
                    teamId = teamId,
                    environment = result.environment.toDomainEnvironment(),
                    isAutoSyncEnabled = result.is_auto_sync_enabled == 1L
                )
            )
        } else {
            // Offline-first default when local DB has not been initialized yet.
            DomainResult.success(AppSettings(teamId = teamId))
        }
    }

    override suspend fun updateSettings(teamId: TeamId, settings: AppSettings): DomainResult<Unit> = withContext(ioDispatcher) {
        try {
            queries.upsert(
                id = teamId.value,
                environment = settings.environment.name,
                is_auto_sync_enabled = if (settings.isAutoSyncEnabled) 1L else 0L,
                updated_at = Clock.System.now().toEpochMilliseconds()
            )
            DomainResult.success(Unit)
        } catch (e: Exception) {
            DomainResult.failure(DomainError.Unknown(e.message ?: "Failed to update settings"))
        }
    }

    override fun observeSettings(teamId: TeamId): Flow<AppSettings> {
        return queries.getSettings(teamId.value)
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map { entity ->
                if (entity != null) {
                    AppSettings(
                        teamId = teamId,
                        environment = entity.environment.toDomainEnvironment(),
                        isAutoSyncEnabled = entity.is_auto_sync_enabled == 1L
                    )
                } else {
                    AppSettings(teamId = teamId)
                }
            }
    }

    override suspend fun clearLocalDb(): DomainResult<Unit> = withContext(ioDispatcher) {
        try {
            queries.clearDb()
            DomainResult.success(Unit)
        } catch (e: Exception) {
            DomainResult.failure(DomainError.Unknown(e.message ?: "Failed to clear DB"))
        }
    }

    override suspend fun exportLocalDb(): DomainResult<ByteArray> = withContext(ioDispatcher) {
        try {
            val jsonExport = """{"mock": "sqlite_export_for_phase3_placeholder"}""".encodeToByteArray()
            DomainResult.success(jsonExport)
        } catch (e: Exception) {
            DomainResult.failure(DomainError.Unknown(e.message ?: "Failed to export DB"))
        }
    }
}

private fun String.toDomainEnvironment(): AppEnvironment =
    AppEnvironment.entries.firstOrNull { it.name == this } ?: AppEnvironment.PRODUCTION
