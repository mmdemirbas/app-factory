package com.appfactory.domain.fake

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.model.FeatureFlag
import com.appfactory.domain.port.FeatureFlagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFeatureFlagRepository : FeatureFlagRepository {
    private val _flags = MutableStateFlow<Map<EntityId, FeatureFlag>>(emptyMap())

    override suspend fun getAll(): List<FeatureFlag> = _flags.value.values.toList()

    override suspend fun getById(id: EntityId): FeatureFlag? = _flags.value[id]

    override suspend fun getByKey(key: String): FeatureFlag? =
        _flags.value.values.firstOrNull { it.key == key }

    override fun observeAll(): Flow<List<FeatureFlag>> =
        _flags.map { it.values.toList() }

    override suspend fun save(flag: FeatureFlag): DomainResult<FeatureFlag> {
        _flags.value = _flags.value + (flag.id to flag)
        return DomainResult.success(flag)
    }

    override suspend fun delete(id: EntityId): DomainResult<Unit> {
        _flags.value = _flags.value - id
        return DomainResult.success(Unit)
    }
}
