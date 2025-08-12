package screens.report

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    selectedFarmName: String?,
    onSelectedFarmNameChange: (String?) -> Unit,
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
        onSelectedItemChange = { client -> onSelectedClientChange(client) },
        displayText = { client -> client.name },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = spaceBetweenFields)
    OutlinedTextField(
        value = selectedFarmName ?: "",
        onValueChange = {
            onSelectedFarmNameChange(it)
        },
        label = { Text("Installation") },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
    Spacer(modifier = spaceBetweenFields)
    OutlinedTextField(
        value = selectedPumpName ?: "",
        onValueChange = {
            onSelectedPumpNameChange(it)
        },
        label = { Text("Forage") },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
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
    OperationTypeBreadcrumb(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationTypeBreadcrumb(
    selectedType: Report.OperationType?,
    onTypeSelected: (Report.OperationType) -> Unit,
    modifier: Modifier = Modifier
) {
    // Map your enum names (adjust DISASSEMBLY if your enum uses another name)
    val assembly = Report.OperationType.ASSEMBLY
    val disassembly = Report.OperationType.DISASSEMBLY

    Column(modifier = modifier) {
        Text(
            text = "Type d'opération",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = selectedType == assembly,
                onClick = { onTypeSelected(assembly) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text("Montage") }
            )
            SegmentedButton(
                selected = selectedType == disassembly,
                onClick = { onTypeSelected(disassembly) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text("Démontage") }
            )
        }
    }
}

