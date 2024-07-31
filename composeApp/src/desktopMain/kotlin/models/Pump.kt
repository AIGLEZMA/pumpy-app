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
    @Transaction
    @Query("SELECT * FROM pump WHERE farmOwnerId = :farmId")
    suspend fun getPumpsByFarmId(farmId: Long): List<Pump>

    @Insert
    suspend fun insert(pump: Pump): Long
}