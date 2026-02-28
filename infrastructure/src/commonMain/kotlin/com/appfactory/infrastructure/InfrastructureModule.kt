package com.appfactory.infrastructure

import com.appfactory.domain.AppDatabase
import com.appfactory.domain.port.AppSettingsRepository
import com.appfactory.domain.port.AuthProvider
import com.appfactory.domain.port.ConnectorRegistry
import com.appfactory.domain.port.FeatureFlagRepository
import com.appfactory.domain.port.SyncEngine
import com.appfactory.infrastructure.auth.SupabaseAuthAdapter
import com.appfactory.infrastructure.connectors.SqlDelightConnectorRegistry
import com.appfactory.infrastructure.connectors.SupabaseConnectorRegistry
import com.appfactory.infrastructure.sync.SyncEngineMode
import com.appfactory.infrastructure.sync.createPlatformSyncEngine
import com.appfactory.infrastructure.storage.local.DatabaseDriverFactory
import com.appfactory.infrastructure.storage.local.SqlDelightAppSettingsRepository
import com.appfactory.infrastructure.storage.local.SqlDelightFeatureFlagRepository
import com.appfactory.infrastructure.storage.remote.SupabaseAppSettingsRepository
import com.appfactory.infrastructure.storage.remote.SupabaseFeatureFlagRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

object InfrastructureModule {
    const val QUALIFIER_LOCAL = "Local"
    const val QUALIFIER_REMOTE = "Remote"

    val module: Module = module {
        single<SupabaseClient> {
            val supabaseUrl = getProperty("supabase.url", "http://127.0.0.1:54321")
            val supabaseAnonKey = getProperty("supabase.anon.key", "placeholder-key")

            createSupabaseClient(supabaseUrl, supabaseAnonKey) {
                install(Auth)
                install(Postgrest)
                install(Realtime)
            }
        }

        single<AuthProvider> {
            SupabaseAuthAdapter(get())
        }

        single<SyncEngine> {
            createPlatformSyncEngine(SyncEngineMode.PowerSync)
        }

        single<AppDatabase> {
            val driverFactory = get<DatabaseDriverFactory>()
            AppDatabase(driverFactory.createDriver())
        }

        single<AppSettingsRepository>(named(QUALIFIER_LOCAL)) {
            SqlDelightAppSettingsRepository(get())
        }
        factory<AppSettingsRepository> {
            get(named(QUALIFIER_LOCAL))
        }

        single<FeatureFlagRepository>(named(QUALIFIER_LOCAL)) {
            SqlDelightFeatureFlagRepository(get())
        }
        factory<FeatureFlagRepository> {
            get(named(QUALIFIER_LOCAL))
        }

        single<ConnectorRegistry>(named(QUALIFIER_LOCAL)) {
            SqlDelightConnectorRegistry(
                db = get(),
                availableDescriptors = emptyList()
            )
        }
        factory<ConnectorRegistry> {
            get(named(QUALIFIER_LOCAL))
        }

        single<AppSettingsRepository>(named(QUALIFIER_REMOTE)) {
            SupabaseAppSettingsRepository(get())
        }

        single<FeatureFlagRepository>(named(QUALIFIER_REMOTE)) {
            SupabaseFeatureFlagRepository(get())
        }

        single<ConnectorRegistry>(named(QUALIFIER_REMOTE)) {
            SupabaseConnectorRegistry(
                supabaseClient = get(),
                availableConnectors = emptyList()
            )
        }
    }
}
