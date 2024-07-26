package screenmodels

import DatabaseProvider
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
        screenModelScope.launch {
            val userDao = DatabaseProvider.getDatabase().userDao()
            userDao.delete(user)
            users = users.filter { it != user }
        }
    }

    private fun loadUsers() {
        screenModelScope.launch {
            isLoading = true
            val userDao = DatabaseProvider.getDatabase().userDao()
            users = userDao.getAllUsers()
            isLoading = false
        }
    }
}