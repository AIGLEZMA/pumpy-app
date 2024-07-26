package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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

private val iconSize = 30.dp

@Composable
fun AccountMenu(onLogout: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Outlined.AccountCircle,
                contentDescription = "Account",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
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
                modifier = Modifier.size(iconSize)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var active by rememberSaveable { mutableStateOf(false) }

    SearchBar(
        modifier = modifier,
        placeholder = { Text("Rechercher...") },
        onSearch = {
            onSearch(query)
            println("On search called for $query")
        },
        query = query,
        active = false,
        onActiveChange = {
            active = false
            println("On active change to $active")
        },
        onQueryChange = {
            println("Query changed from $query to $it")
            onQueryChange(it)
        },
        leadingIcon = { Icon(Icons.Default.Search, "Search") },
        trailingIcon = {
            IconButton(
                onClick = { onQueryChange("") },
                content = { Icon(Icons.Default.Close, "Close") }
            )
        }
    ) {

    }
}