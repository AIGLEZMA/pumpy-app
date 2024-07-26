package screens

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import screenmodels.UsersScreenModel
import ui.Header

class UsersScreen : Screen {

    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.rememberNavigatorScreenModel { UsersScreenModel() }

        var query by rememberSaveable { mutableStateOf("") }
        var isDarkMode by rememberSaveable { mutableStateOf(false) }

        val allUsers = screenModel.users
        val filteredUsers = allUsers.filter { user -> user.username.contains(query, ignoreCase = true) }
        var isLoading = screenModel.isLoading

//        val allItems = listOf("Apple", "Banana", "Cherry", "Date", "Fig", "Grape")
//        val filteredItems = allItems.filter { it.contains(query, ignoreCase = true) }

        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(
                    modifier = Modifier.width(260.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "MAGRINOV",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W500,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        )
                        Spacer(Modifier.height(24.dp))
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.Description, contentDescription = "Home") },
                            badge = { Icon(Icons.Default.Add, contentDescription = "Add") },
                            label = { Text("Accueil") },
                            selected = false,
                            onClick = { },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        Spacer(Modifier.height(8.dp))
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.People, contentDescription = "Clients") },
                            label = { Text("Clients") },
                            selected = false,
                            onClick = { },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        Spacer(Modifier.height(8.dp))
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.Badge, contentDescription = "Users") },
                            label = { Text("Utilisateurs") },
                            selected = true,
                            onClick = { },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    Header(
                        query = query,
                        onQueryChange = { newQuery ->
                            query = newQuery
                            println("Query changed to $newQuery")
                        },
                        onSearch = { searchQuery ->
                            println("Search executed with query: $searchQuery")
                        },
                        onLogout = { println("Logged out") },
                        isDarkMode = isDarkMode,
                        onToggleTheme = {
                            isDarkMode = !isDarkMode
                            println("Theme toggled to ${if (isDarkMode) "Dark Mode" else "Light Mode"}")
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add"
                        )
                        Text(text = "Nouveau")
                    }
                }
            ) { paddingValues ->
                Surface(modifier = Modifier.padding(paddingValues)) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))

                    ) {
                        Text(
                            text = "Utilisateurs (1)",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 30.dp, start = 16.dp)
                        )
                        Spacer(Modifier.height(20.dp))
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredUsers) { user ->
                                val firstLetter = user.username.firstOrNull()?.uppercase() ?: "?"
                                ListItem(
                                    overlineContent = { Text("Vous") }, // TODO: check if it's the logged in user
                                    headlineContent = { Text(text = user.username) },
                                    supportingContent = { if (user.isAdmin) Text("Administrateur") else Text("Normal") },
                                    leadingContent = {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    MaterialTheme.colorScheme.primary
                                                )
                                        ) {
                                            Text(
                                                text = firstLetter,
                                                color = Color.White,
                                                textAlign = TextAlign.Center,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    },
                                    trailingContent = {
                                        Row {
                                            IconButton(onClick = { }) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(onClick = { }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                HorizontalDivider()
                                // Scroll in Material3 + Desktop
                            }
                        }
                    }
                }

            }
        }
    }
}