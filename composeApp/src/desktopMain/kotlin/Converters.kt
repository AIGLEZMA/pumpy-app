import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Report
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {

    private val json = Json { encodeDefaults = true }
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromString(value: String): List<String> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return json.encodeToString(list)
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.format(formatter)
    }

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, formatter)
    }

    @TypeConverter
    fun fromOperationType(type: Report.OperationType): String {
        return type.name
    }

    @TypeConverter
    fun toOperationType(name: String): Report.OperationType {
        return Report.OperationType.valueOf(name)
    }

}
