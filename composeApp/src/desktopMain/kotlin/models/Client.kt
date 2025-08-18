package models

import androidx.room.*

@Entity(tableName = "client")
data class Client(
    @PrimaryKey(autoGenerate = true) val clientId: Long = 0,
    val name: String,
    val phoneNumber: String,
    val location: String,
    val company: Company
)

@Dao
interface ClientDao {
    @Insert
    suspend fun insert(client: Client)

    @Delete
    suspend fun delete(client: Client)

    @Update
    suspend fun update(client: Client)

    @Query("SELECT * FROM client WHERE clientId= :clientId")
    suspend fun getClientById(clientId: Long): Client?

    @Query("SELECT * FROM client")
    suspend fun getAllClients(): List<Client>
}