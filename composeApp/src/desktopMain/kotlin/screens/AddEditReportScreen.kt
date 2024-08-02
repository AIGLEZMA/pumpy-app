package screens

import Logger
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.Client
import models.Report
import screenmodels.AddEditReportScreenModel
import screens.report.GeneralForm

val spaceBetweenFields = Modifier.height(8.dp)

class AddEditReportScreen(private val report: Report? = null) : Screen {

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
}