package com.development.motorlog.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.development.motorlog.ui.theme.MlTextFaint

// Rótulo de seção: MAIÚSCULO, espaçado, apagado — SectionLabel do protótipo, com slot à direita.
@Composable
fun SectionLabel(
    texto: String,
    modifier: Modifier = Modifier,
    direita: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, top = 2.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            texto.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MlTextFaint,
            modifier = Modifier.weight(1f),
        )
        direita?.invoke()
    }
}

// Ação de texto em accent ao lado do rótulo ("Ver todas", "Histórico")
@Composable
fun AcaoDeSecao(texto: String, onClick: () -> Unit) {
    Text(
        texto,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 6.dp, vertical = 8.dp),
    )
}
