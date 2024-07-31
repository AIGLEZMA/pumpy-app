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
import models.Report
import models.Report.OperationType
import models.User
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class AddEditReportScreenModel(private val report: Report? = null) : ScreenModel {
    var reportState by mutableStateOf(ReportState())
        private set

    init {
        if (report != null) {
            reportState = reportState.copy(

            )
        }
    }

    fun saveReport() {
        screenModelScope.launch {
            val reportDao = DatabaseProvider.getDatabase().userDao()
            if (username.isEmpty() || password.isEmpty()) {
                userState = userState.copy(
                    errorMessage = "Veuillez préciser un nom d'utilisateur et un mot de passe"
                )
                return@launch
            }
            if (userState.isEditMode) {
                if (user == null) {
                    Logger.debug("Attempted to save user but the user instance is null ($username)")
                    return@launch
                }
                userDao.update(user.copy(username = username, password = Password.hash(password)))
                Logger.debug("Update user (username: $username)")
            } else {
                val newUser = User(username = username, password = Password.hash(password))
                userDao.insert(newUser)
                Logger.debug("Inserting new user (username: $username)")
            }
            userState = userState.copy(isSaved = true)
        }
    }

    data class ReportState(
        val executionOrder: Long = 0L,
        val requestDate: LocalDate = LocalDate.now(),
        val workFinishDate: LocalDate = LocalDate.now().plusDays(1),
        val pumpOwnerId: Long = 0L,
        val operators: List<String> = emptyList(),
        val type: OperationType,
        val depth: Long?,
        val staticLevel: Long?,
        val dynamicLevel: Long?,
        val pumpShimming: Long?,
        val speed: Float?,
        val engine: String?,
        val pump: String?,
        val elements: String?,
        val notes: String?,
        val quotation: Long,
        val invoice: Long,
        val errorMessage: String? = null,
        val isEditMode: Boolean = false,
        val isSaved: Boolean = false
    )