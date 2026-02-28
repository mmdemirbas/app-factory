package com.appfactory.domain.port

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.model.FeatureFlag
import com.appfactory.domain.model.TeamId
import kotlinx.coroutines.flow.Flow

/**
 * Port: FeatureFlagRepository
 *
 * CRUD + observation for feature flags.
 * Implementations: SqlDelightFeatureFlagRepository, SupabaseFeatureFlagRepository
 */
interface FeatureFlagRepository {
    suspend fun getAll(teamId: TeamId): List<FeatureFlag>
    suspend fun getById(teamId: TeamId, id: EntityId): FeatureFlag?
    suspend fun getByKey(teamId: TeamId, key: String): FeatureFlag?
    fun observeAll(teamId: TeamId): Flow<List<FeatureFlag>>
    suspend fun save(teamId: TeamId, flag: FeatureFlag): DomainResult<FeatureFlag>
    suspend fun delete(teamId: TeamId, id: EntityId): DomainResult<Unit>
}
