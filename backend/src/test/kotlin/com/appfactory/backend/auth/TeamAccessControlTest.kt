package com.appfactory.backend.auth

import com.appfactory.backend.routes.appSettingsRoutes
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.model.AppEnvironment
import com.appfactory.domain.model.AppSettings
import com.appfactory.domain.model.TeamId
import com.appfactory.domain.model.UserId
import com.appfactory.domain.port.AppSettingsRepository
import com.appfactory.domain.port.TeamRepository
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class TeamAccessControlTest {

    @Test
    fun missingBearerToken_returnsUnauthorized() = testApplication {
        val settingsRepo = RecordingAppSettingsRepository()
        val teamRepo = RecordingTeamRepository(isMember = true)
        application { testSettingsApp(settingsRepo, teamRepo) }

        val response = client.get("/api/settings") {
            header(TEAM_HEADER, "team_alpha")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun missingTeamHeader_returnsBadRequest() = testApplication {
        val settingsRepo = RecordingAppSettingsRepository()
        val teamRepo = RecordingTeamRepository(isMember = true)
        application { testSettingsApp(settingsRepo, teamRepo) }

        val response = client.get("/api/settings") {
            header(HttpHeaders.Authorization, "Bearer ${signedToken("user_123")}")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("X-Team-ID"))
    }

    @Test
    fun nonMemberTeam_returnsForbidden() = testApplication {
        val settingsRepo = RecordingAppSettingsRepository()
        val teamRepo = RecordingTeamRepository(isMember = false)
        application { testSettingsApp(settingsRepo, teamRepo) }

        val response = client.get("/api/settings") {
            header(HttpHeaders.Authorization, "Bearer ${signedToken("user_123")}")
            header(TEAM_HEADER, "team_alpha")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals(EntityId("team_alpha"), teamRepo.lastTeamId)
        assertEquals(EntityId.of("usr_user_123"), teamRepo.lastUserId)
    }

    @Test
    fun memberTeam_returnsSettingsForRequestedTeam() = testApplication {
        val settingsRepo = RecordingAppSettingsRepository()
        val teamRepo = RecordingTeamRepository(isMember = true)
        application { testSettingsApp(settingsRepo, teamRepo) }

        val response = client.get("/api/settings") {
            header(HttpHeaders.Authorization, "Bearer ${signedToken("user_123")}")
            header(TEAM_HEADER, "team_alpha")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(EntityId("team_alpha"), settingsRepo.lastRequestedTeamId)
        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("STAGING", json["environment"]?.jsonPrimitive?.content)
        assertEquals(true, json["isAutoSyncEnabled"]?.jsonPrimitive?.boolean)
    }
}

private fun Application.testSettingsApp(
    settingsRepo: AppSettingsRepository,
    teamRepository: TeamRepository,
) {
    configureJwtAuthentication()
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    routing {
        authenticate(SUPABASE_JWT_AUTH) {
            appSettingsRoutes(settingsRepo, teamRepository)
        }
    }
}

private fun signedToken(subject: String): String {
    val issuer = System.getenv("SUPABASE_URL")
        ?.takeIf { it.isNotBlank() }
        ?.trimEnd('/')
        ?.let { "$it/auth/v1" }

    val jwtBuilder = JWT.create()
        .withSubject(subject)

    if (!issuer.isNullOrBlank()) {
        jwtBuilder.withIssuer(issuer)
    }

    return jwtBuilder.sign(Algorithm.HMAC256("change-me-in-production"))
}

private class RecordingAppSettingsRepository : AppSettingsRepository {
    var lastRequestedTeamId: TeamId? = null

    override suspend fun getSettings(teamId: TeamId): DomainResult<AppSettings> {
        lastRequestedTeamId = teamId
        return DomainResult.success(
            AppSettings(
                teamId = teamId,
                environment = AppEnvironment.STAGING,
                isAutoSyncEnabled = true
            )
        )
    }

    override suspend fun updateSettings(teamId: TeamId, settings: AppSettings): DomainResult<Unit> {
        return DomainResult.success(Unit)
    }

    override fun observeSettings(teamId: TeamId): Flow<AppSettings> = emptyFlow()

    override suspend fun clearLocalDb(): DomainResult<Unit> = DomainResult.success(Unit)

    override suspend fun exportLocalDb(): DomainResult<ByteArray> = DomainResult.success(byteArrayOf())
}

private class RecordingTeamRepository(
    private val isMember: Boolean,
) : TeamRepository {
    var lastTeamId: TeamId? = null
    var lastUserId: UserId? = null

    override suspend fun isMember(teamId: TeamId, userId: UserId): DomainResult<Boolean> {
        lastTeamId = teamId
        lastUserId = userId
        return DomainResult.success(isMember)
    }
}
