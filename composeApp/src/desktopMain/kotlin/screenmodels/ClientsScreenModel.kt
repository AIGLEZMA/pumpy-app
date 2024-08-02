package screenmodels

import DatabaseProvider
import Logger
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
        Logger.debug("[Client] Deleting client (name: ${client.name})...")
        screenModelScope.launch {
            val clientDao = DatabaseProvider.getDatabase().clientDao()
            clientDao.delete(client)
            clients = clients.filter { it != client }
        }.invokeOnCompletion {
            Logger.debug("[Client] Deleting client (name: ${client.name}) DONE")
        }
    }

    fun loadClients() {
        Logger.debug("[Client] Loading clients...")
        screenModelScope.launch {
            isLoading = true
            val clientDao = DatabaseProvider.getDatabase().clientDao()
            clients = clientDao.getAllClients()
            isLoading = false
        }.invokeOnCompletion {
            Logger.debug("[Client] Loaded ${clients.size} client(s)")
        }
    }
}