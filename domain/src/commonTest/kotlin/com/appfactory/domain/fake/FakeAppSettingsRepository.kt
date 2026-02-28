package com.appfactory.domain.fake

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.model.AppEnvironment
import com.appfactory.domain.model.AppSettings
import com.appfactory.domain.model.TeamId
import com.appfactory.domain.port.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeAppSettingsRepository : AppSettingsRepository {
    private val _settings = MutableStateFlow<Map<TeamId, AppSettings>>(emptyMap())
    var clearDbCallCount = 0
    var exportDbCallCount = 0

    override suspend fun getSettings(teamId: TeamId): DomainResult<AppSettings> {
        val settings = _settings.value[teamId] ?: AppSettings(teamId = teamId, environment = AppEnvironment.PRODUCTION, isAutoSyncEnabled = true)
        return DomainResult.success(settings)
    }

    override suspend fun updateSettings(teamId: TeamId, settings: AppSettings): DomainResult<Unit> {
        _settings.value = _settings.value + (teamId to settings)
        return DomainResult.success(Unit)
    }

    override fun observeSettings(teamId: TeamId): Flow<AppSettings> {
        return _settings.map { it[teamId] ?: AppSettings(teamId = teamId, environment = AppEnvironment.PRODUCTION, isAutoSyncEnabled = true) }
    }

    override suspend fun clearLocalDb(): DomainResult<Unit> {
        clearDbCallCount++
        return DomainResult.success(Unit)
    }

    override suspend fun exportLocalDb(): DomainResult<ByteArray> {
        exportDbCallCount++
        return DomainResult.success("fake-db-bytes".encodeToByteArray())
    }
}
