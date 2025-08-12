package screenmodels

import DatabaseProvider
import Logger
import Password
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.User

class AddEditUserScreenModel(private val user: User? = null) : ScreenModel {
    var userState by mutableStateOf(UserState())
        private set

    init {
        Logger.debug("[User] New ${if (user != null) "edit" else "add"} user screen model")
        if (user != null) {
            userState = userState.copy(
                username = user.username,
                password = "",
                isEditMode = true
            )
        }
    }

    fun saveUser(username: String, password: String) {
        screenModelScope.launch {
            val userDao = DatabaseProvider.getDatabase().userDao()
            if (username.isEmpty() || password.isEmpty()) {
                userState = userState.copy(
                    errorMessage = "Veuillez préciser un nom d'utilisateur et un mot de passe"
                )
                return@launch
            }

            // Move the CPU-intensive password hashing to a default dispatcher
            val hashedPassword = withContext(Dispatchers.Default) {
                Password.hash(password)
            }

            if (userState.isEditMode) {
                if (user == null) {
                    Logger.debug("[User] Attempted to save user but the user instance is null (username: $username)")
                    return@launch
                }
                userDao.update(user.copy(username = username, password = hashedPassword))
                Logger.debug("[User] Updated user (username: $username)")
            } else {
                val newUser = User(username = username, password = hashedPassword)
                userDao.insert(newUser)
                Logger.debug("[User] Adding new user (username: $username)")
            }
            userState = userState.copy(isSaved = true)
        }
    }

    data class UserState(
        val username: String = "",
        val password: String = "",
        val errorMessage: String? = null,
        val isEditMode: Boolean = false,
        val isSaved: Boolean = false
    )
}