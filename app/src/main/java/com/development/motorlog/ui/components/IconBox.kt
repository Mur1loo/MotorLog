package com.development.motorlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.development.motorlog.ui.theme.MlFormas

// Caixinha quadrada com ícone (38dp, raio 11) — usada em linhas de alerta, histórico e detalhe.
// Sem cor: fundo surfaceAlt e ícone muted. Com cor (status): fundo = cor com alpha, ícone na cor.
@Composable
fun IconBox(icone: Int, modifier: Modifier = Modifier, cor: Color? = null, tamanho: Dp = 38.dp) {
    val fundo = cor?.copy(alpha = 0.15f) ?: MaterialTheme.colorScheme.surfaceContainerHigh
    val tint = cor ?: MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier.size(tamanho).clip(MlFormas.caixaIcone).background(fundo),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icone), contentDescription = null, tint = tint, modifier = Modifier.size(tamanho * 0.53f))
    }
}
