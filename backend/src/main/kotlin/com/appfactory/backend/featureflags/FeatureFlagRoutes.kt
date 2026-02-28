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
            val flags = featureFlagRepo.getAll()
            call.respond(HttpStatusCode.OK, flags)
        }
    }
}
