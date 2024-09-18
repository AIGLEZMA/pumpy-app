package screenmodels

import DatabaseProvider
import Logger
import ReportPdf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import models.Client
import models.Farm
import models.Pump
import models.Report
import java.nio.file.Paths

class ReportsScreenModel : ScreenModel {
    var reports by mutableStateOf<List<Report>>(emptyList())
        private set

    var farms by mutableStateOf<List<Farm>>(emptyList())
        private set

    var pumps by mutableStateOf<List<Pump>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadFarms()
        loadPumps()
        loadReports()
    }

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

    fun loadPumps() {
        Logger.debug("[Pump] Loading pumps...")
        screenModelScope.launch {
            isLoading = true
            val pumpDao = DatabaseProvider.getDatabase().pumpDao()
            pumps = pumpDao.getAllPumps()
            isLoading = false
        }.invokeOnCompletion {
            Logger.debug("[Pump] Loaded ${pumps.size} pump(s)")
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

    fun fetchFarmNames(client: Client): List<String> {
        return farms.filter { it.clientOwnerId == client.clientId }.map { it.name }
    }

    fun fetchPumpNames(farmName: String): List<String> {
        val farm = farms.firstOrNull { it.name.equals(farmName, true) }
        if (farm == null) {
            return emptyList()
        }
        return pumps.filter { it.pumpId == farm.farmId }.map { it.name }
    }

    fun deleteReport(report: Report) {
        Logger.debug("[Report] Deleting report (id: ${report.reportId})...")
        screenModelScope.launch {
            val reportDao = DatabaseProvider.getDatabase().reportDao()
            reportDao.delete(report)
            reports = reports.filter { it != report }
        }.invokeOnCompletion {
            Logger.debug("[Report] Deleting report (id: ${report.reportId}) DONE")
        }
    }

    fun savePdf(
        report: Report,
        clientUsername: String,
        creatorName: String,
        farmName: String,
        pumpName: String
    ) {
        Logger.debug("[Report] Saving pdf of a report (id: ${report.reportId}) ...")
        screenModelScope.launch {
            ReportPdf.generateAndSave(
                report = report,
                clientUsername = clientUsername,
                creatorName = creatorName,
                farmName = farmName,
                pumpName = pumpName,
                Paths.get(System.getProperty("user.home"), "test.pdf")
            )
        }.invokeOnCompletion {
            Logger.debug("[Report] Saving pdf done!")
        }
    }
}