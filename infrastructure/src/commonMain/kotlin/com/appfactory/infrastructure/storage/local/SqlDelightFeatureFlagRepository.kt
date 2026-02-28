package com.appfactory.infrastructure.storage.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.appfactory.domain.AppDatabase
import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp
import com.appfactory.domain.model.FeatureFlag
import com.appfactory.domain.port.FeatureFlagRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class SqlDelightFeatureFlagRepository(
    db: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : FeatureFlagRepository {
    
    private val queries = db.featureFlagQueries

    override suspend fun getAll(): List<FeatureFlag> = withContext(ioDispatcher) {
        queries.selectAll().executeAsList().map { it.toDomain() }
    }

    override suspend fun getById(id: EntityId): FeatureFlag? = withContext(ioDispatcher) {
        queries.selectById(id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getByKey(key: String): FeatureFlag? = withContext(ioDispatcher) {
        // Our SQL query is strictly by ID right now. Let's filter in memory for Phase 3 parity
        // In a real app we'd add `selectByKey` to the .sq file.
        queries.selectAll().executeAsList().find { it.key == key }?.toDomain()
    }

    override fun observeAll(): Flow<List<FeatureFlag>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun save(flag: FeatureFlag): DomainResult<FeatureFlag> = withContext(ioDispatcher) {
        try {
            queries.upsert(
                id = flag.id.value,
                key = flag.key,
                description = flag.description,
                default_enabled = if (flag.defaultEnabled) 1L else 0L,
                created_at = flag.createdAt.instant.toEpochMilliseconds(),
                updated_at = Clock.System.now().toEpochMilliseconds()
            )
            DomainResult.success(flag)
        } catch (e: Exception) {
            DomainResult.failure(DomainError.Unknown(e.message ?: "Failed to save feature flag"))
        }
    }

    override suspend fun delete(id: EntityId): DomainResult<Unit> = withContext(ioDispatcher) {
        try {
            queries.deleteById(id.value)
            DomainResult.success(Unit)
        } catch (e: Exception) {
            DomainResult.failure(DomainError.Unknown(e.message ?: "Failed to delete feature flag"))
        }
    }

    private fun com.appfactory.domain.FeatureFlag.toDomain() = FeatureFlag(
        id = EntityId(id),
        key = key,
        description = description,
        defaultEnabled = default_enabled == 1L,
        createdAt = Timestamp(Instant.fromEpochMilliseconds(created_at)),
        updatedAt = Timestamp(Instant.fromEpochMilliseconds(updated_at))
    )
}
