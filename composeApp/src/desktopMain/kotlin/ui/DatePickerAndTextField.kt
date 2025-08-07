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
import java.time.format.ResolverStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerAndTextField(
    value: LocalDate?,
    label: String,
    onValueChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    var textValue by remember {
        mutableStateOf(value?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "")
    }

    LaunchedEffect(value) {
        val newTextValue = value?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""
        if (textValue != newTextValue) {
            textValue = newTextValue
        }
    }

    var isError by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withResolverStyle(ResolverStyle.STRICT)
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = value?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            if (newValue.all { it.isDigit() || it == '/' } || newValue.isEmpty()) {
                textValue = newValue

                if (newValue.length == 10) {
                    try {
                        val parsedDate = LocalDate.parse(newValue, dateFormatter)
                        onValueChange(parsedDate)
                        isError = false
                    } catch (e: Exception) {
                        isError = true
                    }
                } else {
                    isError = false
                }
            } else {
                isError = true
            }

            if (newValue.isEmpty()) {
                onValueChange(null)
            }
        },
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = "Veuillez entrer une date valide (ex: 07/08/2025)",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
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
