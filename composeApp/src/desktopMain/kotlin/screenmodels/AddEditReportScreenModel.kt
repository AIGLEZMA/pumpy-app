package screenmodels

import DatabaseProvider
import Logger
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import models.Client
import models.Company
import models.Report
import models.User

class AddEditReportScreenModel(private val report: Report? = null) : ScreenModel {
    var state by mutableStateOf(ReportState())
        private set

    var selectedClient by mutableStateOf<Client?>(null)

    var asker by mutableStateOf(report?.asker)
    var wellDrilling by mutableStateOf(report?.wellDrilling)
    var executionOrder by mutableStateOf(report?.executionOrder)
    var requestDate by mutableStateOf(report?.requestDate)
    var workStartDate by mutableStateOf(report?.workStartDate)
    var workFinishDate by mutableStateOf(report?.workFinishDate)
    var operators by mutableStateOf(report?.operators?.toMutableStateList() ?: mutableStateListOf())
    var type by mutableStateOf(report?.type)
    var depth by mutableStateOf(report?.depth)
    var staticLevel by mutableStateOf(report?.staticLevel)
    var dynamicLevel by mutableStateOf(report?.dynamicLevel)
    var pumpShimming by mutableStateOf(report?.pumpShimming)
    var speed by mutableStateOf(report?.speed)
    var engine by mutableStateOf(report?.engine)
    var pump by mutableStateOf(report?.pump)
    var elements by mutableStateOf(report?.elements?.toMutableStateList() ?: mutableStateListOf())
    var notes by mutableStateOf(report?.notes)
    var purchaseRequest by mutableStateOf(report?.purchaseRequest)
    var quotation by mutableStateOf(report?.quotation)
    var purchaseOrder by mutableStateOf(report?.purchaseOrder)
    var invoice by mutableStateOf(report?.invoice)
    var invoiceDate by mutableStateOf(report?.invoiceDate)

    init {
        report?.let {
            state = state.copy(isEditMode = true)
        }
    }

    fun addOperator() {
        operators.add("")
    }

    fun updateOperator(index: Int, newOperator: String) {
        if (index in operators.indices) {
            operators[index] = newOperator
        }
    }

    fun removeOperator(index: Int) {
        if (index in operators.indices) {
            operators.removeAt(index)
        }
    }

    fun addElement() {
        elements.add("")
    }

    fun updateElement(index: Int, newElement: String) {
        if (index in elements.indices) {
            elements[index] = newElement
        }
    }

    fun removeElement(index: Int) {
        if (index in elements.indices) {
            elements.removeAt(index)
        }
    }

    private fun firstValidationError(): String? = when {
        executionOrder == null -> "Le bon d'exécution doit être rempli."
        requestDate == null -> "La date de demande doit être remplie."
        workStartDate == null -> "La date de début des travaux doit être remplie."
        workFinishDate == null -> "La date de fin des travaux doit être remplie."
        // If your dates are Comparable (e.g., LocalDate), keep this check
        workStartDate != null && workFinishDate != null && workFinishDate!! < workStartDate!! ->
            "La date de fin des travaux doit être postérieure ou égale à la date de début."

        type == null -> "Le type doit être sélectionné."
        selectedClient == null -> "Le client doit être sélectionné."
        asker.isNullOrBlank() -> "Le nom du demandeur doit être rempli."
        wellDrilling.isNullOrBlank() -> "Le nom du forage doit être rempli."
        purchaseRequest.isNullOrBlank() -> "Le numéro de demande d'achat doit être rempli."
        quotation.isNullOrBlank() -> "Le numéro de devis doit être rempli."
        purchaseOrder.isNullOrBlank() -> "Le numéro de bon de commande doit être rempli."
        invoice.isNullOrBlank() -> "Le numéro de facture doit être rempli."
        else -> null
    }

    fun saveReport(loggedInUser: User, company: Company) {
        screenModelScope.launch {
            // 1) Validate
            val error = firstValidationError()
            if (error != null) {
                state = state.copy(errorMessage = error)
                return@launch
            }

            // 2) Safe to build
            val newReport = Report(
                creatorId = loggedInUser.id,
                executionOrder = executionOrder!!.toLong(),
                requestDate = requestDate!!,
                workStartDate = workStartDate!!,
                workFinishDate = workFinishDate!!,
                clientOwnerId = selectedClient!!.clientId,
                operators = operators,
                type = type!!,
                company = company,
                wellDrilling = wellDrilling!!, // "forage/pompe"
                asker = asker!!,
                staticLevel = staticLevel,
                dynamicLevel = dynamicLevel,
                pumpShimming = pumpShimming,
                speed = speed,
                engine = engine,
                pump = pump,
                elements = elements,
                depth = depth,
                notes = notes,
                purchaseRequest = purchaseRequest!!,
                purchaseOrder = purchaseOrder!!,
                invoiceDate = invoiceDate,
                invoice = invoice!!,
                quotation = quotation!!
            )

            // 3) Persist
            val reportDao = DatabaseProvider.getDatabase().reportDao()
            if (state.isEditMode && report != null) {
                reportDao.update(newReport.copy(reportId = report.reportId))
            } else {
                reportDao.insert(newReport)
                Logger.debug("[Report] Inserted a new report:")
                Logger.debug("[Report] $newReport")
            }

            state = state.copy(errorMessage = null, isSaved = true)
        }
    }

    data class ReportState(
        val errorMessage: String? = null,
        val isLoading: Boolean = false,
        val isEditMode: Boolean = false,
        val isSaved: Boolean = false,
    )
}