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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.Report
import models.Report.OperationType
import screenmodels.AddEditReportScreenModel
import screenmodels.ClientsScreenModel
import screenmodels.LoginScreenModel
import screenmodels.ReportsScreenModel
import screens.report.ExecutiveForm
import screens.report.GeneralForm
import screens.report.TechnicalForm

val spaceBetweenFields = Modifier.height(4.dp)

class AddEditReportScreen(private val report: Report? = null) : Screen {

    // TODO: add field tests
    // TODO: add loading progress
    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val reportsScreenModel = rememberScreenModel { ReportsScreenModel() }
        val clientsScreenModel = rememberScreenModel { ClientsScreenModel() }
        val loginScreenModel = rememberScreenModel { LoginScreenModel() }
        val screenModel = rememberScreenModel { AddEditReportScreenModel(report) }

        val state = screenModel.state
        val clients = clientsScreenModel.clients

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
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
                        onRequestDateChange = {
                            Logger.debug("[Report] @ Date Old: ${screenModel.requestDate} | New: $it")
                            screenModel.requestDate = it
                        },
                        workFinishDate = screenModel.workFinishDate,
                        onWorkFinishDateChange = { screenModel.workFinishDate = it },
                        clients = clients,
                        selectedClient = screenModel.selectedClient,
                        fetchFarmNames = { client -> reportsScreenModel.fetchFarmNames(client) },
                        onSelectedClientChange = {
                            screenModel.selectedClient = it
                        },
                        selectedFarmName = screenModel.selectedFarmName,
                        onSelectedFarmNameChange = { screenModel.selectedFarmName = it },
                        fetchPumpNames = { farmName -> reportsScreenModel.fetchPumpNames(farmName) },
                        selectedPumpName = screenModel.selectedPumpName,
                        onSelectedPumpNameChange = { screenModel.selectedPumpName = it },
                        operators = screenModel.operators,
                        onOperatorAdd = { screenModel.addOperator() },
                        onOperatorRemove = { screenModel.removeOperator(it) },
                        onOperatorChange = { i, s -> screenModel.updateOperator(i, s) },
                        selectedType = screenModel.type,
                        onTypeSelected = { screenModel.type = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TechnicalForm(
                        depth = screenModel.depth,
                        onDepthChange = { screenModel.depth = it },
                        staticLevel = screenModel.staticLevel,
                        onStaticLevelChange = { screenModel.staticLevel = it },
                        dynamicLevel = screenModel.dynamicLevel,
                        onDynamicLevelChange = { screenModel.dynamicLevel = it },
                        pumpShimming = screenModel.pumpShimming,
                        onPumpShimmingChange = { screenModel.pumpShimming = it },
                        speed = screenModel.speed,
                        onSpeedChange = { screenModel.speed = it },
                        type = screenModel.type ?: OperationType.ASSEMBLY,
                        engine = screenModel.engine,
                        onEngineChange = { screenModel.engine = it },
                        pump = screenModel.pump,
                        onPumpChange = { screenModel.pump = it },
                        elements = screenModel.elements,
                        onElementsChange = { screenModel.elements = it },
                        notes = screenModel.notes,
                        onNotesChange = { screenModel.notes = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
                    ActionButtons(
                        saveReport = {
                            val loggedInUser = loginScreenModel.loginState.user
                            if (loggedInUser == null) {
                                Logger.debug("[AddEditReportScreen] ERROR: Logged in user is null")
                                return@ActionButtons
                            }
                            screenModel.saveReport(loggedInUser)
                        },
                        onCancel = { navigator.pop() }
                    )
                    when {
                        state.isSaved -> {
                            LaunchedEffect(Unit) {
                                navigator.pop()
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
        reportId: Long?,
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

    @Composable
    fun ActionButtons(
        saveReport: () -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier = Modifier,
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
}