package com.appfactory.domain.model

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp
import com.appfactory.domain.model.TeamId
import kotlinx.serialization.Serializable

/**
 * A feature flag controls whether a capability is active.
 *
 * Flags can be toggled per environment and overridden per user.
 * In release builds, flags should be compile-time constants (no runtime overhead).
 *
 * This is part of the meta/admin sample domain.
 * Replace with your own domain models when building a real app.
 */
@Serializable
data class FeatureFlag(
    val id: EntityId,
    val teamId: TeamId,
    val key: String,
    val description: String,
    val defaultEnabled: Boolean,
    val environmentOverrides: Map<Environment, Boolean> = emptyMap(),
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
) {
    fun isEnabledFor(environment: Environment): Boolean =
        environmentOverrides[environment] ?: defaultEnabled

    companion object {
        fun create(
            teamId: TeamId,
            key: String,
            description: String,
            defaultEnabled: Boolean = false,
        ): DomainResult<FeatureFlag> {
            if (key.isBlank()) {
                return DomainResult.failure(
                    DomainError.ValidationFailed("Feature flag key cannot be blank")
                )
            }
            if (!key.matches(Regex("[a-z_][a-z0-9_]*"))) {
                return DomainResult.failure(
                    DomainError.ValidationFailed(
                        "Feature flag key must be lowercase snake_case: $key"
                    )
                )
            }
            val now = Timestamp.now()
            return DomainResult.success(
                FeatureFlag(
                    id = EntityId.generate(),
                    teamId = teamId,
                    key = key,
                    description = description,
                    defaultEnabled = defaultEnabled,
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}

enum class Environment { DEV, STAGING, PROD }
