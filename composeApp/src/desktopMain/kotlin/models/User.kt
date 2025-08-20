package models

import androidx.room.*

@Entity(
    tableName = "user",
    indices = [Index(value = ["username", "company"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val username: String,
    val password: String,
    val isAdmin: Boolean = false,
    val company: Company
)

@Dao
interface UserDao {
    // Prefer this "upsert" for seeding or updates where uniqueness may conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User): Long

    @Delete
    suspend fun delete(user: User)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM user WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT * FROM user WHERE company = :company")
    suspend fun getUsersByCompany(company: Company): List<User>

    @Query("SELECT COUNT(*) FROM user WHERE isAdmin = 1")
    suspend fun countAdmins(): Int

    @Query("SELECT * FROM user WHERE isAdmin = 1 AND company = :company LIMIT 1")
    suspend fun getAdminByCompany(company: Company): User?

    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<User>
}

