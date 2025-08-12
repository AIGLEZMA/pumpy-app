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
import models.Company
import models.User

class LoginScreenModel : ScreenModel {
    var loginState by mutableStateOf(LoginState())
        private set

    fun login(username: String, password: String, company: Company) {
        Logger.debug("[Login] Logging in as $username")
        screenModelScope.launch {
            loginState = loginState.copy(isLoading = true)

            val userDao = DatabaseProvider.getDatabase().userDao()
            val user = userDao.getUserByUsername(username)

            if (user != null) {
                // Perform the CPU-intensive password verification on the Default dispatcher
                val isPasswordCorrect = withContext(Dispatchers.Default) {
                    Password.verify(password, user.password)
                }

                loginState = if (isPasswordCorrect) {
                    LoginState(user = user, isAuthenticated = true, company = company)
                } else {
                    LoginState(errorMessage = "Mot de passe incorrecte")
                }
            } else {
                loginState = LoginState(errorMessage = "Utilisateur non trouvé")
            }
        }.invokeOnCompletion {
            Logger.debug("[Login] Logging in as $username : ${if (loginState.isAuthenticated) "SUCCESS" else "FAILURE (${loginState.errorMessage})"} ")
        }
    }

    fun logout() {
        loginState = loginState.copy(
            user = null,
            isAuthenticated = false,
            errorMessage = null,
            isLoading = false,
            company = Company.UNKNOWN
        )
    }

    data class LoginState(
        val user: User? = null,
        val isAuthenticated: Boolean = false,
        val errorMessage: String? = null,
        val isLoading: Boolean = false,
        val company: Company = Company.UNKNOWN
    )
}