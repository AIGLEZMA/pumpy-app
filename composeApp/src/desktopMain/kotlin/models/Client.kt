package models

import androidx.room.*

@Entity(tableName = "client")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String
)

@Dao
interface ClientDao {
    @Insert
    suspend fun insert(client: Client)

    @Delete
    suspend fun delete(client: Client)

    @Update
    suspend fun update(client: Client)

    @Query("SELECT * FROM client WHERE username = :username")
    suspend fun getClientByUsername(username: String): Client?

    @Query("SELECT * FROM client WHERE id= :id")
    suspend fun getClientById(id: Long): Client?

    @Query("SELECT * FROM user")
    suspend fun getAllClients(): List<Client>
}