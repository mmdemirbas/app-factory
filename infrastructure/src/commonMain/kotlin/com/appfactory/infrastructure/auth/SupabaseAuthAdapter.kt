package com.appfactory.infrastructure.auth

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp
import com.appfactory.domain.port.AuthProvider
import com.appfactory.domain.port.AuthState
import com.appfactory.domain.port.AuthenticatedUser
import com.appfactory.domain.port.Credentials
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SupabaseAuthAdapter(
    private val supabaseClient: SupabaseClient,
) : AuthProvider {
    override suspend fun signIn(credentials: Credentials): DomainResult<AuthenticatedUser> {
        return try {
            when (credentials) {
                is Credentials.EmailPassword -> {
                    supabaseClient.auth.signInWith(Email) {
                        email = credentials.email
                        password = credentials.password
                    }
                }

                is Credentials.OAuthCode -> {
                    return DomainResult.failure(
                        DomainError.ExternalServiceError(
                            "OAuth strictly handles via intent in Supabase KMP, unimplemented"
                        )
                    )
                }
            }

            val user = currentUser()
                ?: return DomainResult.failure(
                    DomainError.ExternalServiceError("User logged in but profile is null")
                )

            DomainResult.success(user)
        } catch (e: Exception) {
            DomainResult.failure(
                DomainError.ExternalServiceError(
                    e.message ?: "Failed to authenticate with Supabase"
                )
            )
        }
    }

    override suspend fun signOut(): DomainResult<Unit> {
        return try {
            supabaseClient.auth.signOut()
            DomainResult.success(Unit)
        } catch (e: Exception) {
            DomainResult.failure(
                DomainError.ExternalServiceError(
                    e.message ?: "Failed to log out of Supabase"
                )
            )
        }
    }

    override fun observeAuthState(): Flow<AuthState> {
        return supabaseClient.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    mapGoTrueUser(status.session.user)
                        ?.let { AuthState.Authenticated(it) }
                        ?: AuthState.Unauthenticated
                }

                is SessionStatus.Initializing -> AuthState.Loading
                is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
                is SessionStatus.RefreshFailure -> AuthState.Unauthenticated
            }
        }
    }

    override suspend fun currentUser(): AuthenticatedUser? {
        val current = supabaseClient.auth.currentUserOrNull() ?: return null
        return mapGoTrueUser(current)
    }

    override suspend fun refreshSession(): DomainResult<Unit> {
        return try {
            supabaseClient.auth.refreshCurrentSession()
            DomainResult.success(Unit)
        } catch (e: Exception) {
            DomainResult.failure(
                DomainError.ExternalServiceError(
                    e.message ?: "Failed to refresh Supabase session"
                )
            )
        }
    }

    private fun mapGoTrueUser(goTrueUser: UserInfo?): AuthenticatedUser? {
        goTrueUser ?: return null
        return AuthenticatedUser(
            internalUserId = EntityId.of("usr_${goTrueUser.id}"),
            email = goTrueUser.email,
            displayName = goTrueUser.userMetadata?.get("full_name")?.toString(),
            createdAt = Timestamp(goTrueUser.createdAt ?: Timestamp.now().instant),
        )
    }
}
