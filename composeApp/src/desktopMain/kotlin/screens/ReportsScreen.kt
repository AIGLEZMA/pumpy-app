package screens

import Theme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.Client
import models.Company
import models.Report
import models.User
import screenmodels.ClientsScreenModel
import screenmodels.LoginScreenModel
import screenmodels.ReportsScreenModel
import screenmodels.UsersScreenModel
import ui.DeleteConfirmationDialog
import ui.Layout
import ui.Loading
import java.text.Normalizer
import java.time.format.DateTimeFormatter
import java.util.*

class ReportsScreen : Screen {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH)

    private fun String.folded(): String =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase(Locale.getDefault())

    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val reportsModel = navigator.rememberNavigatorScreenModel { ReportsScreenModel() }
        val clientsModel = rememberScreenModel { ClientsScreenModel() }
        val usersModel = rememberScreenModel { UsersScreenModel() }
        val loginModel = navigator.rememberNavigatorScreenModel { LoginScreenModel() }

        val currentCompany: Company = loginModel.loginState.company

        var searchQuery by rememberSaveable { mutableStateOf("") }
        var reportToDelete by remember { mutableStateOf<Report?>(null) }

        val snackbarHostState = remember { SnackbarHostState() }

        // Load everything once
        LaunchedEffect(reportsModel, clientsModel, usersModel) {
            usersModel.loadUsers()
            clientsModel.loadClients()
            reportsModel.loadReports()
        }

        // Build a clients lookup (used for filtering + list)
        val clientsById = remember(clientsModel.clients) {
            clientsModel.clients.associateBy { it.clientId }
        }

        // Filter by company AND (if provided) by client's name
        val filteredReports by remember(
            reportsModel.reports, currentCompany, searchQuery, clientsModel.clients
        ) {
            derivedStateOf {
                val q = searchQuery.trim().folded()
                reportsModel.reports
                    .asSequence()
                    .filter { it.company == currentCompany }
                    .filter { report ->
                        if (q.isEmpty()) return@filter true
                        val clientName = clientsById[report.clientOwnerId]?.name.orEmpty().folded()
                        clientName.contains(q)
                    }
                    .toList()
            }
        }

        val isLoading = reportsModel.isLoading

        // Collect UI events (save/print errors)
        LaunchedEffect(reportsModel) {
            reportsModel.events.collect { evt ->
                when (evt) {
                    is ReportsScreenModel.UiEvent.Saved -> {
                        val res = snackbarHostState.showSnackbar(
                            message = "Rapport enregistré : ${evt.path}",
                            actionLabel = "Ouvrir",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                        if (res == SnackbarResult.ActionPerformed) {
                            // fire-and-forget open
                            kotlin.runCatching {
                                java.awt.Desktop.getDesktop().open(java.io.File(evt.path))
                            }
                        }
                    }

                    is ReportsScreenModel.UiEvent.Printed -> {
                        snackbarHostState.showSnackbar(
                            message = "Document envoyé à l’impression",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                    }

                    is ReportsScreenModel.UiEvent.Error -> {
                        snackbarHostState.showSnackbar(
                            message = evt.message,
                            withDismissAction = true,
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        }

        Layout(
            selected = "reports",
            onReportsClick = { },
            onClientsClick = { navigator.push(ClientsScreen()) },
            onUsersClick = { navigator.push(UsersScreen()) },
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onLogout = {
                loginModel.logout(); navigator.popUntilRoot()
            },
            isDarkMode = Theme.isDarkTheme,
            onToggleTheme = { Theme.toggleTheme() },
            onFabClick = { navigator.push(AddEditReportScreen()) },
            companyLabel = currentCompany.pretty,
            autoOpenAfterSave = reportsModel.autoOpenAfterSave,
            onAutoOpenAfterSaveChange = { reportsModel.updateAutoOpenAfterSave(it) }
        ) {
            // Wrap content so we can overlay the SnackbarHost at bottom
            Box(Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                ) {
                    Title(reports = filteredReports)
                    Spacer(Modifier.height(20.dp))

                    if (isLoading) {
                        Loading()
                    } else {
                        Box(Modifier.fillMaxSize()) {
                            val state = rememberLazyListState()
                            ReportsList(
                                state = state,
                                reports = filteredReports,
                                clients = clientsModel.clients,
                                users = usersModel.users,
                                currentCompany = currentCompany,
                                onReportEditClick = { navigator.push(AddEditReportScreen(it)) },
                                onReportSaveClick = { report, clientUsername, creatorName, farm, pump, company ->
                                    reportsModel.savePdf(report, clientUsername, creatorName, farm, pump, company)
                                },
                                onReportPrintClick = { report ->
                                    val creator =
                                        usersModel.users.firstOrNull { it.id == report.creatorId }?.username ?: ""
                                    val client =
                                        clientsModel.clients.firstOrNull { it.clientId == report.clientOwnerId }?.name
                                            ?: ""
                                    reportsModel.printReport(
                                        report = report,
                                        clientUsername = client,
                                        creatorName = creator,
                                        farmName = report.farm,
                                        pumpName = report.wellDrilling,
                                        company = currentCompany
                                    )
                                },
                                onReportDeleteClick = { reportToDelete = it }
                            )
                            VerticalScrollbar(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(state)
                            )
                        }
                    }

                    reportToDelete?.let { report ->
                        DeleteConfirmationDialog(
                            title = "Supprimer le rapport",
                            message = "Êtes-vous sûr de vouloir supprimer le rapport ${report.reportId}",
                            onConfirm = {
                                reportsModel.deleteReport(report)
                                reportToDelete = null
                            },
                            onDismiss = { reportToDelete = null }
                        )
                    }
                }

                // Snackbar host anchored at the bottom of the screen
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
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
        clients: List<Client>,
        users: List<User>,
        currentCompany: Company,
        onReportEditClick: (Report) -> Unit,
        onReportSaveClick: (Report, String, String, String, String, Company) -> Unit,
        onReportPrintClick: (Report) -> Unit,
        onReportDeleteClick: (Report) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        // Build lookup maps once per recomposition
        val clientsById = remember(clients) { clients.associateBy { it.clientId } }
        val usersById = remember(users) { users.associateBy { it.id } }

        LazyColumn(
            state = state,
            modifier = modifier.fillMaxSize()
        ) {
            item { ListHeader() }
            items(reports) { report ->
                val creator = usersById[report.creatorId]
                val client = clientsById[report.clientOwnerId]

                // Proper debug print (no accidental list concatenation)
                println("clients=${clients.joinToString { it.clientId.toString() }}  id=${report.clientOwnerId}")

                var expanded by remember { mutableStateOf(false) }
                var menuExpanded by remember { mutableStateOf(false) }
                var isHovered by remember { mutableStateOf(false) }

                val backgroundColor =
                    if (isHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surface

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
                        .clickable { expanded = !expanded }
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
                        report.requestDate.format(dateTimeFormatter),
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                    )
                    Text(
                        report.wellDrilling,
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                    )
                    Text(
                        report.farm,
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
                                    report.farm,
                                    report.wellDrilling,
                                    currentCompany
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
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RectangleShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                    Text("Général:", style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Bon d'éxecution:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(report.executionOrder.toString())
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Date de demande:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            report.requestDate.format(
                                                DateTimeFormatter.ofPattern(
                                                    "dd MMM yyyy",
                                                    Locale.FRENCH
                                                )
                                            )
                                        )
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Début des travaux:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            report.workStartDate.format(
                                                DateTimeFormatter.ofPattern(
                                                    "dd MMM yyyy",
                                                    Locale.FRENCH
                                                )
                                            )
                                        )
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Fin des travaux:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            report.workFinishDate.format(
                                                DateTimeFormatter.ofPattern(
                                                    "dd MMM yyyy",
                                                    Locale.FRENCH
                                                )
                                            )
                                        )
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Client:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(client?.name ?: "Inconnu")
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Installation:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(report.farm)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Forage:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(report.wellDrilling)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Intervenants:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(report.operators.joinToString(" - "))
                                    }
                                }
                                Spacer(modifier = Modifier.width(24.dp))
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "Technique:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp)
                                    )
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Profondeur (m):",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text("${report.depth} m")
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Niveau statique (m):",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text("${report.staticLevel} m")
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Niveau dynamique (m):",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text("${report.dynamicLevel} m")
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Calage de pompe (m):",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text("${report.pumpShimming} m")
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Débit (m3/h):",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text("${report.speed} m3/h")
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Type:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(report.type.beautiful)
                                    }
                                    if (report.type == Report.OperationType.ASSEMBLY) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(
                                                "Moteur:",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(report.engine ?: "Inconnu")
                                        }
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(
                                                "Pompe:",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(report.pump ?: "Inconnu")
                                        }
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(
                                                "Élements:",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                            val elements = report.elements
                                            if (elements.isEmpty()) {
                                                Text("Aucuns")
                                            } else {
                                                Column { elements.forEach { element -> Text(text = element) } }
                                            }
                                        }
                                    }
                                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Travaux effectués & observations:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Text(report.notes ?: "Inconnu")
                                    }
                                }
                                Spacer(modifier = Modifier.width(24.dp))
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "Executive:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp)
                                    )
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Demande d'achat:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(report.purchaseRequest)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Devis:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(report.quotation)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Bon de commande:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(report.purchaseOrder)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Facture:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(report.invoice)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "Date de facturation:",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(report.invoiceDate?.format(dateTimeFormatter) ?: "Inconnu")
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
