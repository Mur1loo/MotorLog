package com.development.motorlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Bolinha de status com halo translúcido (StatusDot do protótipo). A cor vem de StatusTroca.cor().
@Composable
fun StatusDot(cor: Color, modifier: Modifier = Modifier, tamanho: Dp = 9.dp) {
    Box(
        modifier = modifier.size(tamanho + 6.dp).clip(CircleShape).background(cor.copy(alpha = 0.16f)).padding(3.dp),
    ) {
        Box(Modifier.size(tamanho).clip(CircleShape).background(cor))
    }
}
