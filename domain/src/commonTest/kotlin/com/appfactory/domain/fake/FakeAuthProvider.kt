package com.appfactory.domain.fake

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp
import com.appfactory.domain.port.AuthProvider
import com.appfactory.domain.port.AuthState
import com.appfactory.domain.port.AuthenticatedUser
import com.appfactory.domain.port.Credentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class FakeAuthProvider : AuthProvider {
    var authResultOverride: AuthenticatedUser? = null
    var authFailureOverride: DomainError? = null
    var refreshSessionResultOverride: DomainResult<Unit>? = null
    private val currentUserFlow = MutableStateFlow<AuthenticatedUser?>(null)
    
    val signInCallCount get() = _signInCallCount
    private var _signInCallCount = 0

    val signOutCallCount get() = _signOutCallCount
    private var _signOutCallCount = 0

    val refreshSessionCallCount get() = _refreshSessionCallCount
    private var _refreshSessionCallCount = 0

    override suspend fun signIn(credentials: Credentials): DomainResult<AuthenticatedUser> {
        _signInCallCount++
        
        authFailureOverride?.let { return DomainResult.Failure(it) }
        
        val email = when (credentials) {
            is Credentials.EmailPassword -> credentials.email
            is Credentials.OAuthCode -> "oauthuser@example.com"
        }

        val user = authResultOverride ?: AuthenticatedUser(
            internalUserId = EntityId("fake-id"),
            email = email,
            displayName = "Test User",
            createdAt = Timestamp(Clock.System.now())
        )
        currentUserFlow.value = user
        return DomainResult.Success(user)
    }

    override suspend fun signOut(): DomainResult<Unit> {
        _signOutCallCount++
        currentUserFlow.value = null
        return DomainResult.Success(Unit)
    }

    override fun observeAuthState(): Flow<AuthState> = MutableStateFlow(
        currentUserFlow.value?.let { AuthState.Authenticated(it) } ?: AuthState.Unauthenticated
    ).asStateFlow()

    override suspend fun currentUser(): AuthenticatedUser? = currentUserFlow.value
    
    override suspend fun refreshSession(): DomainResult<Unit> {
        _refreshSessionCallCount++
        return refreshSessionResultOverride ?: DomainResult.Success(Unit)
    }

    fun setCurrentUser(user: AuthenticatedUser?) {
        currentUserFlow.value = user
    }
}
