package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.Client
import screenmodels.AddEditClientScreenModel

class AddEditClientScreen(private val client: Client? = null) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { AddEditClientScreenModel(client) }

        val clientState = screenModel.clientState
        var name by rememberSaveable { mutableStateOf(clientState.name) }
        var phoneNumber by rememberSaveable { mutableStateOf(clientState.phoneNumber) }
        var location by rememberSaveable { mutableStateOf(clientState.location) }
        var phoneNumberError by rememberSaveable { mutableStateOf<String?>(null) }

        fun validatePhone(input: String): String? {
            if (input.isBlank()) return "Le numéro est requis"
            if (input.length != 10) return "Le numéro doit contenir 10 chiffres"
            if (!input.all { it.isDigit() }) return "Le numéro doit contenir uniquement des chiffres"
            if (!input.startsWith("0")) return "Le numéro doit commencer par 0"
            return null
        }

        fun submit() {
            val trimmedName = name.trim()
            val trimmedPhone = phoneNumber.trim()
            val trimmedLocation = location.trim()
            phoneNumberError = validatePhone(trimmedPhone)
            if (phoneNumberError == null) {
                screenModel.saveClient(trimmedName, trimmedPhone, trimmedLocation)
            }
        }

        // Close after successful save
        LaunchedEffect(clientState.isSaved) {
            if (clientState.isSaved) navigator.pop()
        }

        val canSave = phoneNumberError == null && name.isNotBlank()

        Surface(
            modifier = Modifier
                .fillMaxSize()
                // Press Enter anywhere to save (key up), handy on desktop
                .onPreviewKeyEvent { e ->
                    if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                        submit()
                        true
                    } else false
                },
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "MAGRINOV", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (clientState.isEditMode) "Éditer les informations du client" else "Ajouter un client",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(0.21f)
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { input ->
                        // Only keep digits, cap at 10
                        val digitsOnly = input.filter { it.isDigit() }.take(10)
                        phoneNumber = digitsOnly
                        phoneNumberError = validatePhone(digitsOnly)
                    },
                    label = { Text("Numéro de téléphone") },
                    singleLine = true,
                    isError = phoneNumberError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(0.21f)
                )
                phoneNumberError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Localisation") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(0.21f)
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(0.21f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { navigator.pop() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annuler")
                    }
                    Button(
                        onClick = { submit() },
                        enabled = canSave,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Enregistrer")
                    }
                }

                Spacer(Modifier.height(12.dp))

                clientState.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
