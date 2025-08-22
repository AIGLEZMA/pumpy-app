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
import androidx.compose.ui.unit.dp
import models.Client
import models.Report
import screens.spaceBetweenFields
import ui.AutoCompleteTextField
import ui.DatePickerAndTextField
import ui.NumberTextField
import java.time.LocalDate

@Composable
fun GeneralForm(
    executionOrder: Long?,
    onExecutionOrderChange: (Long?) -> Unit,
    requestDate: LocalDate?,
    onRequestDateChange: (LocalDate?) -> Unit,
    workStartDate: LocalDate?,
    onWorkStartDateChange: (LocalDate?) -> Unit,
    workFinishDate: LocalDate?,
    onWorkFinishDateChange: (LocalDate?) -> Unit,
    clients: List<Client>,
    selectedClient: Client?,
    onSelectedClientChange: (Client?) -> Unit,
    asker: String?,
    onAskerChange: (String?) -> Unit,
    wellDrilling: String?,
    onWellDrillingChange: (String?) -> Unit,
    operators: List<String>,
    onOperatorAdd: () -> Unit,
    onOperatorRemove: (Int) -> Unit,
    onOperatorChange: (Int, String) -> Unit,
    selectedType: Report.OperationType?,
    onTypeSelected: (Report.OperationType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focus = LocalFocusManager.current

    NumberTextField(
        value = executionOrder,
        label = "Bon d'exécution",
        onValueChange = onExecutionOrderChange,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focus.moveFocus(FocusDirection.Next); true
                } else false
            }
    )

    DatePickerAndTextField(
        value = requestDate,
        label = "Date de demande",
        onValueChange = onRequestDateChange,
        modifier = modifier.fillMaxWidth()
    )
    DatePickerAndTextField(
        value = workStartDate,
        label = "Date de début des travaux",
        onValueChange = onWorkStartDateChange,
        modifier = modifier.fillMaxWidth()
    )
    DatePickerAndTextField(
        value = workFinishDate,
        label = "Date de fin des travaux",
        onValueChange = onWorkFinishDateChange,
        modifier = modifier.fillMaxWidth()
    )

    AutoCompleteTextField(
        label = "Client",
        items = clients,
        selectedItem = selectedClient,
        onSelectedItemChange = onSelectedClientChange,
        displayText = { it.name },
        enabled = clients.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = spaceBetweenFields)

    OutlinedTextField(
        value = asker ?: "",
        onValueChange = onAskerChange,
        label = { Text("Demandeur") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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
        value = wellDrilling ?: "",
        onValueChange = onWellDrillingChange,
        label = { Text("Forage") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focus.moveFocus(FocusDirection.Next); true
                } else false
            }
    )

    Spacer(modifier = spaceBetweenFields)

    OperatorForm(
        operators = operators,
        onOperatorAdd = onOperatorAdd,
        onOperatorChange = onOperatorChange,
        onOperatorRemove = onOperatorRemove,
        modifier = modifier.fillMaxWidth()
    )

    Spacer(modifier = spaceBetweenFields)

    OperationPicker(
        selectedType = selectedType,
        onTypeSelected = onTypeSelected,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun OperatorForm(
    operators: List<String>,
    onOperatorAdd: () -> Unit,
    onOperatorChange: (Int, String) -> Unit,
    onOperatorRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focus = LocalFocusManager.current

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Intervenants", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onOperatorAdd) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un intervenant")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        operators.forEachIndexed { index, operator ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = operator,
                    onValueChange = { onOperatorChange(index, it) },
                    label = { Text("Intervenant #${index + 1}") },
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
                IconButton(onClick = { onOperatorRemove(index) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer l'intervenant")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationPicker(
    selectedType: Report.OperationType?,
    onTypeSelected: (Report.OperationType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label: (Report.OperationType) -> String = {
        if (it == Report.OperationType.BOTH) "Montage + Démontage" else it.beautiful
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedType?.let(label) ?: "",
            onValueChange = {},
            label = { Text("Opération") },
            placeholder = { Text("Choisir...") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Report.OperationType.entries.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(label(opt), maxLines = 1) },
                    onClick = { onTypeSelected(opt); expanded = false }
                )
            }
        }
    }
}
