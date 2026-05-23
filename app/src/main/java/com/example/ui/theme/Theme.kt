package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GlowingTeal,
    secondary = CyberBlue,
    tertiary = SoftLavender,
    background = DeepSpaceDb,
    surface = BlueGalaxyDb,
    onBackground = PureWhite,
    onSurface = PureWhite,
    onPrimary = DeepSpaceDb,
    onSecondary = DeepSpaceDb
)

@Composable
fun PloysaiTheme(
    content: @Composable () -> Unit
) {
    // Ploysai is optimized for nighttime cozy viewing, so we enforce dark mode!
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
