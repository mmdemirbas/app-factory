package com.appfactory.application.auth

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.AuthProvider
import com.appfactory.domain.port.AuthenticatedUser
import com.appfactory.domain.port.Credentials

class SignInUseCase(
    private val authProvider: AuthProvider
) {
    suspend operator fun invoke(credentials: Credentials): DomainResult<AuthenticatedUser> {
        return authProvider.signIn(credentials)
    }
}
