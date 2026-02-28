package com.appfactory.infrastructure.connectors.googlesheets

import com.appfactory.domain.port.ConnectorConfig
import com.appfactory.infrastructure.connectors.googlesheets.model.GoogleSheetsRow
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the capabilities of the Google Sheets Connector.
 * Uses the stable [GoogleSheetsRow] type rather than SDK-specific native models.
 */
interface GoogleSheetsConnector {
    
    /**
     * Fetches rows from the configured spreadsheet/sheet.
     */
    suspend fun fetchRows(config: ConnectorConfig): List<GoogleSheetsRow>
    
    /**
     * Pushes domain updates back to the Google Sheet.
     */
    suspend fun pushRows(rows: List<GoogleSheetsRow>, config: ConnectorConfig): PushResult
    
    /**
     * Optional realtime observer, depending on polling or webhooks.
     */
    fun observeChanges(config: ConnectorConfig): Flow<RemoteChange<GoogleSheetsRow>>
}

data class PushResult(
    val success: Boolean,
    val rowsUpdated: Int,
    val error: String? = null
)

sealed class RemoteChange<T> {
    data class Upsert<T>(val data: T) : RemoteChange<T>()
    data class Delete<T>(val identifier: String) : RemoteChange<T>()
}
