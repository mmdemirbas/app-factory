package com.appfactory.infrastructure.storage.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.appfactory.domain.AppDatabase
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.model.AppEnvironment
import com.appfactory.domain.model.AppSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SqlDelightAppSettingsRepositoryJvmTest {

    @Test
    fun getSettings_whenLocalDbIsEmpty_returnsDefaultSettings() = runBlocking {
        val repository = testRepository()

        val result = repository.getSettings()

        assertTrue(result is DomainResult.Success<AppSettings>)
        assertEquals(AppSettings(), (result as DomainResult.Success).value)
    }

    @Test
    fun observeSettings_whenLocalDbIsEmpty_emitsDefaultSettings() = runBlocking {
        val repository = testRepository()

        val observed = repository.observeSettings().first()

        assertEquals(AppSettings(), observed)
    }

    @Test
    fun updateSettings_thenGetSettings_returnsPersistedLocalValues() = runBlocking {
        val repository = testRepository()
        val updated = AppSettings(
            environment = AppEnvironment.STAGING,
            isAutoSyncEnabled = false,
        )

        val writeResult = repository.updateSettings(updated)
        assertTrue(writeResult is DomainResult.Success<Unit>)

        val getResult = repository.getSettings()
        assertTrue(getResult is DomainResult.Success<AppSettings>)
        assertEquals(updated, (getResult as DomainResult.Success).value)
    }
}

private fun testRepository(): SqlDelightAppSettingsRepository {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    AppDatabase.Schema.create(driver)
    val db = AppDatabase(driver)
    return SqlDelightAppSettingsRepository(db)
}
