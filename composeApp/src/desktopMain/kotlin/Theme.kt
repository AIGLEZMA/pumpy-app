import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFE5E5E5),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F)
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE5E1E6),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE5E1E6)
)

object Theme {
    private val initialSettings = SettingsManager.loadSettings()

    var isDarkTheme by mutableStateOf(initialSettings.isDarkTheme)
        private set

    @OptIn(DelicateCoroutinesApi::class)
    fun toggleTheme() {
        isDarkTheme = !isDarkTheme

        // Save the updated theme setting asynchronously
        GlobalScope.launch(Dispatchers.IO) {
            SettingsManager.saveSettings(AppSettings(isDarkTheme))
        }
    }
}