import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import screens.LoginScreen
import java.nio.file.Paths

@Composable
@Preview
fun App() {
    val colorScheme = if (Theme.isDarkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
    ) {
        Navigator(LoginScreen()) { navigator ->
            SlideTransition(navigator = navigator)
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