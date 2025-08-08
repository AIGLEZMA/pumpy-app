package screens.report

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.Client
import models.Report
import screens.spaceBetweenFields
import ui.AutoCompleteTextField
import ui.DatePickerAndTextField
import ui.FlexibleAutoCompleteTextField
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
    fetchFarmNames: (Client) -> List<String>,
    selectedFarmName: String?,
    onSelectedFarmNameChange: (String?) -> Unit,
    fetchPumpNames: (String) -> List<String>,
    selectedPumpName: String?,
    onSelectedPumpNameChange: (String?) -> Unit,
    operators: List<String>,
    onOperatorAdd: () -> Unit,
    onOperatorRemove: (Int) -> Unit,
    onOperatorChange: (Int, String) -> Unit,
    selectedType: Report.OperationType?,
    onTypeSelected: (Report.OperationType) -> Unit,
    modifier: Modifier = Modifier,
) {
    NumberTextField(
        value = executionOrder,
        label = "Bon d'exécution",
        onValueChange = { onExecutionOrderChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    DatePickerAndTextField(
        value = requestDate,
        label = "Date de demande",
        onValueChange = { onRequestDateChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    DatePickerAndTextField(
        value = workStartDate,
        label = "Date de début des travaux",
        onValueChange = { onWorkStartDateChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    DatePickerAndTextField(
        value = workFinishDate,
        label = "Date de fin des travaux",
        onValueChange = { onWorkFinishDateChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    AutoCompleteTextField(
        label = "Client",
        items = clients,
        selectedItem = selectedClient,
        onSelectedItemChange = { client ->
            onSelectedClientChange(client)
            onSelectedFarmNameChange(null) // Reset farm and pump when client changes
            onSelectedPumpNameChange(null)
        },
        displayText = { client -> client.name },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = spaceBetweenFields)
    selectedClient?.let {
        val farmNames = fetchFarmNames(selectedClient)
        FlexibleAutoCompleteTextField(
            label = "Installation",
            value = selectedFarmName,
            source = farmNames,
            onSelect = { farmName ->
                onSelectedFarmNameChange(farmName)
                onSelectedPumpNameChange(null)
            },
            onValueChange = { farmName ->
                onSelectedFarmNameChange(farmName)
            },
            displayText = { it },
            modifier = modifier.fillMaxWidth()
        )
    }
    Spacer(modifier = spaceBetweenFields)
    if (!selectedFarmName.isNullOrBlank()) {
        val pumpNames = fetchPumpNames(selectedFarmName)
        FlexibleAutoCompleteTextField(
            label = "Pompe",
            value = selectedPumpName,
            source = pumpNames,
            onSelect = { pumpName -> onSelectedPumpNameChange(pumpName) },
            onValueChange = { pumpName -> onSelectedPumpNameChange(pumpName) },
            displayText = { it },
            modifier = modifier.fillMaxWidth()
        )
    }
    Spacer(modifier = spaceBetweenFields)
    OperatorForm(
        operators = operators,
        onOperatorAdd = onOperatorAdd,
        onOperatorChange = onOperatorChange,
        onOperatorRemove = onOperatorRemove,
        modifier = modifier.fillMaxWidth()
    )
    Spacer(modifier = spaceBetweenFields)
    OperationTypeDropdown(
        selectedType = selectedType,
        onTypeSelected = onTypeSelected,
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationTypeDropdown(
    selectedType: Report.OperationType?,
    onTypeSelected: (Report.OperationType) -> Unit,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedType?.beautiful ?: "Sélectionner un type",
                onValueChange = {},
                readOnly = true,
                label = { Text("Type d'opération") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Report.OperationType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.beautiful) },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OperatorForm(
    operators: List<String>,
    onOperatorAdd: () -> Unit,
    onOperatorChange: (Int, String) -> Unit,
    onOperatorRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Opérateurs", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onOperatorAdd) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un opérateur")
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
                    label = { Text("Opérateur #${index + 1}") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onOperatorRemove(index) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer l'opérateur")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
