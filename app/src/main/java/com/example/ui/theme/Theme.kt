package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RetroDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ArcadeDark,
    primaryContainer = Color(0xFF003847),
    onPrimaryContainer = NeonCyan,
    secondary = NeonMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A0025),
    onSecondaryContainer = Color(0xFFFFB2D6),
    tertiary = RetroGoldCoin,
    onTertiary = ArcadeDark,
    background = ArcadeDark,
    onBackground = RetroWhite,
    surface = ArcadeSurface,
    onSurface = RetroWhite,
    surfaceVariant = ArcadeSurfaceVariant,
    onSurfaceVariant = RetroMuted,
    outline = ArcadeCardBorder,
    error = PixelRedHeart,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // We enforce the signature Retro State RS arcade theme
    MaterialTheme(
        colorScheme = RetroDarkColorScheme,
        typography = Typography,
        content = content
    )
}
