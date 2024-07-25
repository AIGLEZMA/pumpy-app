package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Header(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onLogout: () -> Unit,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        AccountMenu(onLogout = onLogout)

        Spacer(modifier = Modifier.width(8.dp))

        SettingsMenu(isDarkMode = isDarkMode, onToggleTheme = onToggleTheme)
    }
}

@Composable
fun AccountMenu(onLogout: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Outlined.AccountCircle,
                contentDescription = "Account",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = "Se déconnecter", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    onLogout()
                    expanded = false
                })
        }
    }
}

@Composable
fun SettingsMenu(isDarkMode: Boolean, onToggleTheme: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (isDarkMode) "Mode clair" else "Mode sombre") },
                onClick = {
                    onToggleTheme()
                    expanded = false
                })
        }
    }
}

@Preview
@Composable
fun TestHeaderScreen() {
    var query by rememberSaveable { mutableStateOf("") }
    val allItems = listOf("Apple", "Banana", "Cherry", "Date", "Fig", "Grape")
    val filteredItems = allItems.filter { it.contains(query, ignoreCase = true) }
    var isDarkMode by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredItems) { item ->
                Text(item, modifier = Modifier.padding(8.dp))
            }
        }
    }
}