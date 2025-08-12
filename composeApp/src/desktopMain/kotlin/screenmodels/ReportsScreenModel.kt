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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Company
import models.Report
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class ReportsScreenModel : ScreenModel {
    var reports by mutableStateOf<List<Report>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> get() = _events

    // Toggle controlled by UI
    var autoOpenAfterSave by mutableStateOf(false)

    private fun osName() = System.getProperty("os.name").lowercase()

    init {
        loadReports()
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

    private fun tryDesktopAction(action: (Desktop) -> Unit): Boolean {
        return try {
            if (Desktop.isDesktopSupported()) {
                val d = Desktop.getDesktop()
                action(d)
                true
            } else false
        } catch (_: Throwable) {
            false
        }
    }

    private fun runCmd(vararg cmd: String): Boolean = try {
        ProcessBuilder(*cmd).start().waitFor(5, TimeUnit.SECONDS)
        true
    } catch (_: Throwable) {
        false
    }

    private fun openFile(path: Path) {
        val f = path.toFile()
        val done = tryDesktopAction { it.open(f) } || when {
            osName().contains("win") -> runCmd("cmd", "/c", "start", "", f.absolutePath)
            osName().contains("mac") -> runCmd("open", f.absolutePath)
            else -> runCmd("xdg-open", f.absolutePath)
        }
        if (!done) _events.tryEmit(UiEvent.Error("Impossible d’ouvrir le fichier."))
    }

    private fun printFile(path: Path) {
        val f = path.toFile()
        val done = tryDesktopAction { it.print(f) } ||      // May not show dialog, depends on OS
                runCmd("lp", f.absolutePath) ||          // *nix
                runCmd("lpr", f.absolutePath)            // *nix alt
        if (!done) _events.tryEmit(UiEvent.Error("Impossible d’imprimer le fichier."))
    }

    fun printReport(
        report: Report,
        clientUsername: String,
        creatorName: String,
        farmName: String,
        pumpName: String,
        company: Company
    ) {
        Logger.debug("[Report] Printing report (id: ${report.reportId})...")
        screenModelScope.launch {
            withContext(Dispatchers.IO) {
                // Generate to a temp PDF then send to printer
                val tmp = kotlin.io.path.createTempFile(
                    prefix = "Rapport_${report.reportId}_",
                    suffix = ".pdf"
                )

                try {
                    generateReport(
                        report = report,
                        clientUsername = clientUsername,
                        creatorName = creatorName,
                        farmName = farmName,
                        pumpName = pumpName,
                        company = company,
                        outputPath = tmp.toString()
                    )
                    printFile(tmp)
                    _events.tryEmit(UiEvent.Printed(tmp.toString()))
                } catch (e: Exception) {
                    Logger.error("[Report] Print error: ${e.message}", e)
                    _events.tryEmit(UiEvent.Error("Erreur d’impression : ${e.message}"))
                } finally {
                    // leave temp file around so user can reprint from viewer if desired
                }
            }
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
                    val suggested = "Rapport ${report.reportId}.pdf"
                    fileDialog.file = suggested
                    fileDialog.filenameFilter = FilenameFilter { _, name -> name.lowercase().endsWith(".pdf") }
                    fileDialog.isVisible = true

                    val dir = fileDialog.directory
                    var file = fileDialog.file

                    if (dir != null && file != null) {
                        if (!file.lowercase().endsWith(".pdf")) file += ".pdf"
                        val outputPath = Paths.get(dir, file)

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

                            // Lightweight validity check
                            val header = java.nio.file.Files.newInputStream(outputPath).use { ins ->
                                ByteArray(5).also { ins.read(it) }
                            }
                            if (String(header) != "%PDF-") {
                                Logger.debug("[Report] Saved file missing %PDF- header: $outputPath")
                            }

                            // Auto-open if toggled
                            if (autoOpenAfterSave) openFile(outputPath)

                            _events.tryEmit(UiEvent.Saved(outputPath.toString()))
                            Logger.debug("[Report] Saving pdf done: $outputPath")

                        } catch (e: Exception) {
                            Logger.error("[Report] Error saving file: ${e.message}", e)
                            _events.tryEmit(UiEvent.Error("Erreur d’enregistrement : ${e.message}"))
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

    // UI events for snackbars
    sealed class UiEvent {
        data class Saved(val path: String) : UiEvent()
        data class Error(val message: String) : UiEvent()
        data class Printed(val path: String) : UiEvent()
    }
}