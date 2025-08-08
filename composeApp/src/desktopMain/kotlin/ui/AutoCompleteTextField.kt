package ui

import androidx.compose.foundation.layout.fillMaxWidth
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
    modifier: Modifier = Modifier,
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
        onExpandedChange = { expand = !expand },
    ) {
        OutlinedTextField(
            value = text,
            label = { Text(label) },
            onValueChange = { newValue ->
                text = newValue
                expand = true
                // If the user clears the text, treat the item as unselected
                if (newValue.isEmpty()) {
                    onSelectedItemChange(null)
                }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expand) },
            modifier = modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
            singleLine = true
        )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FlexibleAutoCompleteTextField(
    label: String,
    value: String?,
    source: List<T>,
    onSelect: (T) -> Unit,
    onValueChange: (String) -> Unit,
    displayText: (T) -> String,
    modifier: Modifier = Modifier
) {
    var expand by remember { mutableStateOf(false) }
    var text by remember(value) { mutableStateOf(value ?: "") }

    ExposedDropdownMenuBox(
        expanded = expand,
        onExpandedChange = { expand = !expand },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = text,
            label = { Text(label) },
            onValueChange = { newValue ->
                text = newValue
                expand = true
                onValueChange(newValue)
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expand) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
            singleLine = true
        )

        val filteredSource = remember(text, source) {
            if (text.isNotEmpty()) {
                source.filter { displayText(it).contains(text, ignoreCase = true) }
            } else {
                source
            }
        }

        ExposedDropdownMenu(
            expanded = expand,
            onDismissRequest = { expand = false },
        ) {
            filteredSource.forEach { item ->
                DropdownMenuItem(
                    text = { Text(displayText(item)) },
                    onClick = {
                        onSelect(item) // Trigger the onSelect lambda with the chosen item
                        text = displayText(item)
                        expand = false
                    }
                )
            }
        }
    }
}