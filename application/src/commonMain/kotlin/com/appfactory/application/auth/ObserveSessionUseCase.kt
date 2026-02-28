package com.appfactory.application.auth

import com.appfactory.domain.port.AuthProvider
import com.appfactory.domain.port.AuthState
import kotlinx.coroutines.flow.Flow

class ObserveSessionUseCase(
    private val authProvider: AuthProvider
) {
    operator fun invoke(): Flow<AuthState> {
        return authProvider.observeAuthState()
    }
}
