package com.development.motorlog.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Esquema fixo dark, espelhando os tokens do protótipo.
// (o design é dark-only; sem dynamic color, sem alternância clara/escura)
private val MotorLogColorScheme = darkColorScheme(
    primary = MlAccent,
    onPrimary = MlOnAccent,
    secondary = MlAccent,
    onSecondary = MlOnAccent,
    // FilterChip selecionado, indicadores: accent sólido com texto escuro (como no protótipo)
    secondaryContainer = MlAccent,
    onSecondaryContainer = MlOnAccent,
    primaryContainer = MlAccent,
    onPrimaryContainer = MlOnAccent,
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
        shapes = Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(13.dp),
            large = RoundedCornerShape(18.dp),
            extraLarge = RoundedCornerShape(22.dp),   // diálogos e folhas
        ),
        content = content,
    )
}
