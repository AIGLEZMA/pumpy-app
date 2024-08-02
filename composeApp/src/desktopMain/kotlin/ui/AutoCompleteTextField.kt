package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

@Composable
fun <T> AutoCompleteTextField(
    label: String,
    value: String?,
    source: List<T>,
    onSelect: (T) -> Unit,
    displayText: (T) -> String,
    modifier: Modifier = Modifier
) {
    var category by remember { mutableStateOf(value ?: "") }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier.fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null, onClick = {
                expanded = false
            })
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth()
                        .onGloballyPositioned { coordinates -> textFieldSize = coordinates.size.toSize() },
                    value = category,
                    onValueChange = {
                        category = it
                        expanded = true
                    },
                    placeholder = { Text(label) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Open menu"
                            )
                        }
                    })
            }
            AnimatedVisibility(visible = expanded) {
                Card(
                    modifier = Modifier.width(textFieldSize.width.dp) // Modifier.padding(horizontal = 5.dp).width(textFieldSize.width.dp)
                ) {
                    // TODO: add scroll in case there are too many entries
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 150.dp),
                    ) {
                        if (category.isNotEmpty()) {
                            items(source.filter {
                                displayText(it).lowercase()
                                    .contains(category.lowercase()) || displayText(it).lowercase().contains("others")
                            }.sortedBy(displayText)) {
                                ItemsCategory(
                                    title = displayText(it)
                                ) { title ->
                                    category = displayText(it)
                                    expanded = false
                                    onSelect(it)
                                }
                            }
                        } else {
                            items(
                                source.sortedBy(displayText)
                            ) {
                                ItemsCategory(title = displayText(it)) { title ->
                                    category = displayText(it)
                                    expanded = false
                                    onSelect(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ItemsCategory(
    title: String, onSelect: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().clickable {
        onSelect(title)
    }.padding(10.dp)) {
        Text(text = title)
    }
}