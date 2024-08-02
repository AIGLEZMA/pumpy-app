package ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerAndTextField(
    value: LocalDate?,
    label: String,
    onValueChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var textValue by remember(value) {
        mutableStateOf(
            value?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""
        )
    }
    val datePickerState = rememberDatePickerState()
    OutlinedTextField(
        value = textValue,
        onValueChange = {
            textValue = it
            try {
                val parsedDate = LocalDate.parse(it, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                onValueChange(parsedDate)
            } catch (e: Exception) {
                onValueChange(null) // Invalid date format
            }
        },
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Select date")
            }
        },
        modifier = modifier.fillMaxWidth()
    )
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onValueChange(selectedDate)
                            textValue = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        }
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text(text = "Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = "Rejeter")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}