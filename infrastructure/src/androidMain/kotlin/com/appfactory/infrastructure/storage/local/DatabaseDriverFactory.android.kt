package com.appfactory.infrastructure.storage.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import com.appfactory.domain.AppDatabase

class AndroidDatabaseDriverFactory(
    private val context: Context
) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        // SQLDelight 2.0.2 android-driver has a BaseVariant incompatibility with AGP 9.0+
        // PowerSync will take over SQLite in Phase 5 via its own JNI logic.
        // For Phase 3, this is stubbed for Android compilation integrity.
        throw UnsupportedOperationException("Android native SQL driver is temporarily disabled during compilation due to AGP incompatiblity. Run JVM for local testing.")
    }
}
