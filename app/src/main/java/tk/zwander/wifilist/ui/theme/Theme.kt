package tk.zwander.wifilist.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun WiFiListTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> {
            dynamicDarkColorScheme(LocalContext.current)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicLightColorScheme(LocalContext.current)
        }
        darkTheme -> {
            darkColorScheme()
        }
        else -> {
            lightColorScheme()
        }
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}