package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
import screenmodels.LoginScreenModel

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.rememberNavigatorScreenModel { LoginScreenModel() }

        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var selectedCompany by remember { mutableStateOf(Company.MAGRINOV) }

        val loginState = screenModel.loginState
        val focusManager = LocalFocusManager.current

        fun submit() {
            if (!loginState.isLoading) {
                screenModel.login(username.trim(), password, selectedCompany)
            }
        }

        // Navigate once when authenticated
        LaunchedEffect(loginState.isAuthenticated) {
            if (loginState.isAuthenticated) navigator.push(ReportsScreen())
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Magrinov", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Connectez vous pour accéder à l'application.")

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Nom d'utilisateur") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth(0.21f)
                            .onPreviewKeyEvent { e ->
                                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                                    focusManager.moveFocus(FocusDirection.Next)
                                    true
                                } else false
                            }
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it.filterNot { ch -> ch == '\n' || ch == '\t' }
                        },
                        singleLine = true,
                        label = { Text("Mot de passe") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            val description = if (passwordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.21f)
                            .onPreviewKeyEvent { e ->
                                if (e.key == Key.Enter && e.type == KeyEventType.KeyUp) {
                                    submit()
                                    true
                                } else false
                            }
                    )

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth(0.21f)
                            .semantics { role = Role.Tab }
                            .selectableGroup()
                    ) {
                        val options = listOf(Company.MAGRINOV, Company.LOTRAX)
                        options.forEachIndexed { index, company ->
                            SegmentedButton(
                                selected = selectedCompany == company,
                                onClick = { selectedCompany = company },
                                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                                label = { Text(company.pretty) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { submit() },
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
                        shape = MaterialTheme.shapes.small,
                        enabled = !loginState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth(0.21f)
                            .height(54.dp)
                    ) {
                        Text(text = "Connexion", color = Color.White)
                    }

                    Spacer(Modifier.height(16.dp))

                    if (loginState.isLoading) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Connexion en cours...", color = MaterialTheme.colorScheme.primary)
                    } else if (loginState.errorMessage != null) {
                        Text(loginState.errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
