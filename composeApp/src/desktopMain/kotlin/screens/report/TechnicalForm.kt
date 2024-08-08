package screens.report

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    isAssembly: Boolean,
    engine: String?,
    onEngineChange: (String) -> Unit,
    pump: String?,
    onPumpChange: (String) -> Unit,
    elements: String?,
    onElementsChange: (String) -> Unit,
    notes: String?,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Technique",
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier
    )
    Spacer(modifier = spaceBetweenFields)
    NumberTextField(
        value = depth,
        label = "Profondeur (m)",
        onValueChange = { onDepthChange(it) },
        modifier = modifier
    )
    Spacer(modifier = spaceBetweenFields)
    NumberTextField(
        value = staticLevel,
        label = "Niveau statique (m)",
        onValueChange = { onStaticLevelChange(it) },
        modifier = modifier
    )
    Spacer(modifier = spaceBetweenFields)
    NumberTextField(
        value = dynamicLevel,
        label = "Niveau dynamique (m)",
        onValueChange = { onDynamicLevelChange(it) },
        modifier = modifier
    )
    Spacer(modifier = spaceBetweenFields)
    NumberTextField(
        value = pumpShimming,
        label = "Calage de pompe (m)",
        onValueChange = { onPumpShimmingChange(it) },
        modifier = modifier
    )
    Spacer(modifier = spaceBetweenFields)

    var speedText by remember { mutableStateOf(speed?.toString() ?: "") }
    OutlinedTextField(
        value = speedText,
        onValueChange = { newValue ->
            speedText = newValue
            onSpeedChange(newValue.toFloatOrNull())
        },
        label = { Text("Débit (m3/h)") },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )

    if (isAssembly) {
        Spacer(modifier = spaceBetweenFields)
        OutlinedTextField(
            value = engine ?: "",
            onValueChange = {
                onEngineChange(it)
            },
            label = { Text("Moteur") },
            singleLine = true,
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
            modifier = modifier.fillMaxWidth()
        )
        Spacer(modifier = spaceBetweenFields)
        OutlinedTextField(
            value = elements ?: "",
            onValueChange = {
                onElementsChange(it)
            },
            label = { Text("Élements") },
            singleLine = true,
            modifier = modifier.fillMaxWidth()
        )
    }
    Spacer(modifier = spaceBetweenFields)
    LargeTextBox(
        notes = notes,
        onNotesChange = onNotesChange
    )
}

@Composable
fun LargeTextBox(
    notes: String?,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = notes ?: "",
        onValueChange = { onNotesChange(it) },
        label = { Text("Travaux effectués & observations") },
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
    )
}