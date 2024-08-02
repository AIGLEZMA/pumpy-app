import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun AccountIcon(
    firstLetter: String,
    modifier: Modifier = Modifier
) {
    val randomColor = remember { getRandomColor() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(randomColor)
    ) {
        Text(
            text = firstLetter,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun getRandomColor(): Color {
    val random = Random
    val red = random.nextInt(256)
    val green = random.nextInt(256)
    val blue = random.nextInt(256)
    return Color(red, green, blue)
}
