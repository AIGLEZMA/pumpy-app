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

class LoginScreenModel : ScreenModel {
    var loginState by mutableStateOf(LoginState())
        private set

    fun login(username: String, password: String) {
        screenModelScope.launch {
            loginState = loginState.copy(isLoading = true)

            val userDao = DatabaseProvider.getDatabase().userDao()
            val user = userDao.getUserByUsername(username)
            loginState = if (user != null) {
                Logger.debug(
                    "Registered password hash: ${user.password}, provided password: $password with hash: ${
                        Password.hash(
                            password
                        )
                    }"
                )
                if (Password.verify(password, user.password)) {
                    LoginState(user = user, isAuthenticated = true)
                } else {
                    LoginState(errorMessage = "Mot de passe incorrecte")
                }
            } else {
                LoginState(errorMessage = "Utilisateur non trouvé")
            }
        }
    }

    fun logout() {
        loginState = loginState.copy(
            user = null,
            isAuthenticated = false,
            errorMessage = null,
            isLoading = false
        )
    }

    data class LoginState(
        val user: User? = null,
        val isAuthenticated: Boolean = false,
        val errorMessage: String? = null,
        val isLoading: Boolean = false
    )
}