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
import models.Company

class AddEditClientScreenModel(private val client: Client? = null) : ScreenModel {
    var clientState by mutableStateOf(ClientState())
        private set

    init {
        Logger.debug("[Client] New ${if (client != null) "edit" else "add"} client screen model")
        if (client != null) {
            clientState = clientState.copy(
                name = client.name,
                phoneNumber = client.phoneNumber,
                location = client.location,
                isEditMode = true
            )
        }
    }

    fun saveClient(name: String, phoneNumber: String, location: String, company: Company) {
        screenModelScope.launch {
            val clientDao = DatabaseProvider.getDatabase().clientDao()
            if (name.isEmpty() || phoneNumber.isEmpty() || location.isEmpty()) {
                clientState = clientState.copy(
                    errorMessage = "Veuillez préciser le nom, le numéro de téléphone et la localisation du client"
                )
                return@launch
            }
            if (clientState.isEditMode) {
                if (client == null) {
                    Logger.debug("[Client] Attempted to save client but the client instance is null (name: $name)")
                    return@launch
                }
                clientDao.update(
                    client.copy(
                        name = name,
                        phoneNumber = phoneNumber,
                        location = location,
                        company = company
                    )
                )
                Logger.debug("[Client] Updated client (name: $name)")
            } else {
                val newClient = Client(name = name, phoneNumber = phoneNumber, location = location, company = company)
                clientDao.insert(newClient)
                Logger.debug("[Client] Adding new client (name: $name)")
            }
            clientState = clientState.copy(isSaved = true)
        }
    }

    data class ClientState(
        val name: String = "",
        val phoneNumber: String = "",
        val location: String = "",
        val errorMessage: String? = null,
        val isEditMode: Boolean = false,
        val isSaved: Boolean = false
    )
}
