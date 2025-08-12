package ui

import Logger
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Header(
    query: String,
    onQueryChange: (String) -> Unit,
    onLogout: () -> Unit,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    autoOpenAfterSave: Boolean,
    onAutoOpenAfterSaveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
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
            modifier = Modifier.weight(1f)
                .height(56.dp)
                .padding(horizontal = 8.dp)

        )

        Spacer(modifier = Modifier.width(8.dp))

        AccountMenu(onLogout = onLogout)

        Spacer(modifier = Modifier.width(8.dp))

        SettingsMenu(
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme,
            autoOpenAfterSave = autoOpenAfterSave,
            onAutoOpenAfterSaveChange = onAutoOpenAfterSaveChange
        )
    }
}

private val iconSize = 26.dp

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
fun SettingsMenu(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    autoOpenAfterSave: Boolean,
    onAutoOpenAfterSaveChange: (Boolean) -> Unit
) {
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
            // Theme toggle (existing)
            DropdownMenuItem(
                text = { Text(if (isDarkMode) "Mode clair" else "Mode sombre") },
                onClick = {
                    onToggleTheme()
                    expanded = false
                }
            )

            Divider()

            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ouvrir le PDF après enregistrement", modifier = Modifier.weight(1f))
                        // Switch inside the menu; keeps UX clear & compact
                        Switch(
                            checked = autoOpenAfterSave,
                            onCheckedChange = { checked ->
                                onAutoOpenAfterSaveChange(checked)
                                // Keep menu open after toggle for nicer UX
                            }
                        )
                    }
                },
                // Also toggle if the row itself is clicked
                onClick = {
                    onAutoOpenAfterSaveChange(!autoOpenAfterSave)
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchBar(
        modifier = modifier,
        placeholder = { Text("Rechercher...") },
        onSearch = {
            Logger.debug("On search called for $query")
        },
        query = query,
        active = false,
        onActiveChange = { },
        onQueryChange = {
            Logger.debug("Query changed from $query to $it")
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