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
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBFE8EF),
    onPrimaryContainer = Color(0xFF00363F),
    secondary = Color(0xFF4A7A80),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6E8EB),
    onSecondaryContainer = Color(0xFF111315),
    tertiary = Color(0xFF2E6B6B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD3E5E5),
    onTertiaryContainer = Color(0xFF111315),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111315),
    surface = Color(0xFFF2F3F5),
    onSurface = Color(0xFF111315),
    surfaceVariant = Color(0xFFE6E8EB),
    onSurfaceVariant = Color(0xFF5F6368),
    surfaceDim = Color(0xFFDDE0E3),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F8F9),
    surfaceContainer = Color(0xFFF2F3F5),
    surfaceContainerHigh = Color(0xFFEDEFF1),
    surfaceContainerHighest = Color(0xFFE6E8EB),
    outline = Color(0xFFC7CBD0),
    outlineVariant = Color(0xFFE6E8EB),
    inverseSurface = Color(0xFF1A1D21),
    inverseOnSurface = Color(0xFFF2F3F5),
    inversePrimary = TealBlueLight,
    scrim = Color(0xFF000000)
)

private val DarkScheme = darkColorScheme(
    primary = TealBlueLight,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5A),
    onPrimaryContainer = Color(0xFFBFE8EF),
    secondary = Color(0xFF8AB8C0),
    onSecondary = Color(0xFF0F1114),
    secondaryContainer = Color(0xFF2A2E33),
    onSecondaryContainer = Color(0xFFF2F3F5),
    tertiary = Color(0xFF6BA8A8),
    onTertiary = Color(0xFF0F1114),
    tertiaryContainer = Color(0xFF2A2E33),
    onTertiaryContainer = Color(0xFFF2F3F5),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF0F1114),
    onBackground = Color(0xFFF2F3F5),
    surface = Color(0xFF1A1D21),
    onSurface = Color(0xFFF2F3F5),
    surfaceVariant = Color(0xFF2A2E33),
    onSurfaceVariant = Color(0xFF9AA0A6),
    surfaceDim = Color(0xFF0F1114),
    surfaceBright = Color(0xFF2A2E33),
    surfaceContainerLowest = Color(0xFF0F1114),
    surfaceContainerLow = Color(0xFF15181C),
    surfaceContainer = Color(0xFF1A1D21),
    surfaceContainerHigh = Color(0xFF22262A),
    surfaceContainerHighest = Color(0xFF2A2E33),
    outline = Color(0xFF3A3F45),
    outlineVariant = Color(0xFF2A2E33),
    inverseSurface = Color(0xFFF2F3F5),
    inverseOnSurface = Color(0xFF111315),
    inversePrimary = TealBlue,
    scrim = Color(0xFF000000)
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
