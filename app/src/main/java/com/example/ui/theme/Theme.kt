package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IosLightGlassColorScheme = lightColorScheme(
    primary = IosSystemBlue,
    onPrimary = Color.White,
    primaryContainer = IosSystemBlueSubtle,
    onPrimaryContainer = IosSystemBlue,
    secondary = IosSystemIndigo,
    onSecondary = Color.White,
    secondaryContainer = IosSystemGray5,
    onSecondaryContainer = IosTextPrimary,
    tertiary = IosSystemTeal,
    onTertiary = Color.White,
    background = IosCanvasBackground,
    onBackground = IosTextPrimary,
    surface = IosGlassFill,
    onSurface = IosTextPrimary,
    surfaceVariant = IosGlassCard,
    onSurfaceVariant = IosTextSecondary,
    surfaceTint = Color.Transparent,
    outline = IosGlassBorderSubtle,
    outlineVariant = IosGlassBorderHighlight,
    error = IosSystemRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = IosLightGlassColorScheme,
        typography = Typography,
        content = content
    )
}
