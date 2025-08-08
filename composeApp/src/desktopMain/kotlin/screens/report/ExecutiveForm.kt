package screens.report

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ui.DatePickerAndTextField
import java.time.LocalDate

@Composable
fun ExecutiveForm(
    purchaseRequest: String?,
    onPurchaseRequestChange: (String) -> Unit,
    quotation: String?,
    onQuotationChange: (String) -> Unit,
    purchaseOrder: String?,
    onPurchaseOrderChange: (String) -> Unit,
    invoice: String?,
    onInvoiceChange: (String) -> Unit,
    invoiceDate: LocalDate?,
    onInvoiceDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = purchaseRequest ?: "",
        label = { Text("Numéro D.A") },
        singleLine = true,
        onValueChange = { onPurchaseRequestChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = quotation ?: "",
        label = { Text("Numéro de Devis") },
        singleLine = true,
        onValueChange = { onQuotationChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = purchaseOrder ?: "",
        label = { Text("Bon de commande") },
        singleLine = true,
        onValueChange = { onPurchaseOrderChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = invoice ?: "",
        label = { Text("Facture") },
        singleLine = true,
        onValueChange = { onInvoiceChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    DatePickerAndTextField(
        value = invoiceDate,
        label = "Date de facturation",
        onValueChange = { onInvoiceDateChange(it) },
        modifier = modifier.fillMaxWidth()
    )
}