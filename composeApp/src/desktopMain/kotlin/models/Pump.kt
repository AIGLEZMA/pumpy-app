package models

import androidx.room.*

@Entity(
    tableName = "pump",
    foreignKeys = [ForeignKey(
        entity = Farm::class,
        parentColumns = ["farmId"],
        childColumns = ["farmOwnerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Pump(
    @PrimaryKey(autoGenerate = true) val pumpId: Long = 0,
    val name: String,
    val farmOwnerId: Long
)

@Dao
interface PumpDao {
    @Insert
    suspend fun insert(pump: Pump): Long

    @Transaction
    @Query("SELECT * FROM pump WHERE farmOwnerId = :farmId")
    suspend fun getPumpsByFarmId(farmId: Long): List<Pump>

    @Query("SELECT * FROM pump")
    suspend fun getAllPumps(): List<Pump>
}