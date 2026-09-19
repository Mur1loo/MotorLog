package com.development.motorlog.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.development.motorlog.ui.theme.MlFormas

// Card do protótipo: surface + borda 1px + raio 18. Clicável quando onClick != null.
@Composable
fun MlCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    pad: Dp = 16.dp,
    cor: Color = MaterialTheme.colorScheme.surface,
    borda: Color = MaterialTheme.colorScheme.outline,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cores = CardDefaults.cardColors(containerColor = cor)
    val stroke = if (borda == Color.Transparent) null else BorderStroke(1.dp, borda)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = MlFormas.card, colors = cores, border = stroke) {
            Column(Modifier.padding(PaddingValues(pad)), content = content)
        }
    } else {
        Card(modifier = modifier, shape = MlFormas.card, colors = cores, border = stroke) {
            Column(Modifier.padding(PaddingValues(pad)), content = content)
        }
    }
}
