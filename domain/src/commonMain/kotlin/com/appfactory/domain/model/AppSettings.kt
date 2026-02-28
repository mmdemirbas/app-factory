package com.appfactory.domain.model

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult

data class AppSettings(
    val teamId: TeamId,
    val environment: AppEnvironment = AppEnvironment.PRODUCTION,
    val isAutoSyncEnabled: Boolean = true
) {
    fun validate(): DomainResult<AppSettings> {
        return DomainResult.success(this)
    }
}

enum class AppEnvironment {
    STAGING,
    PRODUCTION
}
