import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import screens.UsersScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        Navigator(UsersScreen()) { navigator ->
            SlideTransition(navigator = navigator)
        }
    }
}