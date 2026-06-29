package com.development.motorlog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Esquema fixo dark, espelhando os tokens do protótipo.
// (o design é dark-only; sem dynamic color, sem alternância clara/escura)
private val MotorLogColorScheme = darkColorScheme(
    primary = MlAccent,
    onPrimary = MlOnAccent,
    secondary = MlAccent,
    onSecondary = MlOnAccent,
    background = MlBg,
    onBackground = MlText,
    surface = MlSurface,
    onSurface = MlText,
    surfaceVariant = MlSurfaceAlt,
    onSurfaceVariant = MlTextMuted,
    surfaceContainerLowest = MlBg,
    surfaceContainerLow = MlBgElev,
    surfaceContainer = MlSurface,
    surfaceContainerHigh = MlSurfaceAlt,
    surfaceContainerHighest = MlSurfaceHi,
    outline = MlBorder,
    outlineVariant = MlBorder,
    error = MlOver,
    onError = MlBg,
)

@Composable
fun MotorLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MotorLogColorScheme,
        typography = Typography,
        content = content,
    )
}
