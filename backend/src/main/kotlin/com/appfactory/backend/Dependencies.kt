package com.appfactory.backend

import com.appfactory.infrastructure.InfrastructureModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.logger.Level
import org.koin.ktor.plugin.Koin

fun Application.configureDependencyInjection() {
    install(Koin) {
        printLogger(Level.INFO)
        modules(InfrastructureModule.module)
    }
}
