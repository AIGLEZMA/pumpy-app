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
    speed: Float?,
    onSpeedChange: (Float?) -> Unit,
    type: Report.OperationType,
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
    modifier: Modifier = Modifier,
) {
    NumberTextField(
        value = depth,
        label = "Profondeur (m)",
        onValueChange = { onDepthChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    Spacer(modifier = spaceBetweenFields)
    NumberTextField(
        value = staticLevel,
        label = "Niveau statique (m)",
        onValueChange = { onStaticLevelChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    Spacer(modifier = spaceBetweenFields)
    NumberTextField(
        value = dynamicLevel,
        label = "Niveau dynamique (m)",
        onValueChange = { onDynamicLevelChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    Spacer(modifier = spaceBetweenFields)
    NumberTextField(
        value = pumpShimming,
        label = "Calage de pompe (m)",
        onValueChange = { onPumpShimmingChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    Spacer(modifier = spaceBetweenFields)

    var speedText by remember { mutableStateOf(speed?.toString() ?: "") }
    var isSpeedError by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = speedText,
        onValueChange = { newValue ->
            speedText = newValue
            val floatValue = newValue.toFloatOrNull()
            isSpeedError = newValue.isNotEmpty() && floatValue == null
            onSpeedChange(floatValue)
        },
        label = { Text("Débit (m3/h)") },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = isSpeedError,
        supportingText = {
            if (isSpeedError) {
                Text(
                    text = "Veuillez entrer un nombre décimal valide.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                // This reserves the space even when there's no error
                Text("")
            }
        },
        modifier = modifier.fillMaxWidth()
    )

    Spacer(modifier = spaceBetweenFields)

    if (type == Report.OperationType.ASSEMBLY) {
        OutlinedTextField(
            value = engine ?: "",
            onValueChange = {
                onEngineChange(it)
            },
            label = { Text("Moteur") },
            singleLine = true,
            supportingText = { Text("") },
            modifier = modifier.fillMaxWidth()
        )
        Spacer(modifier = spaceBetweenFields)
        OutlinedTextField(
            value = pump ?: "",
            onValueChange = {
                onPumpChange(it)
            },
            label = { Text("Pompe") },
            singleLine = true,
            supportingText = { Text("") },
            modifier = modifier.fillMaxWidth()
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
    }
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
    modifier: Modifier = Modifier,
) {
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
                    modifier = Modifier.weight(1f)
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
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = notes ?: "",
        onValueChange = { onNotesChange(it) },
        label = { Text("Travaux effectués & observations") },
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        supportingText = { Text("") },
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
    )
}