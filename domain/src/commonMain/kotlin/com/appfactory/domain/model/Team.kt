package com.appfactory.domain.model

import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp

/**
 * A collaborative space where resources (Connectors, Feature Flags, etc.) belong.
 * Replaces singular global scope for resources in the meta-app.
 */
data class Team(
    val id: TeamId,
    val name: String,
    val createdAt: Timestamp,
    val ownerId: UserId
)

/**
 * Represents a user's role and membership within a specific team.
 */
data class TeamMembership(
    val teamId: TeamId,
    val userId: UserId,
    val role: TeamRole,
    val joinedAt: Timestamp
)

enum class TeamRole {
    OWNER,
    ADMIN,
    MEMBER
}

/**
 * Typealias for clarity regarding Team ID semantics.
 */
typealias TeamId = EntityId
