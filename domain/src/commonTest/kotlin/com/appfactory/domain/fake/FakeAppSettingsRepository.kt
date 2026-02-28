package com.appfactory.domain.fake

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.model.AppEnvironment
import com.appfactory.domain.model.AppSettings
import com.appfactory.domain.port.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAppSettingsRepository : AppSettingsRepository {
    private val _settings = MutableStateFlow(AppSettings(AppEnvironment.PRODUCTION, true))
    var clearDbCallCount = 0
    var exportDbCallCount = 0

    override suspend fun getSettings(): DomainResult<AppSettings> {
        return DomainResult.success(_settings.value)
    }

    override suspend fun updateSettings(settings: AppSettings): DomainResult<Unit> {
        _settings.value = settings
        return DomainResult.success(Unit)
    }

    override fun observeSettings(): Flow<AppSettings> {
        return _settings.asStateFlow()
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
