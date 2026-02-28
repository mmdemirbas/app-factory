package com.appfactory.infrastructure.storage.remote

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.model.AppEnvironment
import com.appfactory.domain.model.AppSettings
import com.appfactory.domain.port.AppSettingsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseAppSettingsRepository(
    private val supabaseClient: SupabaseClient,
) : AppSettingsRepository {
    private val settingsId = "singleton_settings"

    override suspend fun getSettings(): DomainResult<AppSettings> {
        return try {
            val result = supabaseClient.from("app_settings").select()
            val dto = result.decodeList<AppSettingsDto>().firstOrNull()

            if (dto != null) {
                val environment =
                    AppEnvironment.entries.firstOrNull { it.name == dto.environment }
                        ?: AppEnvironment.PRODUCTION

                DomainResult.success(
                    AppSettings(
                        environment = environment,
                        isAutoSyncEnabled = dto.isAutoSyncEnabled == 1,
                    )
                )
            } else {
                DomainResult.success(AppSettings())
            }
        } catch (e: Exception) {
            DomainResult.failure(
                DomainError.ExternalServiceError(
                    e.message ?: "Failed to fetch settings from Supabase"
                )
            )
        }
    }

    override suspend fun updateSettings(settings: AppSettings): DomainResult<Unit> {
        return try {
            val payload = AppSettingsDto(
                id = settingsId,
                environment = settings.environment.name,
                isAutoSyncEnabled = if (settings.isAutoSyncEnabled) 1 else 0,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            )

            supabaseClient.from("app_settings").upsert(payload) {
                onConflict = "id"
            }

            DomainResult.success(Unit)
        } catch (e: Exception) {
            DomainResult.failure(
                DomainError.ExternalServiceError(
                    e.message ?: "Failed to update settings in Supabase"
                )
            )
        }
    }

    override fun observeSettings(): Flow<AppSettings> = emptyFlow()

    override suspend fun clearLocalDb(): DomainResult<Unit> = DomainResult.failure(
        DomainError.Unknown("clearLocalDb is not supported on Remote repository")
    )

    override suspend fun exportLocalDb(): DomainResult<ByteArray> = DomainResult.failure(
        DomainError.Unknown("exportLocalDb is not supported on Remote repository")
    )
}

@Serializable
private data class AppSettingsDto(
    @SerialName("id")
    val id: String,
    @SerialName("environment")
    val environment: String,
    @SerialName("is_auto_sync_enabled")
    val isAutoSyncEnabled: Int,
    @SerialName("updated_at")
    val updatedAt: Long,
    @SerialName("user_id")
    val userId: String? = null,
)
