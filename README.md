TODOS

Main inspiration: https://dribbble.com/shots/20659983-Steam-Desktop-App-Redesign
New report inspiration: https://contacts.google.com/new

1. UI
[] Add (+) icon button in side panel (for clients, users) https://cdn.dribbble.com/userupload/11291771/file/original-098f8a6a92b4bac4a60a4b55de42f30f.png?resize=1024x768
[] Make the header smaller (in height)
[] Make the navigation items in the side panel smaller in height + make the text inside them smaller too
[] Add an EFAB to the sidepanel 
[] Revamp settings and user icon (use IconButton) OR make them smaller
[] Use this as users icons
   import androidx.compose.foundation.background
   import androidx.compose.foundation.layout.Box
   import androidx.compose.foundation.layout.size
   import androidx.compose.foundation.shape.CircleShape
   import androidx.compose.material3.MaterialTheme
   import androidx.compose.material3.Surface
   import androidx.compose.material3.Text
   import androidx.compose.runtime.Composable
   import androidx.compose.ui.Alignment
   import androidx.compose.ui.Modifier
   import androidx.compose.ui.graphics.Color
   import androidx.compose.ui.text.style.TextAlign
   import androidx.compose.ui.unit.dp
   import androidx.compose.ui.unit.sp

@Composable
fun AccountIcon(username: String) {
val firstLetter = username.firstOrNull()?.uppercaseChar() ?: '?'

    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.background(MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = firstLetter.toString(),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


[] We do not need a case for when no users exist (they will always be at least one user registered)
[] But for clients and reports: use a white pane with unDraw and Clients (0) and clickable text
