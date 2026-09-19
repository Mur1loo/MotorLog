package com.development.motorlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Bolinha de status (como o StatusDot do protótipo). A cor vem de StatusTroca.cor().
@Composable
fun StatusDot(cor: Color, modifier: Modifier = Modifier, tamanho: Dp = 10.dp) {
    Box(modifier = modifier.size(tamanho).clip(CircleShape).background(cor))
}
