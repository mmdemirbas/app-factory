package com.appfactory.ui.features.connectors.googlesheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Screen outlining the Field Mapping UI for the Google Sheets connector.
 */
@Composable
fun GoogleSheetsMappingScreen(
    onSaveMapping: (spreadsheetId: String, range: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var spreadsheetId by remember { mutableStateOf("") }
    var sheetRange by remember { mutableStateOf("Sheet1") }

    Box(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Google Sheets Connector",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Map your spreadsheet to application fields",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = spreadsheetId,
                onValueChange = { spreadsheetId = it },
                label = { Text("Spreadsheet ID") },
                singleLine = true
            )

            OutlinedTextField(
                value = sheetRange,
                onValueChange = { sheetRange = it },
                label = { Text("Sheet Range / Name") },
                singleLine = true
            )

            Button(
                onClick = { onSaveMapping(spreadsheetId, sheetRange) },
                enabled = spreadsheetId.isNotBlank() && sheetRange.isNotBlank()
            ) {
                Text("Save Configuration")
            }
        }
    }
}
