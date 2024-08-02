package screens

import Logger
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.Client
import models.Farm
import models.Pump
import models.Report
import screenmodels.AddEditReportScreenModel
import ui.AutoCompleteTextField
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AddEditReportScreen(private val report: Report? = null) : Screen {

    val spaceBetweenFields = Modifier.height(8.dp)

    // TODO: add field tests
    // TODO: add loading progress
    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { AddEditReportScreenModel(report) }

        val state = screenModel.state
        val clients = screenModel.clients

        var selectedClient by remember { mutableStateOf<Client?>(null) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center // Center the content
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(400.dp)
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(45.dp))
                    Title(
                        isEditMode = state.isEditMode,
                        reportId = null
                    )
                    Spacer(modifier = Modifier.height(45.dp))
                    GeneralForm(
                        executionOrder = screenModel.executionOrder,
                        onExecutionOrderChange = { screenModel.executionOrder = it },
                        requestDate = screenModel.requestDate,
                        onRequestDateChange = { screenModel.requestDate = it },
                        workFinishDate = screenModel.workFinishDate,
                        onWorkFinishDateChange = { screenModel.workFinishDate = it },
                        clients = clients,
                        selectedClient = selectedClient,
                        onSelectedClientChange = { selectedClient = it },
                        selectedFarm = null,
                        onSelectedFarmChange = { },
                        selectedPump = null,
                        onSelectedPumpChange = { }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ActionButtons(
                        saveReport = { screenModel.saveReport() },
                        onCancel = { navigator.pop() }
                    )
                    when {
                        state.isSaved -> {
                            LaunchedEffect(Unit) {
                                navigator.pop()
                                Logger.debug("Report (name: $state) edited.")
                            }
                        }

                        state.errorMessage != null -> {
                            Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState)
                )
            }
        }
    }

    @Composable
    fun Title(
        isEditMode: Boolean,
        reportId: Long?
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(400.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MAGRINOV",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isEditMode) "Editer le rapport (#$reportId)" else "Rédiger un nouveau rapport",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

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
        DateTextFieldAndPicker(
            value = requestDate,
            label = "Date de demande",
            onValueChange = { onRequestDateChange(it) },
            modifier = modifier
        )
        Spacer(modifier = spaceBetweenFields)
        DateTextFieldAndPicker(
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

    @Composable
    fun ActionButtons(
        saveReport: () -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = saveReport,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Enregister")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Annuler")
            }
        }
    }

    @Composable
    fun NumberTextField(
        value: Long?,
        label: String,
        onValueChange: (Long?) -> Unit,
        modifier: Modifier = Modifier
    ) {
        var text by remember { mutableStateOf(value?.toString() ?: "") }

        OutlinedTextField(
            value = text,
            onValueChange = { newValue ->
                // Update the text state
                text = newValue

                // Convert to Long and update the parent state
                onValueChange(newValue.toLongOrNull())
            },
            label = { Text(label) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = modifier.fillMaxWidth()
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DateTextFieldAndPicker(
        value: LocalDate?,
        label: String,
        onValueChange: (LocalDate?) -> Unit,
        modifier: Modifier = Modifier
    ) {
        var showDatePicker by remember { mutableStateOf(false) }
        var textValue by remember(value) {
            mutableStateOf(
                value?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""
            )
        }
        val datePickerState = rememberDatePickerState()
        OutlinedTextField(
            value = textValue,
            onValueChange = {
                textValue = it
                try {
                    val parsedDate = LocalDate.parse(it, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    onValueChange(parsedDate)
                } catch (e: Exception) {
                    onValueChange(null) // Invalid date format
                }
            },
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Select date")
                }
            },
            modifier = modifier.fillMaxWidth()
        )
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                onValueChange(selectedDate)
                                textValue = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            }
                        },
                        enabled = datePickerState.selectedDateMillis != null
                    ) {
                        Text(text = "Confirmer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(text = "Rejeter")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}