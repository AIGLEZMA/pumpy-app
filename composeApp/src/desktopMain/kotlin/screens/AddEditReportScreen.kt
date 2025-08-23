package screens

import Logger
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.Report
import models.Report.OperationType
import screenmodels.AddEditReportScreenModel
import screenmodels.ClientsScreenModel
import screenmodels.LoginScreenModel
import screens.report.ExecutiveForm
import screens.report.GeneralForm
import screens.report.TechnicalForm

val spaceBetweenFields = Modifier.height(4.dp)

class AddEditReportScreen(private val report: Report? = null) : Screen {

    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val clientsScreenModel = rememberScreenModel { ClientsScreenModel() }
        val loginScreenModel = navigator.rememberNavigatorScreenModel { LoginScreenModel() }
        val screenModel = rememberScreenModel { AddEditReportScreenModel(report) }

        val state = screenModel.state
        val company = loginScreenModel.loginState.company
        val clients = clientsScreenModel.clients.filter { it.company == company }

        // Ensure clients are loaded for the autocomplete
        LaunchedEffect(clientsScreenModel) {
            clientsScreenModel.loadClients()
        }

        // Save action
        fun save() {
            val loggedInUser = loginScreenModel.loginState.user
            val company = loginScreenModel.loginState.company
            if (loggedInUser == null) {
                Logger.debug("[AddEditReportScreen] ERROR: Logged in user is null")
                return
            }
            screenModel.saveReport(loggedInUser, company)
        }

        // Navigate back after successful save
        LaunchedEffect(state.isSaved) {
            if (state.isSaved) navigator.pop()
        }

        LaunchedEffect(clients, report) {
            if (report != null && screenModel.selectedClient == null) {
                screenModel.selectedClient = clients.find { it.clientId == report.clientOwnerId }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                // Keyboard shortcut: Ctrl/Cmd + Enter to save
                .onPreviewKeyEvent { e ->
                    if (e.type == KeyEventType.KeyUp &&
                        e.key == Key.Enter &&
                        (e.isCtrlPressed || e.isMetaPressed)
                    ) {
                        if (!state.isLoading) save()
                        true
                    } else false
                },
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top progress when saving
                    if (state.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                    }

                    // Title
                    Title(isEditMode = state.isEditMode, reportId = report?.reportId)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Three forms in a row, each with its own scrollbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // General
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            val scrollStateGeneral = rememberScrollState()
                            Column(modifier = Modifier.fillMaxSize().padding(end = 24.dp)) {
                                Text("Informations Générales", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollStateGeneral)
                                ) {
                                    GeneralForm(
                                        executionOrder = screenModel.executionOrder,
                                        onExecutionOrderChange = { screenModel.executionOrder = it },
                                        requestDate = screenModel.requestDate,
                                        onRequestDateChange = { screenModel.requestDate = it },
                                        workStartDate = screenModel.workStartDate,
                                        onWorkStartDateChange = { screenModel.workStartDate = it },
                                        workFinishDate = screenModel.workFinishDate,
                                        onWorkFinishDateChange = { screenModel.workFinishDate = it },
                                        clients = clients,
                                        selectedClient = screenModel.selectedClient,
                                        onSelectedClientChange = { screenModel.selectedClient = it },
                                        asker = screenModel.asker,
                                        onAskerChange = { screenModel.asker = it },
                                        wellDrilling = screenModel.wellDrilling,
                                        onWellDrillingChange = { screenModel.wellDrilling = it },
                                        operators = screenModel.operators,
                                        onOperatorAdd = { screenModel.addOperator() },
                                        onOperatorRemove = { screenModel.removeOperator(it) },
                                        onOperatorChange = { i, s -> screenModel.updateOperator(i, s) },
                                        selectedType = screenModel.type,
                                        onTypeSelected = { screenModel.type = it }
                                    )
                                }
                            }
                            VerticalScrollbar(
                                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(scrollStateGeneral)
                            )
                        }

                        // Technical
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            val scrollStateTechnical = rememberScrollState()
                            Column(modifier = Modifier.fillMaxSize().padding(end = 24.dp)) {
                                Text("Informations Techniques", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollStateTechnical)
                                ) {
                                    TechnicalForm(
                                        depth = screenModel.depth,
                                        onDepthChange = { screenModel.depth = it },
                                        staticLevel = screenModel.staticLevel,
                                        onStaticLevelChange = { screenModel.staticLevel = it },
                                        dynamicLevel = screenModel.dynamicLevel,
                                        onDynamicLevelChange = { screenModel.dynamicLevel = it },
                                        pumpShimming = screenModel.pumpShimming,
                                        onPumpShimmingChange = { screenModel.pumpShimming = it },
                                        secondPumpShimming = screenModel.secondPumpShimming,
                                        onSecondPumpShimmingChange = { screenModel.secondPumpShimming = it },
                                        speed = screenModel.speed,
                                        onSpeedChange = { screenModel.speed = it },
                                        current = screenModel.current,
                                        onCurrentChange = { screenModel.current = it },
                                        type = screenModel.type ?: OperationType.ASSEMBLY,
                                        depthAfterCleaning = screenModel.depthAfterCleaning,
                                        onDepthAfterCleaningChange = { screenModel.depthAfterCleaning = it },
                                        engine = screenModel.engine,
                                        onEngineChange = { screenModel.engine = it },
                                        pump = screenModel.pump,
                                        onPumpChange = { screenModel.pump = it },
                                        elements = screenModel.elements,
                                        onElementAdd = { screenModel.addElement() },
                                        onElementRemove = { screenModel.removeElement(it) },
                                        onElementChange = { i, s -> screenModel.updateElement(i, s) },
                                        notes = screenModel.notes,
                                        onNotesChange = { screenModel.notes = it }
                                    )
                                }
                            }
                            VerticalScrollbar(
                                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(scrollStateTechnical)
                            )
                        }

                        // Executive
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            val scrollStateExecutive = rememberScrollState()
                            Column(modifier = Modifier.fillMaxSize().padding(end = 24.dp)) {
                                Text("Informations Exécutives", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollStateExecutive)
                                ) {
                                    ExecutiveForm(
                                        purchaseRequest = screenModel.purchaseRequest,
                                        onPurchaseRequestChange = { screenModel.purchaseRequest = it },
                                        quotation = screenModel.quotation,
                                        onQuotationChange = { screenModel.quotation = it },
                                        purchaseOrder = screenModel.purchaseOrder,
                                        onPurchaseOrderChange = { screenModel.purchaseOrder = it },
                                        invoice = screenModel.invoice,
                                        onInvoiceChange = { screenModel.invoice = it },
                                        invoiceDate = screenModel.invoiceDate,
                                        onInvoiceDateChange = { screenModel.invoiceDate = it }
                                    )
                                }
                            }
                            VerticalScrollbar(
                                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(scrollStateExecutive)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ActionButtons(
                            saveReport = { if (!state.isLoading) save() },
                            onCancel = { navigator.pop() },
                            saving = state.isLoading
                        )
                    }

                    // Error
                    state.errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    @Composable
    fun Title(
        isEditMode: Boolean,
        reportId: Long?,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "MAGRINOV", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isEditMode) "Éditer le rapport (#$reportId)" else "Rédiger un nouveau rapport",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    @Composable
    fun ActionButtons(
        saveReport: () -> Unit,
        onCancel: () -> Unit,
        saving: Boolean,
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !saving,
                modifier = Modifier.width(150.dp)
            ) {
                Text("Annuler")
            }
            Button(
                onClick = saveReport,
                enabled = !saving,
                modifier = Modifier.width(150.dp)
            ) {
                Text("Enregistrer")
            }
        }
    }
}
