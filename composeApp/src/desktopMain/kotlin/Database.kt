import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import models.*
import java.nio.file.Paths

@Database(entities = [User::class, Client::class, Farm::class, Pump::class, Report::class], version = 5)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun clientDao(): ClientDao
    abstract fun farmDao(): FarmDao
    abstract fun pumpDao(): PumpDao
    abstract fun reportDao(): ReportDao
}

object DatabaseProvider {
    private var instance: AppDatabase? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun getDatabase(): AppDatabase {
        return instance ?: synchronized(this) {
            val databasePath = getApplicationDataPath()
            Logger.debug("[Database] Database path: $databasePath")
            val newInstance = Room.databaseBuilder<AppDatabase>(
                name = databasePath
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(connection: SQLiteConnection) {
                        super.onCreate(connection)
                        val adminPassword = System.getenv("ADMIN_PASSWORD") ?: "admin"
                        val hashedPassword = Password.hash(adminPassword)
                        GlobalScope.launch(Dispatchers.IO) {
                            instance?.userDao()
                                ?.insert(User(username = "admin", password = hashedPassword, isAdmin = true))
                            Logger.debug("[Database] Admin user inserted with password: $adminPassword, hash: $hashedPassword")
                        }
                    }
                })
                .fallbackToDestructiveMigration(true)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
            instance = newInstance
            newInstance
        }
    }

    private fun getApplicationDataPath(): String {
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

}