package com.appfactory.application.auth

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.AuthProvider

class SignOutUseCase(
    private val authProvider: AuthProvider
) {
    suspend operator fun invoke(): DomainResult<Unit> {
        return authProvider.signOut()
    }
}
