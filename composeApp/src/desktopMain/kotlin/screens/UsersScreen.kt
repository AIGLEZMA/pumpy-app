package screens

import Logger
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.User
import screenmodels.LoginScreenModel
import screenmodels.UsersScreenModel
import ui.Layout
import ui.Loading

class UsersScreen : Screen {

    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.rememberNavigatorScreenModel { UsersScreenModel() }
        val loginScreenModel = navigator.rememberNavigatorScreenModel { LoginScreenModel() }

        var searchQuery by rememberSaveable { mutableStateOf("") }
        var isDarkMode by rememberSaveable { mutableStateOf(false) }
        var userToDelete by remember { mutableStateOf<User?>(null) }

        val loginState = loginScreenModel.loginState
        val allUsers = screenModel.users
        // Dummy data for testing
//        val allUsers = remember {
//            mutableStateListOf(
//                User(1, "John Doe", "", true),
//                User(2, "Jane Doe", "", false),
//                User(3, "Adam Cena", "", true),
//                User(4, "Mohammed Ali", "", false),
//                User(5, "Jesus Biden", "", true),
//                User(6, "Jesus Biden", "", false),
//                User(7, "Alma Hose", "", false),
//                User(8, "Sara Trump", "", false),
//                User(9, "Ali Amine", "", false),
//                User(10, "Cristiano Messi", "", false),
//                User(11, "Lionel Ronaldo", "", false)
//            )
//        }
        val filteredUsers = allUsers.filter { user -> user.username.contains(searchQuery, ignoreCase = true) }
        val sortedUsers = filteredUsers.sortedWith(compareBy({ it.id != loginState.user?.id }, { it.username }))

        val isLoading = screenModel.isLoading

        Layout(
            selected = "users",
            onReportsClick = { navigator.push(LoginScreen()) },
            onClientsClick = {},
            onUsersClick = {},
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onLogout = {
                loginScreenModel.logout()
                navigator.popUntilRoot()
            },
            isDarkMode = isDarkMode, // TODO
            onToggleTheme = {}, // TODO
            onFabClick = { navigator.push(AddEditUserScreen()) }
        ) {
            if (isLoading) {
                Loading()
            } else {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))

                ) {
                    Title(sortedUsers = sortedUsers)
                    Spacer(Modifier.height(20.dp))
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val state = rememberLazyListState()
                        UsersList(
                            state = state,
                            loggedInUser = loginState.user,
                            sortedUsers = sortedUsers,
                            onUserEditClick = {
                                Logger.debug("Moving from Users screen into Edit User screen")
                                navigator.push(AddEditUserScreen(it))
                            }, // TODO: permissions
                            onUserDeleteClick = { userToDelete = it } // TODO: permissions
                        )
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(
                                scrollState = state
                            )
                        )
                    }
                    userToDelete?.let { user ->
                        DeleteConfirmationDialog(
                            user = user,
                            onConfirm = {
                                screenModel.deleteUser(userToDelete!!)
                                userToDelete = null
                            },
                            onDismiss = { userToDelete = null }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun Title(
        sortedUsers: List<User>,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.padding(top = 30.dp, start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Utilisateurs",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier.width(5.dp))
            Text(
                text = "(${sortedUsers.size})",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    @Composable
    fun UsersList(
        state: LazyListState,
        loggedInUser: User?,
        sortedUsers: List<User>,
        onUserEditClick: (User) -> Unit,
        onUserDeleteClick: (User) -> Unit,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(
            state = state,
            modifier = modifier.fillMaxSize()
        ) {
            items(sortedUsers) { user ->
                val firstLetter = user.username.firstOrNull()?.uppercase() ?: "?"
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    overlineContent = {
                        if (loggedInUser != null && user.id == loggedInUser.id) Text(
                            text = "Vous", fontSize = 12.sp
                        )
                    },
                    headlineContent = { Text(text = user.username, fontSize = 14.sp) },
                    supportingContent = { if (user.isAdmin) Text("Administrateur") else Text("Normal") },
                    leadingContent = {
                        AccountIcon(firstLetter)
                    },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { onUserEditClick(user) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onUserDeleteClick(user) }) {
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

    @Composable
    fun AccountIcon(
        firstLetter: String,
        modifier: Modifier = Modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primary
                )
        ) {
            Text(
                text = firstLetter,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    @Composable
    fun DeleteConfirmationDialog(
        user: User,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(text = "Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Annuler")
                }
            },
            title = { Text("Supprimer l'utilisateur") },
            text = { Text("Êtes-vous sûr de vouloir supprimer l'utilisateur ${user.username}?") }
        )
    }
}