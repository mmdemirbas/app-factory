package com.appfactory.infrastructure.connectors.googlesheets.model

import kotlinx.serialization.Serializable

/**
 * Stable, domain-agnostic representation of a Google Sheets row.
 * This is owned by the connector infrastructure, not the Google SDK,
 * and not the domain.
 */
@Serializable
data class GoogleSheetsRow(
    val spreadsheetId: String,
    val sheetName: String,
    val rowIndex: Int,
    val cells: Map<String, CellValue>
)

/**
 * Represents the generic value of a single cell in a spreadsheet.
 */
@Serializable
data class CellValue(
    val value: String
)
