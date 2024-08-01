import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import screens.AddEditReportScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        Navigator(AddEditReportScreen()) { navigator ->
            SlideTransition(navigator = navigator)
        }
    }
}