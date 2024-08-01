package screenmodels

import DatabaseProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import models.Report

class AddEditReportScreenModel(private val report: Report? = null) : ScreenModel {
    var state by mutableStateOf(ReportState())
        private set

    var executionOrder by mutableStateOf(report?.executionOrder)
    var requestDate by mutableStateOf(report?.requestDate)
    var workFinishDate by mutableStateOf(report?.workFinishDate)
    var pumpOwnerId by mutableStateOf(report?.pumpOwnerId)
    var operators by mutableStateOf(report?.operators)
    var type by mutableStateOf(report?.type)
    var depth by mutableStateOf(report?.depth)
    var staticLevel by mutableStateOf(report?.staticLevel)
    var dynamicLevel by mutableStateOf(report?.dynamicLevel)
    var pumpShimming by mutableStateOf(report?.pumpShimming)
    var speed by mutableStateOf(report?.speed)
    var engine by mutableStateOf(report?.engine)
    var pump by mutableStateOf(report?.pump)
    var elements by mutableStateOf(report?.elements)
    var notes by mutableStateOf(report?.notes)
    var quotation by mutableStateOf(report?.quotation)
    var invoice by mutableStateOf(report?.invoice)

    init {
        report?.let {
            state = state.copy(isEditMode = true)
        }
    }

    fun saveReport() {
        screenModelScope.launch {
            val reportDao = DatabaseProvider.getDatabase().reportDao()

            if (executionOrder == null || requestDate == null || workFinishDate == null
                || pumpOwnerId == null || operators == null || type == null
                || quotation == null || invoice == null
            ) {
                state = state.copy(
                    errorMessage = "All required fields must be filled in"
                )
                return@launch
            }

            val newReport = Report(
                executionOrder = executionOrder!!.toLong(),
                requestDate = requestDate!!,
                workFinishDate = workFinishDate!!,
                pumpOwnerId = pumpOwnerId!!,
                operators = operators!!,
                type = type!!,
                depth = depth,
                staticLevel = staticLevel,
                dynamicLevel = dynamicLevel,
                pumpShimming = pumpShimming,
                speed = speed,
                engine = engine,
                pump = pump,
                elements = elements,
                notes = notes,
                quotation = quotation!!,
                invoice = invoice!!
            )

            if (state.isEditMode && report != null) {
                reportDao.update(newReport.copy(reportId = report.reportId))
            } else {
                reportDao.insert(newReport)
            }

            state = state.copy(isSaved = true)
        }
    }

    data class ReportState(
        val errorMessage: String? = null,
        val isEditMode: Boolean = false,
        val isSaved: Boolean = false
    )
}