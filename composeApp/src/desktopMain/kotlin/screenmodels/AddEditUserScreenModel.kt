package screenmodels

import DatabaseProvider
import Logger
import Password
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import models.User

class AddEditUserScreenModel(private val userId: Long? = null) : ScreenModel {
    var userState by mutableStateOf(UserState())
        private set

    init {
        if (userId != null) {
            loadUser(userId)
        }
    }

    private fun loadUser(id: Long) {
        screenModelScope.launch {
            val userDao = DatabaseProvider.getDatabase().userDao()
            val user = userDao.getUserById(id)
            userState = userState.copy(
                username = user!!.username,
                password = "",
                isEditMode = true
            )
        }
    }

    fun saveUser(username: String, password: String) {
        screenModelScope.launch {
            val userDao = DatabaseProvider.getDatabase().userDao()
            if (userState.isEditMode) {
                val user = userDao.getUserById(userId!!)
                userDao.update(user!!.copy(username = username, password = Password.hash(password)))
                Logger.debug("Update user (id: $userId) (username: $username)")
            } else {
                val newUser = User(username = username, password = Password.hash(password))
                userDao.insert(newUser)
                Logger.debug("Inserting new user (username: $username)")
            }
            userState = userState.copy(isSaved = true)
        }
    }

    data class UserState(
        val username: String = "",
        val password: String = "",
        val isEditMode: Boolean = false,
        val isSaved: Boolean = false
    )
}
