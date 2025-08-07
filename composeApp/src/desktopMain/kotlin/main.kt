import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    val svgDiagramPath = javaClass.getResource("/diagram.svg")
    if (svgDiagramPath != null) {
        print(svgDiagramPath.toExternalForm())
    }
    val svgDiagramPath2 = javaClass.getResource("diagram.svg")
    if (svgDiagramPath2 != null) {
        print(svgDiagramPath2.toExternalForm())
    }
    Window(
        state = windowState,
        onCloseRequest = ::exitApplication,
        title = "Magrinov",
    ) { App() }
}
