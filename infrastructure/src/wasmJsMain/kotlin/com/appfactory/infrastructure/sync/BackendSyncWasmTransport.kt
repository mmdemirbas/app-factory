package com.appfactory.infrastructure.sync

import com.appfactory.domain.port.SyncScope
import com.appfactory.domain.port.SyncState
import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise

class BackendSyncWasmTransport(
    private val baseUrl: String = defaultBackendBaseUrl(),
) : BackendSyncTransport {

    override suspend fun trigger(scope: SyncScope): BackendSyncTriggerResponse {
        val payload = BackendSyncHttpContract.triggerRequestBody(scope)
        val responseText = fetchHttp("$baseUrl/api/sync/trigger", "POST", payload)
            .await<JsAny?>()
            ?.toString()
            .orEmpty()
        val (statusCode, body) = responseText.toHttpParts()
        return BackendSyncHttpContract.parseTriggerResponse(statusCode, body)
    }

    override suspend fun state(scope: SyncScope): SyncState {
        val responseText = fetchHttp(scopeStateUrl(scope), "GET", null)
            .await<JsAny?>()
            ?.toString()
            .orEmpty()
        val (statusCode, body) = responseText.toHttpParts()
        return BackendSyncHttpContract.parseStateResponse(statusCode, body, scope)
    }

    private fun scopeStateUrl(scope: SyncScope): String {
        if (scope == SyncScope.All) return "$baseUrl/api/sync/state"
        return "$baseUrl/api/sync/state?entityType=${encodeURIComponent(scope.entityType)}"
    }
}

private fun defaultBackendBaseUrl(): String = "http://localhost:8081"

private fun String.toHttpParts(): Pair<Int, String> {
    val separator = indexOf('\n')
    if (separator == -1) return 500 to this

    val statusCode = substring(0, separator).toIntOrNull() ?: 500
    val body = substring(separator + 1)
    return statusCode to body
}

@JsFun(
    """
    async (url, method, body) => {
      const init = { method };
      if (body !== null && body !== undefined) {
        init.headers = { "Content-Type": "application/json" };
        init.body = body;
      }
      const response = await fetch(url, init);
      const text = await response.text();
      return response.status + "\n" + text;
    }
    """
)
private external fun fetchHttp(url: String, method: String, body: String?): Promise<JsAny?>

@JsFun("(value) => encodeURIComponent(value)")
private external fun encodeURIComponent(value: String): String
