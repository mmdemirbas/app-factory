package com.appfactory.backend.auth

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.model.TeamId
import com.appfactory.domain.model.UserId
import com.appfactory.domain.port.TeamRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

const val TEAM_HEADER = "X-Team-ID"

data class AuthorizedTeamContext(
    val userId: UserId,
    val teamId: TeamId,
)

suspend fun ApplicationCall.requireAuthorizedTeam(teamRepository: TeamRepository): AuthorizedTeamContext? {
    val principal = principal<JWTPrincipal>()
    val subject = principal?.payload?.subject
    if (subject.isNullOrBlank()) {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
        return null
    }

    val teamHeader = request.headers[TEAM_HEADER]?.trim()
    if (teamHeader.isNullOrEmpty()) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing X-Team-ID header"))
        return null
    }

    val teamId = EntityId(teamHeader)
    val userId = subject.toUserId()

    return when (val membershipResult = teamRepository.isMember(teamId, userId)) {
        is DomainResult.Success -> {
            if (!membershipResult.value) {
                respond(HttpStatusCode.Forbidden, mapOf("error" to "Forbidden for requested team"))
                null
            } else {
                AuthorizedTeamContext(userId = userId, teamId = teamId)
            }
        }

        is DomainResult.Failure -> {
            respond(HttpStatusCode.InternalServerError, mapOf("error" to membershipResult.error.message))
            null
        }
    }
}

private fun String.toUserId(): UserId {
    val prefixed = if (startsWith("usr_")) this else "usr_$this"
    return EntityId.of(prefixed)
}
