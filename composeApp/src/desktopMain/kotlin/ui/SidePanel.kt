package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SidePanel(
    selectedItem: String,
    onItemClick: (String) -> Unit,
    onNewReportClick: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) {
        PermanentNavigationDrawer(
            modifier = Modifier.padding(top = 30.dp),
            drawerContent = {
                PermanentDrawerSheet(Modifier.width(210.dp)) {
                    Text("Magrinov", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = onNewReportClick,
                            //modifier = Modifier.fillMaxWidth(0.9f)
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Text("Nouveau rapport")
                        }
                        HorizontalDivider()
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                            label = { Text("Accueil") },
                            selected = "home" == selectedItem,
                            onClick = { onItemClick("home") },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.People, contentDescription = "Clients") },
                            label = { Text("Clients") },
                            selected = "clients" == selectedItem,
                            onClick = { onItemClick("clients") },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.Badge, contentDescription = "Users") },
                            label = { Text("Utilisateurs") },
                            selected = "users" == selectedItem,
                            onClick = { onItemClick("users") },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    content()
                }
            }
        )
    }
}

