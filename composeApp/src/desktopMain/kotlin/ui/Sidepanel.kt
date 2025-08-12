package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    companyLabel: String? = null,
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
            if (!companyLabel.isNullOrBlank()) {
                CompanyBadge(
                    label = companyLabel,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                )
            }
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

@Composable
private fun CompanyBadge(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // tiny dot
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(8.dp)
            ) {}
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

