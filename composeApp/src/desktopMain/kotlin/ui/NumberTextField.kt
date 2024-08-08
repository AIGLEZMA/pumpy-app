package ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun NumberTextField(
    value: Long?,
    label: String,
    onValueChange: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(value?.toString() ?: "") }
    var isError by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = text,
        onValueChange = { newValue ->
            text = newValue
            val longValue = newValue.toLongOrNull()
            isError = longValue == null && newValue.isNotEmpty()
            onValueChange(longValue)
        },
        label = { Text(label) },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = "Veuillez entrer un nombre valide.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        singleLine = true,

        modifier = modifier.fillMaxWidth()
    )
}