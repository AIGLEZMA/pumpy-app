package screens.report

import Logger
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import models.Client
import models.Farm
import models.Pump
import screens.spaceBetweenFields
import ui.AutoCompleteTextField
import ui.DatePickerAndTextField
import ui.NumberTextField
import java.time.LocalDate

// TODO: add icons
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
    selectedFarm: Farm?,
    onSelectedFarmChange: (Farm?) -> Unit,
    selectedPump: Pump?,
    onSelectedPumpChange: (Pump?) -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Général",
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier
    )
    Spacer(modifier = spaceBetweenFields)
    NumberTextField(
        value = executionOrder,
        label = "Bon d'exécution",
        onValueChange = { onExecutionOrderChange(it) },
        modifier = modifier
    )
    Spacer(modifier = spaceBetweenFields)
    DatePickerAndTextField(
        value = requestDate,
        label = "Date de demande",
        onValueChange = { onRequestDateChange(it) },
        modifier = modifier
    )
    Spacer(modifier = spaceBetweenFields)
    DatePickerAndTextField(
        value = workFinishDate,
        label = "Date de débit des travaux",
        onValueChange = { onWorkFinishDateChange(it) },
        modifier = modifier
    )
    Spacer(modifier = spaceBetweenFields)
    AutoCompleteTextField(
        label = "Client",
        value = selectedClient?.name,
        source = clients,
        onSelect = { client ->
            Logger.debug("Selected ${client.name} client")
            onSelectedClientChange(client)
        },
        displayText = { client -> client.name }
    )
    Spacer(modifier = spaceBetweenFields)
    selectedClient?.let {
        AutoCompleteTextField(
            label = "Ferme",
            value = selectedFarm?.name,
            source = emptyList<Farm>(),
            onSelect = { farm ->
                Logger.debug("Selected ${farm.name} farm")
                onSelectedFarmChange(farm)
            },
            displayText = { farm -> farm.name }
        )
    }
}