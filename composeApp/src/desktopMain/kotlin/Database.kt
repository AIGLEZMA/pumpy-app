import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import models.*
import kotlin.io.path.Path

@Database(entities = [User::class, Client::class, Report::class], version = 5)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun clientDao(): ClientDao
    abstract fun reportDao(): ReportDao
}

object DatabaseProvider {
    private var instance: AppDatabase? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun getDatabase(): AppDatabase {
        return instance ?: synchronized(this) {
            val databasePath = Path(getApplicationDataPath(), "app_database.db").toString()
            Logger.debug("[Database] Database path: $databasePath")
            val newInstance = Room.databaseBuilder<AppDatabase>(
                name = databasePath
            )
                .fallbackToDestructiveMigration(true)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
            instance = newInstance
            newInstance
        }
    }

    suspend fun ensureAdminAccounts(database: AppDatabase) {
        val dao = database.userDao()

        Company.entries
            .filter { it != Company.UNKNOWN } // Ignore UNKNOWN
            .forEach { company ->
                val existing = dao.getAdminByCompany(company)
                if (existing == null) {
                    val plainPassword = "admin" // TODO: env ?
                    val hashedPassword = Password.hash(plainPassword)
                    val defaultUsername = "admin@${company.name.lowercase()}"

                    dao.upsertUser(
                        User(
                            username = defaultUsername,
                            password = hashedPassword,
                            isAdmin = true,
                            company = company
                        )
                    )

                    Logger.debug("[Database] Created admin for ${company.pretty} with username: '$defaultUsername'")
                    Logger.debug("[Security] Default password (change it after first login): $plainPassword")
                }
            }
    }

}