package models

import androidx.room.*
import java.time.LocalDate

@Entity(
    tableName = "report",
    foreignKeys = [ForeignKey(
        entity = Client::class,
        parentColumns = ["clientId"],
        childColumns = ["clientOwnerId"],
        onDelete = ForeignKey.CASCADE
    ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["creatorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Report(
    @PrimaryKey(autoGenerate = true) val reportId: Long = 0,
    val creatorId: Long,
    val executionOrder: Long,
    val requestDate: LocalDate,
    val workStartDate: LocalDate,
    val workFinishDate: LocalDate,
    val clientOwnerId: Long,
    val operators: List<String>,
    val type: OperationType,
    val company: Company,
    val wellDrilling: String,
    val asker: String,
    val depth: Long?,
    val staticLevel: Long?,
    val dynamicLevel: Long?,
    val pumpShimming: Long?,
    val speed: Float?,
    val engine: String?,
    val pump: String?,
    val elements: List<String>,
    val notes: String?,
    val purchaseRequest: String, // demande d'achat
    val quotation: String, // devis
    val purchaseOrder: String, // bon de commande
    val invoice: String, // facture
    val invoiceDate: LocalDate?
) {
    enum class OperationType(val beautiful: String) {
        ASSEMBLY("Montage"),
        DISASSEMBLY("Démontage"),
        BOTH("Démontage et montage"),
        CLEANING("Nettoyage");
    }
}

@Dao
interface ReportDao {
    @Insert
    suspend fun insert(report: Report)

    @Delete
    suspend fun delete(report: Report)

    @Update
    suspend fun update(report: Report)

    @Query("SELECT * FROM report")
    suspend fun getAllReports(): List<Report>
}