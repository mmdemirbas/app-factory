package com.appfactory.infrastructure.storage.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.appfactory.domain.AppDatabase
import org.w3c.dom.Worker

class WasmDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        // According to SQLDelight documentation for WebWorker
        return WebWorkerDriver(
            Worker(
                js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)""")
            )
        )
    }
}
