package com.appfactory.backend.featureflags

import com.appfactory.domain.port.FeatureFlagRepository
import com.appfactory.infrastructure.InfrastructureModule
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

fun Route.featureFlagRoutes() {
    val featureFlagRepo: FeatureFlagRepository by inject(named(InfrastructureModule.QUALIFIER_REMOTE))

    route("/api/feature-flags") {
        get {
            val flags = featureFlagRepo.getAll()
            call.respond(HttpStatusCode.OK, flags)
        }
    }
}
