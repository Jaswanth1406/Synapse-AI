package com.example.synapseai.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val VedasparkDarkScheme = darkColorScheme(
    primary = ElectricIndigo,
    onPrimary = TextPrimary,
    primaryContainer = ElectricIndigoDark,
    onPrimaryContainer = TextPrimary,
    secondary = CyanAccent,
    onSecondary = DarkBackground,
    secondaryContainer = CyanAccentDark,
    onSecondaryContainer = TextPrimary,
    tertiary = WarmAmber,
    onTertiary = DarkBackground,
    tertiaryContainer = WarmAmberDark,
    onTertiaryContainer = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextPrimary,
    outline = GlassBorder
)

@Composable
fun SynapseAITheme(
    content: @Composable () -> Unit
) {
    val colorScheme = VedasparkDarkScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkSurface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}