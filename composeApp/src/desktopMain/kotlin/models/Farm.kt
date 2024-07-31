package models

import androidx.room.*

@Entity(
    tableName = "farm",
    foreignKeys = [ForeignKey(
        entity = Client::class,
        parentColumns = ["clientId"],
        childColumns = ["clientOwnerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Farm(
    @PrimaryKey(autoGenerate = true) val farmId: Long = 0,
    val name: String,
    val clientOwnerId: Long
)

@Dao
interface FarmDao {
    @Transaction
    @Query("SELECT * FROM farm WHERE clientOwnerId = :clientId")
    suspend fun getFarmsByClientId(clientId: Long): List<Farm>

    @Insert
    suspend fun insert(farm: Farm): Long
}