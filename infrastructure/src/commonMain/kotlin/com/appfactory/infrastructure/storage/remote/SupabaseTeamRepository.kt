package com.appfactory.infrastructure.storage.remote

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.model.TeamId
import com.appfactory.domain.model.UserId
import com.appfactory.domain.port.TeamRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseTeamRepository(
    private val supabaseClient: SupabaseClient,
) : TeamRepository {
    override suspend fun isMember(teamId: TeamId, userId: UserId): DomainResult<Boolean> {
        return try {
            val hasMembership = candidateUserIds(userId).any { candidateUserId ->
                val result = supabaseClient.from("team_membership").select {
                    filter {
                        eq("team_id", teamId.value)
                        eq("user_id", candidateUserId)
                    }
                }
                result.decodeList<TeamMembershipDto>().isNotEmpty()
            }

            DomainResult.success(hasMembership)
        } catch (e: Exception) {
            DomainResult.failure(
                DomainError.ExternalServiceError(
                    e.message ?: "Failed to validate team membership from Supabase"
                )
            )
        }
    }
}

private fun candidateUserIds(userId: UserId): List<String> {
    val externalSubject = userId.value.removePrefix("usr_")
    return if (externalSubject == userId.value) {
        listOf(userId.value)
    } else {
        listOf(userId.value, externalSubject)
    }
}

@Serializable
private data class TeamMembershipDto(
    @SerialName("team_id")
    val teamId: String,
    @SerialName("user_id")
    val userId: String,
)
