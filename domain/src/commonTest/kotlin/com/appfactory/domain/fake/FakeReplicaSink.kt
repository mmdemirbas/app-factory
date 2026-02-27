package com.appfactory.domain.fake

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.ReplicaSink

class FakeReplicaSink : ReplicaSink {
    val inserts = mutableListOf<InsertRecord>()
    val updates = mutableListOf<UpdateRecord>()
    val deletes = mutableListOf<DeleteRecord>()

    override suspend fun onInsert(table: String, record: Map<String, Any?>): DomainResult<Unit> {
        inserts.add(InsertRecord(table, record))
        return DomainResult.Success(Unit)
    }

    override suspend fun onUpdate(table: String, record: Map<String, Any?>, old: Map<String, Any?>): DomainResult<Unit> {
        updates.add(UpdateRecord(table, record, old))
        return DomainResult.Success(Unit)
    }

    override suspend fun onDelete(table: String, id: String): DomainResult<Unit> {
        deletes.add(DeleteRecord(table, id))
        return DomainResult.Success(Unit)
    }

    data class InsertRecord(val table: String, val record: Map<String, Any?>)
    data class UpdateRecord(val table: String, val record: Map<String, Any?>, val old: Map<String, Any?>)
    data class DeleteRecord(val table: String, val id: String)
}
