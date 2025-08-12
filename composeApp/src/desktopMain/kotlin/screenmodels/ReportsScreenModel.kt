package screenmodels

import DatabaseProvider
import Logger
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import generateReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.*
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
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
        pumpName: String,
        company: Company
    ) {
        Logger.debug("[Report] Saving pdf of a report (id: ${report.reportId}) ...")
        screenModelScope.launch {
            withContext(Dispatchers.IO) {
                val parentFrame = Frame()
                try {
                    val fileDialog = FileDialog(parentFrame, "Sauvegarder le rapport", FileDialog.SAVE)

                    // Suggest a default name with .pdf
                    val suggested = "Rapport ${report.reportId}.pdf"
                    fileDialog.file = suggested

                    // (Note: FilenameFilter here does not enforce the extension on SAVE)
                    fileDialog.filenameFilter = FilenameFilter { _, name -> name.lowercase().endsWith(".pdf") }

                    fileDialog.isVisible = true

                    val dir = fileDialog.directory
                    var file = fileDialog.file

                    if (dir != null && file != null) {
                        // Ensure .pdf extension
                        if (!file.lowercase().endsWith(".pdf")) file += ".pdf"

                        val outputPath = Paths.get(dir, file)

                        // Optional: confirm overwrite if exists
                        if (java.nio.file.Files.exists(outputPath)) {
                            Logger.debug("[Report] Overwriting existing file: $outputPath")
                            // You can add a UI confirmation here if you want
                        }

                        try {
                            generateReport(
                                report = report,
                                clientUsername = clientUsername,
                                creatorName = creatorName,
                                farmName = farmName,
                                pumpName = pumpName,
                                company = company,
                                outputPath = outputPath.toString()
                            )

                            // Optional sanity check: does file start with %PDF- ?
                            val header = java.nio.file.Files.newInputStream(outputPath).use { ins ->
                                ByteArray(5).also { ins.read(it) }
                            }
                            val isPdf = String(header) == "%PDF-"
                            if (!isPdf) {
                                Logger.debug("[Report] Saved file does not appear to be a valid PDF (missing %PDF- header): $outputPath")
                            }

                            Logger.debug("[Report] Saving pdf done: $outputPath")
                            // TODO: UI toast/snackbar: "Rapport enregistré : $outputPath"
                        } catch (e: Exception) {
                            Logger.error("[Report] Error saving file: ${e.message}", e)
                            // TODO: UI error notification
                        }
                    } else {
                        Logger.debug("[Report] Save operation cancelled by user.")
                    }
                } finally {
                    parentFrame.dispose()
                }
            }
        }
    }

}