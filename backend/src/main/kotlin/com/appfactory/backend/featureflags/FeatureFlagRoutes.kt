package com.appfactory.backend.featureflags

import com.appfactory.backend.auth.requireAuthorizedTeam
import com.appfactory.domain.port.FeatureFlagRepository
import com.appfactory.domain.port.TeamRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.featureFlagRoutes(
    featureFlagRepo: FeatureFlagRepository,
    teamRepository: TeamRepository,
) {
    route("/api/feature-flags") {
        get {
            val accessContext = call.requireAuthorizedTeam(teamRepository) ?: return@get
            val teamId = accessContext.teamId
            val flags = featureFlagRepo.getAll(teamId)
            call.respond(HttpStatusCode.OK, flags)
        }
    }
}
