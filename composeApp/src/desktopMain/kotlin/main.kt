import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import magrinov.composeapp.generated.resources.Res
import magrinov.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    Window(
        state = windowState,
        onCloseRequest = ::exitApplication,
        title = "Magrinov",
        icon = painterResource(Res.drawable.icon)
    ) { App() }
}
