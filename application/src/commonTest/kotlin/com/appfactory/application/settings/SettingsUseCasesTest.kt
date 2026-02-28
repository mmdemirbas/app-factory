package com.appfactory.application.settings

import com.appfactory.domain.fake.FakeAppSettingsRepository
import com.appfactory.domain.model.AppEnvironment
import com.appfactory.domain.model.AppSettings
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.first

class SettingsUseCasesTest : StringSpec({

    val settingsArb = arbitrary { 
        AppSettings(
            environment = Arb.enum<AppEnvironment>().bind(),
            isAutoSyncEnabled = Arb.boolean().bind()
        )
    }

    "Get and Update Settings modify the repository state" {
        val fakeRepo = FakeAppSettingsRepository()
        val getUseCase = GetSettingsUseCase(fakeRepo)
        val updateUseCase = UpdateSettingsUseCase(fakeRepo)

        checkAll(settingsArb) { settings ->
            updateUseCase(settings)
            
            val result = getUseCase()
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe settings
        }
    }

    "ObserveSettingsUseCase returns live updates" {
        val fakeRepo = FakeAppSettingsRepository()
        val observeUseCase = ObserveSettingsUseCase(fakeRepo)
        val updateUseCase = UpdateSettingsUseCase(fakeRepo)

        val newSettings = AppSettings(AppEnvironment.STAGING, false)
        updateUseCase(newSettings)
        
        observeUseCase().first() shouldBe newSettings
    }

    "ClearLocalDbUseCase dispatches to repository" {
        val fakeRepo = FakeAppSettingsRepository()
        val clearUseCase = ClearLocalDbUseCase(fakeRepo)

        clearUseCase()
        fakeRepo.clearDbCallCount shouldBe 1
    }

    "ExportLocalDbUseCase retrieves bytes from repository" {
        val fakeRepo = FakeAppSettingsRepository()
        val exportUseCase = ExportLocalDbUseCase(fakeRepo)

        val result = exportUseCase()
        result.isSuccess shouldBe true
        result.getOrNull()?.isNotEmpty() shouldBe true
        fakeRepo.exportDbCallCount shouldBe 1
    }
})
