package com.appfactory.application.settings

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.model.AppSettings
import com.appfactory.domain.port.AppSettingsRepository
import kotlinx.coroutines.flow.Flow

import com.appfactory.domain.model.TeamId

class GetSettingsUseCase(
    private val repository: AppSettingsRepository
) {
    suspend operator fun invoke(teamId: TeamId): DomainResult<AppSettings> {
        return repository.getSettings(teamId)
    }
}

class UpdateSettingsUseCase(
    private val repository: AppSettingsRepository
) {
    suspend operator fun invoke(teamId: TeamId, settings: AppSettings): DomainResult<Unit> {
        return repository.updateSettings(teamId, settings)
    }
}

class ObserveSettingsUseCase(
    private val repository: AppSettingsRepository
) {
    operator fun invoke(teamId: TeamId): Flow<AppSettings> {
        return repository.observeSettings(teamId)
    }
}

class ClearLocalDbUseCase(
    private val repository: AppSettingsRepository
) {
    suspend operator fun invoke(): DomainResult<Unit> {
        return repository.clearLocalDb()
    }
}

class ExportLocalDbUseCase(
    private val repository: AppSettingsRepository
) {
    suspend operator fun invoke(): DomainResult<ByteArray> {
        return repository.exportLocalDb()
    }
}
