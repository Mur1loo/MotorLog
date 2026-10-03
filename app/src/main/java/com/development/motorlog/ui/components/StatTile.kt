package com.development.motorlog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.motorlog.ui.theme.MlTextFaint
import com.development.motorlog.ui.theme.chakra

// Tile de estatística do protótipo: rótulo apagado + ícone à direita + número em Chakra Petch.
@Composable
fun StatTile(
    rotulo: String,
    valor: String?,
    modifier: Modifier = Modifier,
    unidade: String? = null,
    icone: Int? = null,
    cor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    conteudo: (@Composable ColumnScope.() -> Unit)? = null,
) {
    MlCard(modifier = modifier.fillMaxHeight(), onClick = onClick, pad = 14.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(rotulo.uppercase(), style = MaterialTheme.typography.labelSmall, color = MlTextFaint, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (icone != null) {
                Icon(painterResource(icone), contentDescription = null, tint = if (cor == MaterialTheme.colorScheme.onSurface) MlTextFaint else cor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        if (valor != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                // com fonte grande o número encolhe pra caber no bloco, em vez de ser cortado
                BasicText(
                    valor, style = chakra(25.sp).copy(color = cor), maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = 25.sp),
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (unidade != null) {
                    Spacer(Modifier.width(4.dp))
                    Text(unidade, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp))
                }
            }
        }
        conteudo?.invoke(this)
    }
}

// Linha de estatísticas com espaçamento do protótipo
@Composable
fun LinhaDeTiles(modifier: Modifier = Modifier, content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(10.dp), content = content)
}
