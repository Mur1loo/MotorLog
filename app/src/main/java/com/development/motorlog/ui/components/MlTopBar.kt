package com.development.motorlog.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.development.motorlog.R

// Cabeçalho do protótipo (Header): título em Chakra Petch (27 "big" na Garagem, 19 nas outras),
// subtítulo apagado, seta de voltar e ações à direita.
@Composable
fun MlTopBar(
    titulo: String,
    subtitulo: String? = null,
    grande: Boolean = false,
    onVoltar: (() -> Unit)? = null,
    acoes: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .heightIn(min = 56.dp)
            .padding(horizontal = if (onVoltar != null) 6.dp else 16.dp, vertical = if (grande) 10.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onVoltar != null) {
            IconButton(onClick = onVoltar) {
                Icon(painterResource(R.drawable.ic_ml_back), contentDescription = "Voltar", modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(2.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                titulo,
                style = if (grande) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            if (subtitulo != null) {
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        acoes?.invoke(this)
    }
}
