package com.appfactory.domain.common

import com.benasher44.uuid.uuid4
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

/**
 * Typed, opaque entity identifier.
 *
 * All entity IDs in the system are EntityId instances.
 * Never use raw String IDs in domain code.
 *
 * NOTE: This class is in domain.common so it can be extracted
 * as a standalone library without carrying domain business logic.
 */
@Serializable
@JvmInline
value class EntityId(val value: String) {
    init {
        require(value.isNotBlank()) { "EntityId cannot be blank" }
    }

    override fun toString(): String = value

    companion object {
        fun generate(): EntityId = EntityId(uuid4().toString())
        fun of(value: String): EntityId = EntityId(value)
    }
}
