package com.appfactory.domain.port

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp
import kotlinx.coroutines.flow.Flow

/**
 * Port: AuthProvider
 *
 * Handles user authentication. Does not know which auth backend is used.
 *
 * Implementations:
 *   - infrastructure/auth/SupabaseAuthAdapter
 *   - infrastructure/auth/FirebaseAuthAdapter
 *   - infrastructure/auth/MigratingAuthAdapter (dual-write, used during migration)
 *
 * See ADR-009, ADR-011.
 */
interface AuthProvider {
    suspend fun signIn(credentials: Credentials): DomainResult<AuthenticatedUser>
    suspend fun signOut(): DomainResult<Unit>
    fun observeAuthState(): Flow<AuthState>
    suspend fun currentUser(): AuthenticatedUser?
    suspend fun refreshSession(): DomainResult<Unit>
}

/**
 * Credentials passed to signIn. The type determines the auth flow.
 */
sealed class Credentials {
    data class EmailPassword(val email: String, val password: String) : Credentials()
    data class OAuthCode(val code: String, val provider: OAuthService) : Credentials()
}

/**
 * The internal user identity. Always uses internalUserId, never the
 * auth provider's subject ID directly. See ADR-011 (identity linking).
 */
data class AuthenticatedUser(
    val internalUserId: EntityId,
    val email: String?,
    val displayName: String?,
    val createdAt: Timestamp,
)

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: AuthenticatedUser) : AuthState()
    data object Loading : AuthState()
}

enum class OAuthService {
    GOOGLE,
    GITHUB,
    MICROSOFT,
}
