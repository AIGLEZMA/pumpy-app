package screenmodels

import DatabaseProvider
import Logger
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import models.Farm
import models.Report

class ReportsScreenModel : ScreenModel {
    var reports by mutableStateOf<List<Report>>(emptyList())
        private set

    var farms by mutableStateOf<List<Farm>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadFarms()
        loadReports()
    }

//    fun deleteUser(user: User) {
//        Logger.debug("[User] Deleting user (username: ${user.username})...")
//        screenModelScope.launch {
//            val userDao = DatabaseProvider.getDatabase().userDao()
//            userDao.delete(user)
//            users = users.filter { it != user }
//        }.invokeOnCompletion {
//            Logger.debug("[User] Deleting user (username: ${user.username}) DONE")
//        }
//    }

    fun loadFarms() {
        Logger.debug("[Farm] Loading farms...")
        screenModelScope.launch {
            isLoading = true
            val farmDao = DatabaseProvider.getDatabase().farmDao()
            farms = farmDao.getAllFarms()
            isLoading = false
        }.invokeOnCompletion {
            Logger.debug("[Farm] Loaded ${farms.size} farm(s)")
        }
    }

    fun loadReports() {
        Logger.debug("[Report] Loading reports...")
        screenModelScope.launch {
            isLoading = true
            val reportDao = DatabaseProvider.getDatabase().reportDao()
            reports = reportDao.getAllReports()
            isLoading = false
        }.invokeOnCompletion {
            Logger.debug("[Report] Loaded ${reports.size} report(s)")
        }
    }
}