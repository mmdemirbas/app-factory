package com.appfactory.domain.model

import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp

/**
 * The core domain representation of a system user.
 * 
 * Note: [id] corresponds to the internal system user ID (`internal_user_id`),
 * NOT the external authentication provider's UID.
 */
data class User(
    val id: UserId,
    val email: String,
    val displayName: String?,
    val createdAt: Timestamp
)

/**
 * Typealias for clarity regarding User ID semantics.
 */
typealias UserId = EntityId
