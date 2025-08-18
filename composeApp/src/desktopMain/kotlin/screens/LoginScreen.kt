package screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.Company
import models.User
import screenmodels.LoginScreenModel
import ui.AutoCompleteTextField

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.rememberNavigatorScreenModel { LoginScreenModel() }

        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var selectedCompany by remember { mutableStateOf<Company?>(null) }
        var selectedUser by remember { mutableStateOf<User?>(null) }
        var userList by remember { mutableStateOf<List<User>>(emptyList()) }

        val loginState = screenModel.loginState

        fun submit() {
            if (!loginState.isLoading && selectedUser != null) {
                screenModel.loginWithUser(selectedUser!!, password)
            }
        }

        LaunchedEffect(selectedCompany) {
            selectedUser = null
            if (selectedCompany != null) {
                userList = screenModel.getUsersForCompany(selectedCompany!!)
            }
        }

        LaunchedEffect(loginState.isAuthenticated) {
            if (loginState.isAuthenticated) navigator.push(ReportsScreen())
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.35f).padding(24.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Magrinov", style = MaterialTheme.typography.headlineLarge)
                        Text("Connectez-vous pour accéder à l'application.")

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(Company.MAGRINOV, Company.LOTRAX).forEach { company ->
                                OutlinedButton(
                                    onClick = { selectedCompany = company },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selectedCompany == company)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                ) {
                                    Text(company.pretty)
                                }
                            }
                        }

                        AutoCompleteTextField(
                            label = "Nom d'utilisateur",
                            items = userList,
                            selectedItem = selectedUser,
                            onSelectedItemChange = { selectedUser = it },
                            displayText = { it.username },
                            enabled = selectedCompany != null,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it.filterNot { ch -> ch == '\n' || ch == '\t' }
                            },
                            singleLine = true,
                            enabled = selectedCompany != null,
                            label = { Text("Mot de passe") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            trailingIcon = {
                                val image =
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                val description =
                                    if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe"
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = description)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onPreviewKeyEvent { e ->
                                    if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                                        submit()
                                        true
                                    } else false
                                }
                        )

                        Button(
                            onClick = { submit() },
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
                            shape = MaterialTheme.shapes.small,
                            enabled = !loginState.isLoading && selectedUser != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            val buttonText = if (loginState.isLoading) "Connexion..." else "Connexion"
                            Text(text = buttonText, color = Color.White)
                        }

                        AnimatedVisibility(visible = loginState.isLoading) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(8.dp))
                                Text("Connexion en cours...", color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        AnimatedVisibility(visible = loginState.errorMessage != null) {
                            Text(loginState.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
