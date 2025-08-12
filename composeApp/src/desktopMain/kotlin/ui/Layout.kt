package ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Layout(
    selected: String,
    onReportsClick: () -> Unit,
    onClientsClick: () -> Unit,
    onUsersClick: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onLogout: () -> Unit,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onFabClick: () -> Unit,
    companyLabel: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    PermanentNavigationDrawer(
        drawerContent = {
            SidepanelContent(
                selected, onReportsClick, onClientsClick, onUsersClick, companyLabel, modifier
            )
        }
    ) {
        Scaffold(
            topBar = {
                Header(
                    query = query,
                    onQueryChange = onQueryChange,
                    onLogout = onLogout,
                    isDarkMode = isDarkMode,
                    onToggleTheme = onToggleTheme,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(onClick = onFabClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                    Text(text = "Nouveau")
                }
            }
        ) { paddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {
                content()
            }
        }
    }
}