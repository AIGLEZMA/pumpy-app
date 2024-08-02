package screenmodels

import DatabaseProvider
import Logger
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import models.User

class UsersScreenModel : ScreenModel {
    var users by mutableStateOf<List<User>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadUsers()
    }

    fun deleteUser(user: User) {
        Logger.debug("[User] Deleting user (username: ${user.username})...")
        screenModelScope.launch {
            val userDao = DatabaseProvider.getDatabase().userDao()
            userDao.delete(user)
            users = users.filter { it != user }
        }.invokeOnCompletion {
            Logger.debug("[User] Deleting user (username: ${user.username}) DONE")
        }
    }

    fun loadUsers() {
        Logger.debug("[User] Loading users...")
        screenModelScope.launch {
            isLoading = true
            val userDao = DatabaseProvider.getDatabase().userDao()
            users = userDao.getAllUsers()
            isLoading = false
        }.invokeOnCompletion {
            Logger.debug("[User] Loaded ${users.size} user(s)")
        }
    }
}