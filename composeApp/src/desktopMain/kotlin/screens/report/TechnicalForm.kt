package screens.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.Report
import screens.spaceBetweenFields
import ui.NumberTextField

@Composable
fun TechnicalForm(
    depth: Long?,
    onDepthChange: (Long?) -> Unit,
    staticLevel: Long?,
    onStaticLevelChange: (Long?) -> Unit,
    dynamicLevel: Long?,
    onDynamicLevelChange: (Long?) -> Unit,
    pumpShimming: Long?,
    onPumpShimmingChange: (Long?) -> Unit,
    secondPumpShimming: Long?,
    onSecondPumpShimmingChange: (Long?) -> Unit,
    speed: Float?,
    onSpeedChange: (Float?) -> Unit,
    current: Float?,
    onCurrentChange: (Float?) -> Unit,
    type: Report.OperationType,
    depthAfterCleaning: Long?,
    onDepthAfterCleaningChange: (Long?) -> Unit,
    engine: String?,
    onEngineChange: (String) -> Unit,
    pump: String?,
    onPumpChange: (String) -> Unit,
    elements: List<String>,
    onElementAdd: () -> Unit,
    onElementRemove: (Int) -> Unit,
    onElementChange: (Int, String) -> Unit,
    notes: String?,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focus = LocalFocusManager.current

    NumberTextField(
        value = depth,
        label = "Profondeur (m)",
        onValueChange = onDepthChange,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focus.moveFocus(FocusDirection.Next); true
                } else false
            }
    )
    Spacer(modifier = spaceBetweenFields)

    if (type == Report.OperationType.CLEANING) {
        NumberTextField(
            value = depthAfterCleaning,
            label = "Profondeur après nettoyage (m)",
            onValueChange = onDepthAfterCleaningChange,
            modifier = modifier
                .fillMaxWidth()
                .onPreviewKeyEvent { e ->
                    if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                        focus.moveFocus(FocusDirection.Next); true
                    } else false
                }
        )

        Spacer(modifier = spaceBetweenFields)
    }

    NumberTextField(
        value = staticLevel,
        label = "Niveau statique (m)",
        onValueChange = onStaticLevelChange,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focus.moveFocus(FocusDirection.Next); true
                } else false
            }
    )
    Spacer(modifier = spaceBetweenFields)

    NumberTextField(
        value = dynamicLevel,
        label = "Niveau dynamique (m)",
        onValueChange = onDynamicLevelChange,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focus.moveFocus(FocusDirection.Next); true
                } else false
            }
    )
    Spacer(modifier = spaceBetweenFields)

    NumberTextField(
        value = pumpShimming,
        label = "Calage de pompe (m)",
        onValueChange = onPumpShimmingChange,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focus.moveFocus(FocusDirection.Next); true
                } else false
            }
    )
    Spacer(modifier = spaceBetweenFields)

    NumberTextField(
        value = secondPumpShimming,
        label = "2ᵉ Calage de la pompe (m)",
        onValueChange = onSecondPumpShimmingChange,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focus.moveFocus(FocusDirection.Next); true
                } else false
            }
    )

    Spacer(modifier = spaceBetweenFields)

    // Débit (float) with comma/dot handling and validation
    var speedText by remember(speed) { mutableStateOf(speed?.toString() ?: "") }
    var isSpeedError by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = speedText,
        onValueChange = { newValue ->
            // allow digits, one comma or dot, and empty
            val cleaned = newValue
                .replace(',', '.')
                .filterIndexed { idx, c ->
                    c.isDigit() || c == '.' || (c == '-' && idx == 0)
                }
            speedText = cleaned
            val parsed = cleaned.toFloatOrNull()
            isSpeedError = cleaned.isNotEmpty() && parsed == null
            onSpeedChange(parsed)
        },
        label = { Text("Débit (m3/h)") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        isError = isSpeedError,
        supportingText = {
            if (isSpeedError) {
                Text("Veuillez entrer un nombre décimal valide.", color = MaterialTheme.colorScheme.error)
            } else {
                Text("") // keep space
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focus.moveFocus(FocusDirection.Next); true
                } else false
            }
    )

    Spacer(modifier = spaceBetweenFields)

    // Current (float) with comma/dot handling and validation
    var currentText by remember(current) { mutableStateOf(current?.toString() ?: "") }
    var isCurrentError by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = currentText,
        onValueChange = { newValue ->
            // allow digits, one comma or dot, and empty
            val cleaned = newValue
                .replace(',', '.')
                .filterIndexed { idx, c ->
                    c.isDigit() || c == '.' || (c == '-' && idx == 0)
                }
            currentText = cleaned
            val parsed = cleaned.toFloatOrNull()
            isCurrentError = cleaned.isNotEmpty() && parsed == null
            onCurrentChange(parsed)
        },
        label = { Text("Courrant (A)") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        isError = isCurrentError,
        supportingText = {
            if (isCurrentError) {
                Text("Veuillez entrer un nombre décimal valide.", color = MaterialTheme.colorScheme.error)
            } else {
                Text("") // keep space
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focus.moveFocus(FocusDirection.Next); true
                } else false
            }
    )

    Spacer(modifier = spaceBetweenFields)

    OutlinedTextField(
        value = engine ?: "",
        onValueChange = onEngineChange,
        label = { Text("Moteur") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        supportingText = { Text("") },
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focus.moveFocus(FocusDirection.Next); true
                } else false
            }
    )
    Spacer(modifier = spaceBetweenFields)

    OutlinedTextField(
        value = pump ?: "",
        onValueChange = onPumpChange,
        label = { Text("Pompe") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        supportingText = { Text("") },
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focus.moveFocus(FocusDirection.Next); true
                } else false
            }
    )
    Spacer(modifier = spaceBetweenFields)

    ElementsForm(
        elements = elements,
        onElementAdd = onElementAdd,
        onElementChange = onElementChange,
        onElementRemove = onElementRemove,
        modifier = modifier.fillMaxWidth()
    )
    Spacer(modifier = spaceBetweenFields)

    NotesTextBox(
        notes = notes,
        onNotesChange = onNotesChange
    )
}

@Composable
fun ElementsForm(
    elements: List<String>,
    onElementAdd: () -> Unit,
    onElementChange: (Int, String) -> Unit,
    onElementRemove: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focus = LocalFocusManager.current

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Élements", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onElementAdd) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un élement")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        elements.forEachIndexed { index, element ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = element,
                    onValueChange = { onElementChange(index, it) },
                    label = { Text("Élement #${index + 1}") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier
                        .weight(1f)
                        .onPreviewKeyEvent { e ->
                            if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                                focus.moveFocus(FocusDirection.Next); true
                            } else false
                        }
                )
                IconButton(onClick = { onElementRemove(index) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer l'élement")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun NotesTextBox(
    notes: String?,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = notes ?: "",
        onValueChange = onNotesChange,
        label = { Text("Travaux effectués & observations") },
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        supportingText = { Text("") },
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
    )
}
