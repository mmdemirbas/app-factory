package com.appfactory.domain.common

import kotlin.jvm.JvmInline
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Domain timestamp. Wraps kotlinx.datetime.Instant.
 *
 * Using a wrapper keeps the domain free from direct kotlinx.datetime usage
 * in call sites, making future changes to the time library contained.
 */
@Serializable
@JvmInline
value class Timestamp(val instant: Instant) {
    override fun toString(): String = instant.toString()

    companion object {
        fun now(): Timestamp = Timestamp(Clock.System.now())
        fun of(instant: Instant): Timestamp = Timestamp(instant)
    }
}

operator fun Timestamp.compareTo(other: Timestamp): Int =
    this.instant.compareTo(other.instant)
