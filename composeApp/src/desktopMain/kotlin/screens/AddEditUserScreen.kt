package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.Company
import models.User
import screenmodels.AddEditUserScreenModel
import java.util.Locale.getDefault

class AddEditUserScreen(private val user: User? = null) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { AddEditUserScreenModel(user) }

        val userState = screenModel.userState
        var username by remember { mutableStateOf(userState.username) }
        var password by remember { mutableStateOf(userState.password) }
        var passwordVisible by remember { mutableStateOf(false) }

        var company by remember { mutableStateOf(userState.company) }
        var expanded by remember { mutableStateOf(false) }
        val companies = listOf(Company.MAGRINOV.pretty, Company.LOTRAX.pretty)
        var selectedCompanyText by remember { mutableStateOf(userState.company.pretty) }

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
                    text = if (userState.isEditMode) "Editer l'utilisateur" else "Ajouter un nouveau utilisateur",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nom d'utilisateur") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.21f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.21f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth(0.21f)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        readOnly = true,
                        value = selectedCompanyText,
                        onValueChange = {},
                        label = { Text("Entreprise") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        companies.forEach { companyOption ->
                            DropdownMenuItem(
                                text = { Text(companyOption) },
                                onClick = {
                                    selectedCompanyText = companyOption
                                    company = Company.valueOf(companyOption.uppercase(getDefault()))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { screenModel.saveUser(username, password, company) },
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
                    userState.isSaved -> {
                        LaunchedEffect(Unit) {
                            navigator.pop()
                        }
                    }

                    userState.errorMessage != null -> {
                        Text(userState.errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
