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

@Database(entities = [User::class, Client::class, Farm::class, Pump::class, Report::class], version = 3)
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
            val databasePath = Paths.get(System.getProperty("user.home"), "Magrinov/app_database.db").toString()
            Logger.debug("DatabasePath: Database path: $databasePath")
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
                            Logger.debug("DatabaseCallback: Admin user inserted with password: $adminPassword, hash: $hashedPassword")
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