package com.appfactory.infrastructure.sync

import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackendSyncJvmTransport(
    private val baseUrl: String = defaultBackendBaseUrl(),
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .build(),
) : BackendSyncTransport {

    override suspend fun trigger(scope: SyncScope): BackendSyncTriggerResponse = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/api/sync/trigger"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(BackendSyncHttpContract.triggerRequestBody(scope)))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        BackendSyncHttpContract.parseTriggerResponse(response.statusCode(), response.body())
    }

    override suspend fun state(scope: SyncScope): SyncState = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(scopeStateUrl(scope)))
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        BackendSyncHttpContract.parseStateResponse(response.statusCode(), response.body(), scope)
    }

    private fun scopeStateUrl(scope: SyncScope): String {
        if (scope == SyncScope.All) return "$baseUrl/api/sync/state"

        val encodedEntityType = URLEncoder.encode(scope.entityType, StandardCharsets.UTF_8)
        return "$baseUrl/api/sync/state?entityType=$encodedEntityType"
    }
}

private fun defaultBackendBaseUrl(): String =
    "http://localhost:8081"
