package screenmodels

import DatabaseProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import models.Client

class ClientsScreenModel : ScreenModel {
    var clients by mutableStateOf<List<Client>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadClients()
    }

    fun deleteClient(client: Client) {
        screenModelScope.launch {
            val clientDao = DatabaseProvider.getDatabase().clientDao()
            clientDao.delete(client)
            clients = clients.filter { it != client }
        }
    }

    private fun loadClients() {
        screenModelScope.launch {
            isLoading = true
            val clientDao = DatabaseProvider.getDatabase().clientDao()
            clients = clientDao.getAllClients()
            isLoading = false
        }
    }
}