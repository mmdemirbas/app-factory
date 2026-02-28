package com.appfactory.backend.sync

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.SyncEngine
import com.appfactory.domain.port.SyncResult
import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SyncRoutesTest {

    @Test
    fun triggerRoute_success_returnsCountersAndCallsEngineWithScope() = testApplication {
        val fakeEngine = RecordingSyncEngine(
            syncResultToReturn = DomainResult.success(
                SyncResult(
                    scope = SyncScope("feature_flag"),
                    recordsSynced = 5,
                    conflictsResolved = 2,
                )
            )
        )
        application { testSyncApp(fakeEngine) }

        val response = client.post("/api/sync/trigger") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"entityType":"feature_flag"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("ok", json["status"]?.jsonPrimitive?.content)
        assertEquals("feature_flag", json["scope"]?.jsonPrimitive?.content)
        assertEquals(5, json["recordsSynced"]?.jsonPrimitive?.int)
        assertEquals(2, json["conflictsResolved"]?.jsonPrimitive?.int)
        assertEquals(1, fakeEngine.syncNowCallCount)
        assertEquals(SyncScope("feature_flag"), fakeEngine.lastSyncScope)
    }

    @Test
    fun triggerRoute_failure_returnsBadGatewayAndErrorPayload() = testApplication {
        val fakeEngine = RecordingSyncEngine(
            syncResultToReturn = DomainResult.failure(
                DomainError.ExternalServiceError("upstream failed")
            )
        )
        application { testSyncApp(fakeEngine) }

        val response = client.post("/api/sync/trigger") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"entityType":"configured_connector"}""")
        }

        assertEquals(HttpStatusCode.BadGateway, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("error"))
        assertTrue(body.contains("upstream failed"))
        assertEquals(1, fakeEngine.syncNowCallCount)
        assertEquals(SyncScope("configured_connector"), fakeEngine.lastSyncScope)
    }

    @Test
    fun stateRoute_defaultScope_returnsStateAndCallsObserveOnce() = testApplication {
        val fakeEngine = RecordingSyncEngine(stateToReturn = SyncState.Offline)
        application { testSyncApp(fakeEngine) }

        val response = client.get("/api/sync/state") {
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("Offline", json["state"]?.jsonPrimitive?.content)
        assertEquals(1, fakeEngine.observeCallCount)
        assertEquals(SyncScope.All, fakeEngine.lastObserveScope)
    }

    @Test
    fun stateRoute_queryScope_usesRequestedScope() = testApplication {
        val fakeEngine = RecordingSyncEngine(stateToReturn = SyncState.Syncing)
        application { testSyncApp(fakeEngine) }

        val response = client.get("/api/sync/state?entityType=app_settings") {
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("Syncing", json["state"]?.jsonPrimitive?.content)
        assertEquals(SyncScope("app_settings"), fakeEngine.lastObserveScope)
    }
}

private fun Application.testSyncApp(syncEngine: SyncEngine) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    routing {
        syncRoutes(syncEngine)
    }
}

private class RecordingSyncEngine(
    var syncResultToReturn: DomainResult<SyncResult> = DomainResult.success(
        SyncResult(SyncScope.All, 0, 0)
    ),
    var stateToReturn: SyncState = SyncState.Idle,
) : SyncEngine {
    var syncNowCallCount: Int = 0
        private set
    var observeCallCount: Int = 0
        private set
    var lastSyncScope: SyncScope? = null
        private set
    var lastObserveScope: SyncScope? = null
        private set

    override suspend fun syncNow(scope: SyncScope): DomainResult<SyncResult> {
        syncNowCallCount += 1
        lastSyncScope = scope
        return syncResultToReturn
    }

    override fun observeSyncState(scope: SyncScope): Flow<SyncState> {
        observeCallCount += 1
        lastObserveScope = scope
        return flowOf(stateToReturn)
    }
}
