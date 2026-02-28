package com.appfactory.backend.featureflags

import com.appfactory.domain.port.FeatureFlagRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.featureFlagRoutes(featureFlagRepo: FeatureFlagRepository) {
    route("/api/feature-flags") {
        get {
            val teamId = call.request.headers["X-Team-ID"]?.let { com.appfactory.domain.common.EntityId(it) } ?: com.appfactory.domain.common.EntityId("default_team")
            val flags = featureFlagRepo.getAll(teamId)
            call.respond(HttpStatusCode.OK, flags)
        }
    }
}
