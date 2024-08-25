package screens.report

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Text(
        text = "Executive",
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier.padding(bottom = 16.dp)
    )
    OutlinedTextField(
        value = purchaseRequest ?: "",
        label = { Text("Numéro D.A") },
        onValueChange = { onPurchaseRequestChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = quotation ?: "",
        label = { Text("Numéro de Devis") },
        onValueChange = { onQuotationChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = purchaseOrder ?: "",
        label = { Text("Bon de commande") },
        onValueChange = { onPurchaseOrderChange(it) },
        modifier = modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = invoice ?: "",
        label = { Text("Facture") },
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