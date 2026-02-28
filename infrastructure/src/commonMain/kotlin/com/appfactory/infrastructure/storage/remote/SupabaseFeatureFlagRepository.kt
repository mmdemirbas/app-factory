package com.appfactory.infrastructure.storage.remote

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp
import com.appfactory.domain.model.FeatureFlag
import com.appfactory.domain.port.FeatureFlagRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseFeatureFlagRepository(
    private val supabaseClient: SupabaseClient,
) : FeatureFlagRepository {
    override suspend fun getAll(): List<FeatureFlag> {
        return try {
            val result = supabaseClient.from("feature_flag").select()
            result.decodeList<FeatureFlagDto>().map { it.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getById(id: EntityId): FeatureFlag? {
        return getAll().firstOrNull { it.id == id }
    }

    override suspend fun getByKey(key: String): FeatureFlag? {
        return getAll().firstOrNull { it.key == key }
    }

    override fun observeAll(): Flow<List<FeatureFlag>> = emptyFlow()

    override suspend fun save(flag: FeatureFlag): DomainResult<FeatureFlag> {
        return try {
            val payload = FeatureFlagDto(
                id = flag.id.value,
                name = flag.key,
                description = flag.description,
                isEnabled = if (flag.defaultEnabled) 1 else 0,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            )

            supabaseClient.from("feature_flag").upsert(payload) {
                onConflict = "id"
            }

            DomainResult.success(flag)
        } catch (e: Exception) {
            DomainResult.failure(
                DomainError.ExternalServiceError(e.message ?: "Failed to save flag")
            )
        }
    }

    override suspend fun delete(id: EntityId): DomainResult<Unit> {
        return try {
            supabaseClient.from("feature_flag").delete {
                filter {
                    eq("id", id.value)
                }
            }
            DomainResult.success(Unit)
        } catch (e: Exception) {
            DomainResult.failure(
                DomainError.ExternalServiceError(e.message ?: "Failed to delete flag")
            )
        }
    }
}

@Serializable
private data class FeatureFlagDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("is_enabled")
    val isEnabled: Int,
    @SerialName("updated_at")
    val updatedAt: Long,
    @SerialName("user_id")
    val userId: String? = null,
)

private fun FeatureFlagDto.toDomain(): FeatureFlag = FeatureFlag(
    id = EntityId.of(id),
    key = name,
    description = description,
    defaultEnabled = isEnabled == 1,
    createdAt = Timestamp(Instant.fromEpochMilliseconds(updatedAt)),
    updatedAt = Timestamp(Instant.fromEpochMilliseconds(updatedAt)),
)
