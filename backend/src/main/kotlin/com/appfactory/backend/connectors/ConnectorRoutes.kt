package com.appfactory.backend.connectors

import com.appfactory.domain.port.ConnectorDescriptor
import com.appfactory.domain.port.ConnectorRegistry
import com.appfactory.infrastructure.InfrastructureModule
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

fun Route.connectorRoutes() {
    val connectorRegistry: ConnectorRegistry by inject(named(InfrastructureModule.QUALIFIER_REMOTE))

    route("/api/connectors") {
        get {
            call.respond(
                HttpStatusCode.OK,
                connectorRegistry.available().map { it.toResponse() }
            )
        }
    }
}

@Serializable
private data class ConnectorDescriptorResponse(
    val id: String,
    val name: String,
    val description: String,
    val oauthService: String?,
    val capabilities: Set<String>,
)

private fun ConnectorDescriptor.toResponse(): ConnectorDescriptorResponse = ConnectorDescriptorResponse(
    id = id.value,
    name = name,
    description = description,
    oauthService = oauthService?.name,
    capabilities = capabilities.mapTo(linkedSetOf()) { it.name }
)
