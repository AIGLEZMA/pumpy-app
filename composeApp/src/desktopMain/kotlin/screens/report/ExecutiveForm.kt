package screens.report

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = purchaseRequest ?: "",
        label = { Text("Numéro D.A") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        onValueChange = onPurchaseRequestChange,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focusManager.moveFocus(FocusDirection.Next); true
                } else false
            }
    )

    OutlinedTextField(
        value = quotation ?: "",
        label = { Text("Numéro de devis") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        onValueChange = onQuotationChange,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focusManager.moveFocus(FocusDirection.Next); true
                } else false
            }
    )

    OutlinedTextField(
        value = purchaseOrder ?: "",
        label = { Text("Bon de commande") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        onValueChange = onPurchaseOrderChange,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    focusManager.moveFocus(FocusDirection.Next); true
                } else false
            }
    )

    OutlinedTextField(
        value = invoice ?: "",
        label = { Text("Facture") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        onValueChange = onInvoiceChange,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                    // Done on the last text field → move to date picker (next focus)
                    focusManager.moveFocus(FocusDirection.Next); true
                } else false
            }
    )

    DatePickerAndTextField(
        value = invoiceDate,
        label = "Date de facturation",
        onValueChange = onInvoiceDateChange,
        modifier = modifier.fillMaxWidth()
    )
}
