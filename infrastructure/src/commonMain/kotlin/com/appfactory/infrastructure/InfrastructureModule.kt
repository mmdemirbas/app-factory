package com.appfactory.infrastructure

import com.appfactory.domain.AppDatabase
import com.appfactory.domain.port.AppSettingsRepository
import com.appfactory.domain.port.ConnectorRegistry
import com.appfactory.domain.port.FeatureFlagRepository
import com.appfactory.infrastructure.connectors.SqlDelightConnectorRegistry
import com.appfactory.infrastructure.storage.local.DatabaseDriverFactory
import com.appfactory.infrastructure.storage.local.SqlDelightAppSettingsRepository
import com.appfactory.infrastructure.storage.local.SqlDelightFeatureFlagRepository
import org.koin.core.module.Module
import org.koin.dsl.module

object InfrastructureModule {
    val module: Module = module {
        // Core Database
        single<AppDatabase> {
            val driverFactory = get<DatabaseDriverFactory>()
            AppDatabase(driverFactory.createDriver())
        }

        // Repository Adapters
        single<AppSettingsRepository> {
            SqlDelightAppSettingsRepository(get())
        }

        single<FeatureFlagRepository> {
            SqlDelightFeatureFlagRepository(get())
        }

        single<ConnectorRegistry> {
            SqlDelightConnectorRegistry(
                db = get(),
                availableDescriptors = emptyList() // Will be loaded dynamically in later phases
            )
        }
    }
}
