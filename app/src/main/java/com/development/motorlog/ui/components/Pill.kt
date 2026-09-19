package com.development.motorlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.development.motorlog.ui.theme.MlFormas

// Etiqueta do protótipo: fundo = a própria cor com alpha (ok/soon/over) ou surfaceAlt (neutra).
@Composable
fun Pill(
    texto: String,
    cor: Color,
    modifier: Modifier = Modifier,
    icone: Int? = null,
    fundo: Color = cor.copy(alpha = 0.15f),
) {
    Row(
        modifier = modifier.clip(MlFormas.pill).background(fundo).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icone != null) {
            Icon(painterResource(icone), contentDescription = null, tint = cor, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(texto, color = cor, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun PillNeutra(texto: String, modifier: Modifier = Modifier, icone: Int? = null) = Pill(
    texto, MaterialTheme.colorScheme.onSurfaceVariant, modifier, icone,
    fundo = MaterialTheme.colorScheme.surfaceContainerHigh,
)
