package screenmodels

import DatabaseProvider
import Logger
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import models.*

class AddEditReportScreenModel(private val report: Report? = null) : ScreenModel {
    var state by mutableStateOf(ReportState())
        private set

    var selectedClient by mutableStateOf<Client?>(null)
    var selectedFarmName by mutableStateOf<String?>("")
    var selectedPumpName by mutableStateOf<String?>("")

    var executionOrder by mutableStateOf(report?.executionOrder)
    var requestDate by mutableStateOf(report?.requestDate)
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
    var elements by mutableStateOf(report?.elements)
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

    fun saveReport(loggedInUser: User) {
        screenModelScope.launch {
            val reportDao = DatabaseProvider.getDatabase().reportDao()
            val pumpDao = DatabaseProvider.getDatabase().pumpDao()
            val farmDao = DatabaseProvider.getDatabase().farmDao()

            val validationErrors = listOf(
                executionOrder to "Le bon d'exécution doit être rempli",
                requestDate to "La date de demande doit être remplie",
                workFinishDate to "La date de débit des travaux doit être remplie",
                type to "Le type doit être sélectionné",
                selectedClient to "Le client doit être sélectionné",
                purchaseRequest to "Le numéro de demande d'achat doit être rempli",
                purchaseOrder to "Le numéro de bon de commande doit être rempli",
                quotation to "Le numéro de devis doit être rempli",
                invoice to "Le numéro de facture doit être rempli"
            )

            validationErrors.forEach { (field, errorMessage) ->
                if (field == null) {
                    state = state.copy(errorMessage = errorMessage)
                    return@launch
                }
            }

            // Get or create the farm
            val farmId = if (!selectedFarmName.isNullOrEmpty()) {
                val existingFarms = farmDao.getFarmsByClientId(selectedClient!!.clientId)
                val existingFarm = existingFarms.find { it.name.equals(selectedFarmName, ignoreCase = true) }
                existingFarm?.farmId ?: farmDao.insert(
                    Farm(
                        name = selectedFarmName!!,
                        clientOwnerId = selectedClient!!.clientId
                    )
                )
            } else {
                throw IllegalArgumentException("Farm name cannot be empty.")
            }

            // Get or create the pump
            val pumpId = if (!selectedFarmName.isNullOrEmpty()) {
                val existingPumps = pumpDao.getPumpsByFarmId(farmId)
                val existingPump = existingPumps.find { it.name.equals(selectedPumpName, ignoreCase = true) }
                existingPump?.pumpId ?: pumpDao.insert(Pump(name = selectedPumpName!!, farmOwnerId = farmId))
            } else {
                throw IllegalArgumentException("Pump name cannot be empty.")
            }

            val newReport = Report(
                creatorId = loggedInUser.id,
                executionOrder = executionOrder!!.toLong(),
                requestDate = requestDate!!,
                workFinishDate = workFinishDate!!,
                pumpOwnerId = pumpId,
                operators = operators,
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
                purchaseRequest = purchaseRequest!!,
                purchaseOrder = purchaseOrder!!,
                invoiceDate = invoiceDate,
                invoice = invoice!!,
                quotation = quotation!!
            )

            if (state.isEditMode && report != null) {
                reportDao.update(newReport.copy(reportId = report.reportId))
            } else {
                reportDao.insert(newReport)

                Logger.debug("[Report] Inserted a new report: ")
                Logger.debug("[Report] $newReport")
            }

            state = state.copy(isSaved = true)
        }
    }

    data class ReportState(
        val errorMessage: String? = null,
        val isLoading: Boolean = false,
        val isEditMode: Boolean = false,
        val isSaved: Boolean = false,
    )
}