import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class AppSettings(
    val isDarkTheme: Boolean = false
)

object SettingsManager {
    private val json = Json { prettyPrint = true }

    private val settingsFile: File by lazy {
        val appDataDir = File(getApplicationDataPath()).parentFile
        if (!appDataDir.exists()) appDataDir.mkdirs()
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