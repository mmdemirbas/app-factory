package com.appfactory.domain.fake

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.model.FeatureFlag
import com.appfactory.domain.model.TeamId
import com.appfactory.domain.port.FeatureFlagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFeatureFlagRepository : FeatureFlagRepository {
    private val _flags = MutableStateFlow<Map<EntityId, FeatureFlag>>(emptyMap())

    override suspend fun getAll(teamId: TeamId): List<FeatureFlag> = _flags.value.values.filter { it.teamId == teamId }

    override suspend fun getById(teamId: TeamId, id: EntityId): FeatureFlag? = _flags.value[id]?.takeIf { it.teamId == teamId }

    override suspend fun getByKey(teamId: TeamId, key: String): FeatureFlag? =
        _flags.value.values.firstOrNull { it.key == key && it.teamId == teamId }

    override fun observeAll(teamId: TeamId): Flow<List<FeatureFlag>> =
        _flags.map { map -> map.values.filter { it.teamId == teamId } }

    override suspend fun save(teamId: TeamId, flag: FeatureFlag): DomainResult<FeatureFlag> {
        _flags.value = _flags.value + (flag.id to flag.copy(teamId = teamId))
        return DomainResult.success(flag.copy(teamId = teamId))
    }

    override suspend fun delete(teamId: TeamId, id: EntityId): DomainResult<Unit> {
        _flags.value = _flags.value - id
        return DomainResult.success(Unit)
    }
}
