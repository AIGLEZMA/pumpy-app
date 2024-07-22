package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var active by rememberSaveable { mutableStateOf(false) }

    Box(modifier.semantics { isTraversalGroup = true }) {
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter).semantics { traversalIndex = 0f },
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
}

// TODO: remove this text code
@Composable
fun TestSearchField() {
    var query by rememberSaveable { mutableStateOf("") }
    val allItems = listOf("Apple", "Banana", "Cherry", "Date", "Fig", "Grape")

    val filteredItems = allItems.filter { it.contains(query, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SearchField(
            query = query,
            onQueryChange = { newQuery ->
                query = newQuery
                println("Query changed to $newQuery")
            },
            onSearch = { searchQuery ->
                println("Search executed with query: $searchQuery")
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
