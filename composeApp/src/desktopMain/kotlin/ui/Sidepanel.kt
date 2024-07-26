package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SidepanelContent(
    selected: String,
    onReportsClick: () -> Unit,
    onClientsClick: () -> Unit,
    onUsersClick: () -> Unit,
    modifier: Modifier
) {
    PermanentDrawerSheet(
        modifier = modifier.width(260.dp)
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
                icon = { Icon(Icons.Outlined.Description, contentDescription = "Reports") },
                badge = { Icon(Icons.Default.Add, contentDescription = "Add") },
                label = { Text("Rapports") },
                selected = selected == "reports",
                onClick = onReportsClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            Spacer(Modifier.height(8.dp))
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.People, contentDescription = "Clients") },
                label = { Text("Clients") },
                selected = selected == "clients",
                onClick = onClientsClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            Spacer(Modifier.height(8.dp))
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.Badge, contentDescription = "Users") },
                label = { Text("Utilisateurs") },
                selected = selected == "users",
                onClick = onUsersClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

