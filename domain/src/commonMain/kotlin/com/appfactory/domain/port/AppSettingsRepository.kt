package com.appfactory.domain.port

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.model.AppSettings
import com.appfactory.domain.model.TeamId
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    suspend fun getSettings(teamId: TeamId): DomainResult<AppSettings>
    suspend fun updateSettings(teamId: TeamId, settings: AppSettings): DomainResult<Unit>
    fun observeSettings(teamId: TeamId): Flow<AppSettings>
    
    // Developer tool features that persist to local DB infrastructure
    suspend fun clearLocalDb(): DomainResult<Unit>
    suspend fun exportLocalDb(): DomainResult<ByteArray> // Representing the SQLite DB file bytes
}
