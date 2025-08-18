package models

import androidx.room.*

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val password: String,
    val isAdmin: Boolean = false,
    val company: Company
)

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)

    @Delete
    suspend fun delete(user: User)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM user WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM user WHERE id= :id")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT * FROM user WHERE company = :company")
    suspend fun getUsersByCompany(company: Company): List<User>

    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<User>
}