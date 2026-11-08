package io.github.elli0t0.calendar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val TealBlue = Color(0xFF006A7A)
private val TealBlueLight = Color(0xFF5AC8D8)

private val LightScheme = lightColorScheme(
    primary = TealBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBFE8EF),
    onPrimaryContainer = Color(0xFF00363F),
    secondary = Color(0xFF4A6B70),
    background = Color(0xFFF6F8F8),
    onBackground = Color(0xFF191C1D),
    surface = Color.White,
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFE8EDED),
    onSurfaceVariant = Color(0xFF5A6365),
    error = Color(0xFFBA1A1A)
)

private val DarkScheme = darkColorScheme(
    primary = TealBlueLight,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5A),
    onPrimaryContainer = Color(0xFFBFE8EF),
    secondary = Color(0xFFA8C6CB),
    background = Color(0xFF101414),
    onBackground = Color(0xFFE2E5E5),
    surface = Color(0xFF1A1F20),
    onSurface = Color(0xFFE2E5E5),
    surfaceVariant = Color(0xFF2A3233),
    onSurfaceVariant = Color(0xFFA9B3B4),
    error = Color(0xFFFFB4AB)
)

private val Shapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun CalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        shapes = Shapes,
        content = content
    )
}
