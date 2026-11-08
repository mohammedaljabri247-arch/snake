package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NeonArcadeColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = PureBlack,
    primaryContainer = DeepPurple,
    onPrimaryContainer = ElectricCyan,
    secondary = ElectricCyan,
    onSecondary = PureBlack,
    secondaryContainer = GlassSurface,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonMagenta,
    onTertiary = TextPrimary,
    background = PureBlack,
    onBackground = TextPrimary,
    surface = DarkNavy,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = DangerRed
)

@Composable
fun SnakeEvolutionTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeonArcadeColorScheme,
        typography = Typography,
        content = content
    )
}
