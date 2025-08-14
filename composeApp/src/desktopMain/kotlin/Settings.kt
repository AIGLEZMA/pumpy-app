import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class AppSettings(
    val isDarkTheme: Boolean = false,
    val autoOpenPDF: Boolean = false
)

object SettingsRepository {
    // Initialize from disk exactly once
    private val _settings = MutableStateFlow(Settings.loadSettings())
    val settings: StateFlow<AppSettings> get() = _settings

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()

    fun update(transform: (AppSettings) -> AppSettings) {
        ioScope.launch {
            mutex.withLock {
                val updated = transform(_settings.value)
                Settings.saveSettings(updated)
                _settings.value = updated
            }
        }
    }
}

object Settings {
    private val json = Json { prettyPrint = true }

    private val settingsFile: File by lazy {
        val appDataDir = File(getApplicationDataPath())
        File(appDataDir, "settings.json")
    }

    fun loadSettings(): AppSettings {
        return try {
            if (settingsFile.exists()) {
                json.decodeFromString<AppSettings>(settingsFile.readText())
            } else {
                AppSettings()
            }
        } catch (e: Exception) {
            println("Error loading settings: $e")
            AppSettings()
        }
    }

    fun saveSettings(settings: AppSettings) {
        try {
            settingsFile.writeText(json.encodeToString(settings))
        } catch (e: Exception) {
            println("Error saving settings: $e")
        }
    }
}