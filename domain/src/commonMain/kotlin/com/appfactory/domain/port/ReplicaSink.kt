package com.appfactory.domain.port

import com.appfactory.domain.common.DomainResult

/**
 * Port: ReplicaSink
 *
 * Receives change events and writes them to a secondary store.
 * Used by the bidirectional replication service during the
 * Firebase → Supabase migration window.
 *
 * The replication service runs as a separate Docker container.
 * See infrastructure/replication/ and ADR-012.
 */
interface ReplicaSink {
    suspend fun onInsert(
        table: String,
        record: Map<String, Any?>,
    ): DomainResult<Unit>

    suspend fun onUpdate(
        table: String,
        record: Map<String, Any?>,
        old: Map<String, Any?>,
    ): DomainResult<Unit>

    suspend fun onDelete(
        table: String,
        id: String,
    ): DomainResult<Unit>
}
