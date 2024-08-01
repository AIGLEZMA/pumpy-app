package screens

import Logger
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.Client
import models.Report
import screenmodels.AddEditReportScreenModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AddEditReportScreen(private val report: Report? = null) : Screen {

    val spaceBetweenFields = Modifier.height(8.dp)

    // TODO: add field tests
    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { AddEditReportScreenModel(report) }

        val state = screenModel.state
        val clients = listOf(
            Client(1, "AIT NASSER Jaouad", "", ""),
            Client(2, "GOURAIZIM Mouad", "", ""),
            Client(3, "AKHERAZZ Rayane", "", "")
        )
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
                            text = if (state.isEditMode) "Editer le rapport (#)" else "Rédiger un nouveau rapport",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(45.dp))
                    GeneralForm(
                        screenModel = screenModel,
                        clients = clients
                    )
                    repeat(20) { index ->
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            label = { Text("TextField $index") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ActionButtons(
                        navigator = navigator,
                        screenModel = screenModel
                    )
                    when {
                        state.isSaved -> {
                            LaunchedEffect(Unit) {
                                navigator.pop()
                                Logger.debug("Client (name: ${state.toString()}) edited.")
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

    // TODO: add icons
    @Composable
    fun GeneralForm(
        clients: List<Client>,
        screenModel: AddEditReportScreenModel,
        modifier: Modifier = Modifier
    ) {
        var selectedClient by mutableStateOf<Client?>(null)
        var selectedPump by mutableStateOf<Pump?>(null)

        val pumps = remember {
            mutableStateListOf(
                Pump(name = "Pump A"),
                Pump(name = "Pump B"),
                Pump(name = "Pump C")
            )
        }

        Text(
            text = "Général",
            style = MaterialTheme.typography.titleSmall,
            modifier = modifier
        )
        Spacer(modifier = spaceBetweenFields)
        NumberTextField(
            value = screenModel.executionOrder,
            label = "Bon d'exécution",
            onValueChange = { screenModel.executionOrder = it ?: 0L },
            modifier = modifier
        )
        Spacer(modifier = spaceBetweenFields)
        DateTextFieldAndPicker(
            value = screenModel.requestDate,
            label = "Date de demande",
            onValueChange = { screenModel.requestDate = it ?: LocalDate.now() },
            modifier = modifier
        )
        Spacer(modifier = spaceBetweenFields)
        DateTextFieldAndPicker(
            value = screenModel.workFinishDate,
            label = "Date de débit des travaux",
            onValueChange = { screenModel.workFinishDate = it ?: LocalDate.now() },
            modifier = modifier
        )
        Spacer(modifier = spaceBetweenFields)
        ClientSelector(
            clients = clients,
            onClientSelected = { selectedClient = it },
            modifier = modifier
        )
        Spacer(modifier = spaceBetweenFields)
        PumpSelector(
            pumps = pumps,
            onPumpSelected = { selectedPump = it },
            modifier = modifier
        )
    }

    @Composable
    fun ActionButtons(
        navigator: Navigator,
        screenModel: AddEditReportScreenModel,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { screenModel.saveReport() },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Enregister")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { navigator.pop() },
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

    @Composable
    fun ClientSelector(
        clients: List<Client>,
        onClientSelected: (Client?) -> Unit,
        modifier: Modifier = Modifier
    ) {
        var query by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        val filteredClients = clients.filter { it.name.contains(query, ignoreCase = true) }

        Column(modifier = modifier) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    expanded = it.isNotEmpty()
                    onClientSelected(null)
                },
                label = { Text("Client") },
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Show all clients")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (query.isEmpty()) {
                    clients.forEach { client ->
                        DropdownMenuItem(onClick = {
                            expanded = false
                            query = client.name
                            onClientSelected(client)
                        }) {
                            Text(text = client.name)
                        }
                    }
                } else {
                    if (filteredClients.isEmpty()) {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            onClientSelected(null) // No match found
                        }) {
                            Text(text = "Aucun client correspondant")
                        }
                    } else {
                        filteredClients.forEach { client ->
                            DropdownMenuItem(onClick = {
                                expanded = false
                                query = client.name
                                onClientSelected(client)
                            }) {
                                Text(text = client.name)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PumpSelector(
        pumps: List<Pump>,
        onPumpSelected: (Pump?) -> Unit,
        modifier: Modifier = Modifier
    ) {
        var query by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        val filteredPumps = pumps.filter { it.name.contains(query, ignoreCase = true) }

        Column(modifier = modifier) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    expanded = it.isNotEmpty()
                    onPumpSelected(null)
                },
                label = { Text("Pump") },
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Show all pumps")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (query.isEmpty()) {
                    pumps.forEach { pump ->
                        DropdownMenuItem(onClick = {
                            expanded = false
                            query = pump.name
                            onPumpSelected(pump)
                        }) {
                            Text(text = pump.name)
                        }
                    }
                } else {
                    if (filteredPumps.isEmpty()) {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            onPumpSelected(null) // No match found
                        }) {
                            Text(text = "Aucun pompe correspondant")
                        }
                    } else {
                        filteredPumps.forEach { pump ->
                            DropdownMenuItem(onClick = {
                                expanded = false
                                query = pump.name
                                onPumpSelected(pump)
                            }) {
                                Text(text = pump.name)
                            }
                        }
                        DropdownMenuItem(onClick = {
                            expanded = false
                            onPumpSelected(Pump(name = query)) // Create new pump with the entered name
                        }) {
                            Text(text = "Créer une nouvelle pompe: $query") // Create new pump message in French
                        }
                    }
                }
            }
        }
    }

    data class Pump(val name: String) // Example Pump data class


    fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}