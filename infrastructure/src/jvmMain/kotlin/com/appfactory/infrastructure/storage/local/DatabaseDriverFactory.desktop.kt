package com.appfactory.infrastructure.storage.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.appfactory.domain.AppDatabase
import java.io.File

class DesktopDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".appfactory/appfactory.db")
        databasePath.parentFile.mkdirs()
        
        val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")
        
        // We ensure tables are created on JVM startup if it's the first run
        if (!databasePath.exists() || databasePath.length() == 0L) {
            AppDatabase.Schema.create(driver)
        }
        
        return driver
    }
}
