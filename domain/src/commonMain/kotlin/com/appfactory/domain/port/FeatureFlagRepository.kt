package com.appfactory.domain.port

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.model.FeatureFlag
import kotlinx.coroutines.flow.Flow

/**
 * Port: FeatureFlagRepository
 *
 * CRUD + observation for feature flags.
 * Implementations: SqlDelightFeatureFlagRepository, SupabaseFeatureFlagRepository
 */
interface FeatureFlagRepository {
    suspend fun getAll(): List<FeatureFlag>
    suspend fun getById(id: EntityId): FeatureFlag?
    suspend fun getByKey(key: String): FeatureFlag?
    fun observeAll(): Flow<List<FeatureFlag>>
    suspend fun save(flag: FeatureFlag): DomainResult<FeatureFlag>
    suspend fun delete(id: EntityId): DomainResult<Unit>
}
