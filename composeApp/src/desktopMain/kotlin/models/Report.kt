package models

import androidx.room.*
import java.time.LocalDate

@Entity(
    tableName = "report",
    foreignKeys = [ForeignKey(
        entity = Pump::class,
        parentColumns = ["pumpId"],
        childColumns = ["pumpOwnerId"],
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
    val pumpOwnerId: Long,
    val operators: List<String>,
    val type: OperationType,
    val depth: Long?,
    val staticLevel: Long?,
    val dynamicLevel: Long?,
    val pumpShimming: Long?,
    val speed: Float?,
    val engine: String?,
    val pump: String?,
    val elements: String?,
    val notes: String?,
    val purchaseRequest: String, // demande d'achat
    val quotation: String, // devis
    val purchaseOrder: String, // bon de commande
    val invoice: String, // facture
    val invoiceDate: LocalDate?,
) {
    enum class OperationType(val beautiful: String) {
        ASSEMBLY("Montage"),
        DISASSEMBLY("Démontage")
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

    @Query("SELECT * FROM report WHERE pumpOwnerId = :pumpId")
    suspend fun getReportByPumpId(pumpId: Long): List<Report>

    @Query("SELECT * FROM report")
    suspend fun getAllReports(): List<Report>
}