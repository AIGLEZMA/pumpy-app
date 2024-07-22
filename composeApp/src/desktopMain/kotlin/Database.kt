import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import models.User
import models.UserDao
import java.nio.file.Paths

@Database(entities = [User::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

object DatabaseProvider {
    private var instance: AppDatabase? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun getDatabase(): AppDatabase {
        return instance ?: synchronized(this) {
            val databasePath = Paths.get(System.getProperty("user.home"), "Magrinov/app_database.db").toString()
            Logger.debug("DatabasePath: Database path: $databasePath")
            val newInstance = Room.databaseBuilder<AppDatabase>(
                name = databasePath
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(connection: SQLiteConnection) {
                        super.onCreate(connection)
                        val adminPassword = System.getenv("ADMIN_PASSWORD") ?: "admin"
                        val hashedPassword = BCrypt.withDefaults().hashToString(12, adminPassword.toCharArray())
                        GlobalScope.launch(Dispatchers.IO) {
                            instance?.userDao()
                                ?.insert(User(username = "admin", password = hashedPassword, isAdmin = true))
                            Logger.debug("DatabaseCallback: Admin user inserted with password: $adminPassword")
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
}