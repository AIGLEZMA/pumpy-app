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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import screenmodels.LoginScreenModel

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.rememberNavigatorScreenModel { LoginScreenModel() }

        val loginState = screenModel.loginState
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        // FocusRequesters for each field and button
        val usernameFocusRequester = remember { FocusRequester() }
        val passwordFocusRequester = remember { FocusRequester() }
        val loginButtonFocusRequester = remember { FocusRequester() }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Magrinov",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Connectez vous pour accéder à l'application.")

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nom d'utilisateur") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(0.21f)
                        .focusRequester(usernameFocusRequester)
                        .focusProperties { next = passwordFocusRequester }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it.filterNot { char -> char in listOf('\t', '\n') }
                    },
                    label = { Text("Mot de passe") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.21f)
                        .focusRequester(passwordFocusRequester)
                        .focusProperties { next = loginButtonFocusRequester }
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.key.nativeKeyCode == 13) { // 13 is the native key code for ENTER
                                screenModel.login(username, password)
                                true
                            } else {
                                false
                            }
                        }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { screenModel.login(username, password) },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.21f)
                        .focusRequester(loginButtonFocusRequester)
                ) {
                    Text(text = "Connexion", color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (loginState.isLoading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Connexion en cours...", color = MaterialTheme.colorScheme.primary)
                } else {
                    when {
                        loginState.isAuthenticated -> {
                            LaunchedEffect(Unit) {
                                navigator.push(ReportsScreen())
                            }
                        }

                        loginState.errorMessage != null -> {
                            Text(loginState.errorMessage, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
