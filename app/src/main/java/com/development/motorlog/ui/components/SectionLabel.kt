package com.development.motorlog.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Rótulo de seção: MAIÚSCULO, espaçado, muted — como o SectionLabel do protótipo.
@Composable
fun SectionLabel(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 2.dp, top = 4.dp, bottom = 2.dp),
    )
}
