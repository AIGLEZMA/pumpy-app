package screens.report

import Logger
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Text(
        text = "Général",
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier.padding(bottom = 16.dp)
    )
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
        value = workFinishDate,
        label = "Date de débit des travaux",
        onValueChange = { onWorkFinishDateChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    AutoCompleteTextField(
        label = "Client",
        value = selectedClient?.name,
        source = clients,
        onSelect = { client ->
            Logger.debug("[GeneralForm] Selected ${client.name} client")
            onSelectedClientChange(client)
        },
        displayText = { client -> client.name },
        onValueChange = { },
        modifier = modifier.fillMaxWidth()
    )
    Spacer(modifier = spaceBetweenFields)
    selectedClient?.let {
        val farmNames = fetchFarmNames(selectedClient)
        AutoCompleteTextField(
            label = "Installation",
            value = selectedFarmName,
            source = farmNames,
            onSelect = { },
            onValueChange = { farmName ->
                Logger.debug("[GeneralForm] Value changed to $farmName farm")
                onSelectedFarmNameChange(farmName)
            },
            displayText = { farmName -> farmName },
            modifier = modifier.fillMaxWidth()
        )
    }
    Spacer(modifier = spaceBetweenFields)
    selectedFarmName?.let {
        val pumpNames = fetchPumpNames(it)
        AutoCompleteTextField(
            label = "Pompe",
            value = selectedPumpName,
            source = pumpNames,
            onSelect = { },
            onValueChange = { pumpName ->
                Logger.debug("[GeneralForm] Value changed to $pumpName pump")
                onSelectedPumpNameChange(pumpName)
            },
            displayText = { pumpName -> pumpName },
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

@Composable
fun OperationTypeDropdown(
    selectedType: Report.OperationType?,
    onTypeSelected: (Report.OperationType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedType?.beautiful ?: "Type d'opération",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    Modifier.clickable { expanded = true }
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Report.OperationType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(text = type.beautiful) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Intervenants",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                operators.forEachIndexed { index, operator ->
                    OperatorTextField(
                        operator = operator,
                        onValueChange = { newOperator ->
                            onOperatorChange(index, newOperator)
                        },
                        onRemoveClick = {
                            onOperatorRemove(index)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (index < operators.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Ajouter un intervenant",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onOperatorAdd() }
                    )
                }
            }
        }
    }
}

@Composable
fun OperatorTextField(
    operator: String,
    onValueChange: (String) -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = operator,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .heightIn(min = 40.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            singleLine = true,
            shape = MaterialTheme.shapes.small
        )
        IconButton(
            onClick = onRemoveClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "Remove Operator",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
