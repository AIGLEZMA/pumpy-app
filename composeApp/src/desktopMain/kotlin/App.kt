import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import screens.LoginScreen
import screens.SplashScreen
import java.nio.file.Paths

@OptIn(DelicateCoroutinesApi::class)
@Composable
@Preview
fun App() {
    val isDatabaseInitialized = remember { mutableStateOf(false) }
    val showSplash = remember { mutableStateOf(true) }

    // Use a LaunchedEffect to initialize the database in the background

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        DatabaseProvider.getDatabase()
        isDatabaseInitialized.value = true

        // Ensure the splash screen is shown for at least 2 seconds to prevent flickering
        val elapsedTime = System.currentTimeMillis() - startTime
        if (elapsedTime < 2000) {
            delay(2000 - elapsedTime)
        }
        showSplash.value = false
    }

    val colorScheme = if (Theme.isDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
    ) {
        if (showSplash.value) {
            SplashScreen()
        } else {
            Navigator(LoginScreen()) { navigator ->
                SlideTransition(navigator = navigator)
            }
        }
    }
}

fun getApplicationDataPath(): String {
    val os = System.getProperty("os.name").lowercase()
    val appName = "Magrinov"
    val userHome = System.getProperty("user.home")

    val basePath = when {
        os.contains("win") -> {
            // Windows: C:\Users\<username>\AppData\Roaming\
            System.getenv("APPDATA") ?: Paths.get(userHome, "AppData", "Roaming").toString()
        }

        os.contains("mac") -> {
            // macOS: ~/Library/Application Support/
            Paths.get(userHome, "Library", "Application Support").toString()
        }
        // Linux and other Unix-like systems
        else -> {
            // Linux: ~/.local/share/
            Paths.get(userHome, ".local", "share").toString()
        }
    }

    // Combine the base path with your application's directory and create it if it doesn't exist
    val appDataDir = Paths.get(basePath, appName).toFile()
    if (!appDataDir.exists()) {
        appDataDir.mkdirs()
    }

    return Paths.get(appDataDir.absolutePath, "app_database.db").toString()
}