package com.appfactory.backend

import com.appfactory.backend.auth.authRoutes
import com.appfactory.backend.auth.configureJwtAuthentication
import com.appfactory.backend.auth.SUPABASE_JWT_AUTH
import com.appfactory.backend.connectors.connectorRoutes
import com.appfactory.backend.featureflags.featureFlagRoutes
import com.appfactory.backend.sync.syncRoutes
import com.appfactory.domain.port.AppSettingsRepository
import com.appfactory.domain.port.AuthProvider
import com.appfactory.domain.port.ConnectorRegistry
import com.appfactory.domain.port.FeatureFlagRepository
import com.appfactory.domain.port.SyncEngine
import com.appfactory.domain.port.TeamRepository
import com.appfactory.infrastructure.InfrastructureModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import com.appfactory.backend.health.healthRoutes
import com.appfactory.backend.routes.appSettingsRoutes
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import io.ktor.server.auth.authenticate
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.ktor.ext.getKoin

fun main() {
    val port = System.getenv("BACKEND_PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDependencyInjection()
    configureJwtAuthentication()
    val koin = getKoin()

    install(CallLogging)
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("X-Team-ID")
        allowNonSimpleContentTypes = true
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    routing {
        val authProvider = koin.get<AuthProvider>()
        val settingsRepo = koin.get<AppSettingsRepository>(named(InfrastructureModule.QUALIFIER_REMOTE))
        val featureFlagRepo = koin.get<FeatureFlagRepository>(named(InfrastructureModule.QUALIFIER_REMOTE))
        val connectorRegistry = koin.get<ConnectorRegistry>(named(InfrastructureModule.QUALIFIER_REMOTE))
        val syncEngine = koin.get<SyncEngine>()
        val teamRepository = koin.get<TeamRepository>()

        healthRoutes()
        authRoutes(authProvider)
        authenticate(SUPABASE_JWT_AUTH) {
            appSettingsRoutes(settingsRepo, teamRepository)
            featureFlagRoutes(featureFlagRepo, teamRepository)
            connectorRoutes(connectorRegistry)
            syncRoutes(syncEngine)
        }
    }
}
