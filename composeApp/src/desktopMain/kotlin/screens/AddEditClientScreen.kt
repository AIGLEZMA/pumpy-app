package screens

import Logger
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        var name by remember { mutableStateOf(clientState.name) }
        var phoneNumber by remember { mutableStateOf(clientState.phoneNumber) }
        var location by remember { mutableStateOf(clientState.location) }

        var phoneNumberError by remember { mutableStateOf<String?>(null) }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MAGRINOV",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (clientState.isEditMode) "Editer les informations du client" else "Ajouter un nouveau client",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.21f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                            phoneNumber = it
                            phoneNumberError = if (it.isEmpty() || it.startsWith("0")) {
                                null
                            } else {
                                "Phone number must start with 0"
                            }
                        } else if (it.isEmpty()) {
                            phoneNumber = it
                            phoneNumberError = null
                        }
                    },
                    label = { Text("Numéro de téléphone") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = phoneNumberError != null,
                    modifier = Modifier.fillMaxWidth(0.21f)
                )
                phoneNumberError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Localisation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.21f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { screenModel.saveClient(name, phoneNumber, location) },
                    modifier = Modifier.fillMaxWidth(0.21f)
                ) {
                    Text(text = "Enregister")
                }
                Button(
                    onClick = { navigator.pop() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(0.21f)
                ) {
                    Text(text = "Annuler")
                }
                when {
                    clientState.isSaved -> {
                        LaunchedEffect(Unit) {
                            navigator.pop()
                            Logger.debug("Client (name: ${clientState.name}) edited.")
                        }
                    }

                    clientState.errorMessage != null -> {
                        Text(clientState.errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
