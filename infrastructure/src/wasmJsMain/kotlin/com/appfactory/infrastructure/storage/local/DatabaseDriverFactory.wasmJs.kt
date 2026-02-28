package com.appfactory.infrastructure.storage.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

@JsFun("""() => new Worker(new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url))""")
external fun createWorker(): Worker

class WasmDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        return WebWorkerDriver(createWorker())
    }
}
