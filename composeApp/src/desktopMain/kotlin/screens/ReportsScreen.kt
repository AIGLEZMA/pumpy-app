package screens

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.*
import screenmodels.ClientsScreenModel
import screenmodels.LoginScreenModel
import screenmodels.ReportsScreenModel
import screenmodels.UsersScreenModel
import ui.DeleteConfirmationDialog
import ui.Layout
import ui.Loading
import java.time.format.DateTimeFormatter
import java.util.*

class ReportsScreen : Screen {

    private val FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH)

    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { ReportsScreenModel() }
        val clientsScreenModel = rememberScreenModel { ClientsScreenModel() }
        val usersScreenModel = rememberScreenModel { UsersScreenModel() }
        val loginScreenModel = navigator.rememberNavigatorScreenModel { LoginScreenModel() }

        var searchQuery by rememberSaveable { mutableStateOf("") }
        var reportToDelete by remember { mutableStateOf<Report?>(null) }

        LaunchedEffect(Unit) {
            screenModel.loadFarms()
            screenModel.loadPumps()
            screenModel.loadReports()
        }

        val filteredReports = screenModel.reports.filter { report -> true } // TODO: filter

        val isLoading = screenModel.isLoading

        Layout(
            selected = "reports",
            onReportsClick = { },
            onClientsClick = { navigator.push(ClientsScreen()) },
            onUsersClick = { navigator.push(UsersScreen()) },
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onLogout = {
                loginScreenModel.logout()
                navigator.popUntilRoot()
            },
            isDarkMode = Theme.isDarkTheme,
            onToggleTheme = { Theme.toggleTheme() },
            onFabClick = { navigator.push(AddEditReportScreen()) }
        ) {
            // TODO: no reports view
            if (isLoading) {
                Loading()
            } else {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                ) {
                    Title(reports = filteredReports)
                    Spacer(Modifier.height(20.dp))
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val state = rememberLazyListState()
                        ReportsList(
                            state = state,
                            reports = filteredReports,
                            pumps = screenModel.pumps,
                            farms = screenModel.farms,
                            clients = clientsScreenModel.clients,
                            users = usersScreenModel.users,
                            onReportEditClick = {
                                navigator.push(AddEditReportScreen(it))
                            }, // TODO: permissions
                            onReportSaveClick = { report, clientUsername, creatorName, farm, pump ->
                                screenModel.savePdf(
                                    report,
                                    clientUsername,
                                    creatorName,
                                    farm,
                                    pump
                                )
                            },
                            onReportPrintClick = { },
                            onReportDeleteClick = { reportToDelete = it } // TODO: permissions
                        )
                        VerticalScrollbar(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(
                                scrollState = state
                            )
                        )
                    }
                    reportToDelete?.let { report ->
                        DeleteConfirmationDialog(
                            title = "Supprimer le rapport",
                            message = "Êtes-vous sûr de vouloir supprimer le rapport ${report.reportId}",
                            onConfirm = {
                                screenModel.deleteReport(report)
                                reportToDelete = null
                            },
                            onDismiss = { reportToDelete = null }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun Title(
        reports: List<Report>,
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier.padding(top = 30.dp, start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rapports",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier.width(5.dp))
            Text(
                text = "(${reports.size})",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    @Composable
    fun ReportsList(
        state: LazyListState,
        reports: List<Report>,
        pumps: List<Pump>,
        farms: List<Farm>,
        clients: List<Client>,
        users: List<User>,
        onReportEditClick: (Report) -> Unit,
        onReportSaveClick: (Report, String, String, String, String) -> Unit,
        onReportPrintClick: (Report) -> Unit,
        onReportDeleteClick: (Report) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        LazyColumn(
            state = state,
            modifier = modifier.fillMaxSize()
        ) {
            item {
                ListHeader()
            }
            items(reports) { report ->
                val creator = users.firstOrNull { it.id == report.creatorId }
                val pump = pumps.firstOrNull { it.pumpId == report.pumpOwnerId }
                val farm = farms.firstOrNull { it.farmId == pump?.farmOwnerId }
                val client = clients.firstOrNull { it.clientId == farm?.clientOwnerId }

                var expanded by remember { mutableStateOf(false) }
                var menuExpanded by remember { mutableStateOf(false) }
                var isHovered by remember { mutableStateOf(false) }

                val backgroundColor = if (isHovered) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isHovered = true
                                    tryAwaitRelease()
                                    isHovered = false
                                }
                            )
                        }
                        .clickable(onClick = { expanded = !expanded })
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        report.reportId.toString(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                    )

                    Text(
                        report.requestDate.format(FORMATTER),
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                    )

                    Text(
                        pump?.name ?: "Inconnu",
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                    )
                    Text(
                        farm?.name ?: "Inconnu",
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                    )
                    Text(
                        client?.name ?: "Inconnu",
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                    )
                    Text(
                        creator?.username ?: "Inconnu",
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                    )
                    Row(
                        modifier = Modifier.weight(2f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = {
                                onReportSaveClick(
                                    report,
                                    client?.name ?: "",
                                    creator?.username ?: "",
                                    farm?.name ?: "",
                                    pump?.name ?: ""
                                )
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onReportEditClick(report) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "More actions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Imprimer") },
                            onClick = {
                                onReportPrintClick(report)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Supprimer") },
                            onClick = {
                                onReportDeleteClick(report)
                                menuExpanded = false
                            }
                        )
                    }
                }
                if (expanded) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RectangleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Détails du rapport",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Général:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Bon d'éxecution:")
                                        Text(report.executionOrder.toString())
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Date de demande:")
                                        Text(
                                            report.requestDate.format(
                                                DateTimeFormatter.ofPattern(
                                                    "dd MMM yyyy",
                                                    Locale.FRENCH
                                                )
                                            )
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Débit de travaux:")
                                        Text(
                                            report.workFinishDate.format(
                                                DateTimeFormatter.ofPattern(
                                                    "dd MMM yyyy",
                                                    Locale.FRENCH
                                                )
                                            )
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Client:")
                                        Text(client?.name ?: "Inconnu")
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Installation:")
                                        Text(farm?.name ?: "Inconnu")
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Pompe:")
                                        Text(pump?.name ?: "Inconnu")
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Intervenants:")
                                        Text(report.operators.joinToString(" - "))
                                    }
                                }
                                Spacer(modifier = Modifier.width(24.dp))
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Technique:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Profondeur (m):")
                                        Text("${report.depth} m")
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Niveau statique (m):")
                                        Text("${report.staticLevel} m")
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Niveau dynamique (m):")
                                        Text("${report.dynamicLevel} m")
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Calage de pompe (m):")
                                        Text("${report.pumpShimming} m")
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Débit (m3/h):")
                                        Text("${report.speed} m3/h")
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Type:")
                                        Text(report.type.beautiful)
                                    }
                                    if (report.type == Report.OperationType.ASSEMBLY) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Moteur:")
                                            Text(report.engine ?: "Inconnu")
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Pompe:")
                                            Text(report.pump ?: "Inconnu")
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Élements:")
                                            Text(report.elements ?: "Inconnu")
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Travaux effectués & observations:")
                                        Text(report.notes ?: "Inconnu")
                                    }
                                }
                                Spacer(modifier = Modifier.width(24.dp))
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Executive:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Demande d'achat:")
                                        Text(report.purchaseRequest)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Devis:")
                                        Text(report.quotation)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Bon de commande:")
                                        Text(report.purchaseOrder)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Facture:")
                                        Text(report.invoice)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Date de facturation:")
                                        Text(report.invoiceDate?.format(FORMATTER) ?: "Inconnu")
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }

    @Composable
    fun ListHeader(
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "ID",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
            Text(
                "Date",
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
            Text(
                "Pompe",
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
            Text(
                "Installation",
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
            Text(
                "Client",
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
            Text(
                "Demandeur",
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
            Spacer(modifier = Modifier.weight(2f))
        }
        HorizontalDivider(modifier = modifier.padding(horizontal = 8.dp))
    }
}