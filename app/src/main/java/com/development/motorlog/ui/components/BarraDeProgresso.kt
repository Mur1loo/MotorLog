package com.development.motorlog.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Barra do protótipo (ProgressBar): fina, arredondada, sem o stop indicator do M3, e que
// PREENCHE ao entrar na tela (de 0 até o valor) — sensação de instrumento acordando.
@Composable
fun BarraDeProgresso(pct: Float, cor: Color, modifier: Modifier = Modifier, altura: Dp = 7.dp) {
    var alvo by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(pct) { alvo = pct.coerceIn(0f, 1f) }
    val animado by animateFloatAsState(targetValue = alvo, animationSpec = tween(650), label = "progresso")
    LinearProgressIndicator(
        progress = { animado },
        modifier = modifier.fillMaxWidth().height(altura),
        color = cor,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        gapSize = 0.dp,
        drawStopIndicator = {},
    )
}
