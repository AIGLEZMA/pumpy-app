import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import models.*
import kotlin.io.path.Path

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

}