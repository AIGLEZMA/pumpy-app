package screens

import AccountIcon
import Theme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.Client
import screenmodels.ClientsScreenModel
import screenmodels.LoginScreenModel
import ui.DeleteConfirmationDialog
import ui.Layout
import ui.Loading

class ClientsScreen : Screen {

    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { ClientsScreenModel() }
        val loginScreenModel = navigator.rememberNavigatorScreenModel { LoginScreenModel() }

        var searchQuery by rememberSaveable { mutableStateOf("") }
        var clientToDelete by remember { mutableStateOf<Client?>(null) }

        LaunchedEffect(Unit) {
            screenModel.loadClients()
        }

        loginScreenModel.loginState
        val allClients = screenModel.clients
        val filteredClients = allClients.filter { client -> client.name.contains(searchQuery, ignoreCase = true) }

        val isLoading = screenModel.isLoading

        Layout(
            selected = "clients",
            onReportsClick = { navigator.push(ReportsScreen()) },
            onClientsClick = {},
            onUsersClick = { navigator.push(UsersScreen()) },
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onLogout = {
                loginScreenModel.logout()
                navigator.popUntilRoot()
            },
            isDarkMode = Theme.isDarkTheme,
            onToggleTheme = { Theme.toggleTheme() },
            onFabClick = { navigator.push(AddEditClientScreen()) },
            companyLabel = loginScreenModel.loginState.company.pretty
        ) {
            // TODO: no clients view
            if (isLoading) {
                Loading()
            } else {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))

                ) {
                    Title(clients = allClients)
                    Spacer(Modifier.height(20.dp))
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val state = rememberLazyListState()
                        ClientsList(
                            state = state,
                            clients = filteredClients,
                            onClientEditClick = {
                                navigator.push(AddEditClientScreen(it))
                            }, // TODO: permissions
                            onClientDeleteClick = { clientToDelete = it } // TODO: permissions
                        )
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(
                                scrollState = state
                            )
                        )
                    }
                    clientToDelete?.let { client ->
                        DeleteConfirmationDialog(
                            title = "Supprimer le client",
                            message = "Êtes-vous sûr de vouloir supprimer le client ${client.name}",
                            onConfirm = {
                                screenModel.deleteClient(client!!)
                                clientToDelete = null
                            },
                            onDismiss = { clientToDelete = null }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun Title(
        clients: List<Client>,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.padding(top = 30.dp, start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clients",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier.width(5.dp))
            Text(
                text = "(${clients.size})",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    @Composable
    fun ClientsList(
        state: LazyListState,
        clients: List<Client>,
        onClientEditClick: (Client) -> Unit,
        onClientDeleteClick: (Client) -> Unit,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(
            state = state,
            modifier = modifier.fillMaxSize()
        ) {
            items(clients) { client ->
                val firstLetter = client.name.firstOrNull()?.uppercase() ?: "?"
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    headlineContent = { Text(text = client.name, fontSize = 14.sp) },
                    supportingContent = { Text(client.phoneNumber) },
                    leadingContent = {
                        AccountIcon(firstLetter)
                    },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { onClientEditClick(client) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onClientDeleteClick(client) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}