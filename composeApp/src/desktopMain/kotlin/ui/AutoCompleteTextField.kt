package ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AutoCompleteTextField(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onSelectedItemChange: (T?) -> Unit,
    displayText: (T) -> String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expand by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(selectedItem?.let(displayText) ?: "") }

    LaunchedEffect(selectedItem) {
        val newText = selectedItem?.let(displayText) ?: ""
        if (text != newText) {
            text = newText
        }
    }

    ExposedDropdownMenuBox(
        expanded = expand,
        onExpandedChange = { if (enabled) expand = !expand },
    ) {
        OutlinedTextField(
            value = text,
            label = { Text(label) },
            onValueChange = { newValue ->
                if (enabled) {
                    text = newValue
                    expand = true
                    // If the user clears the text, treat the item as unselected
                    if (newValue.isEmpty()) {
                        onSelectedItemChange(null)
                    }
                }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expand) },
            modifier = modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
            singleLine = true,
            enabled = enabled // ✅ Apply enabled state
        )

        if (enabled) {
            val filteredItems = remember(text, items) {
                items.filter { displayText(it).contains(text, ignoreCase = true) }
            }

            ExposedDropdownMenu(
                expanded = expand,
                onDismissRequest = { expand = false },
            ) {
                filteredItems.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(displayText(item)) },
                        onClick = {
                            text = displayText(item)
                            onSelectedItemChange(item)
                            expand = false
                        }
                    )
                }
            }
        }
    }
}
