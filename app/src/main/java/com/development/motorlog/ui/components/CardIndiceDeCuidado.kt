package com.development.motorlog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.motorlog.domain.IndiceDeCuidado
import com.development.motorlog.domain.StatusTroca
import com.development.motorlog.ui.theme.chakra
import com.development.motorlog.ui.theme.cor

// cor da nota: verde a partir de "Bem cuidada", amarelo em "Pede atenção", vermelho abaixo
fun corDoIndice(nota: Int): Color = when {
    nota >= 70 -> StatusTroca.OK.cor()
    nota >= 50 -> StatusTroca.PERTO.cor()
    else -> StatusTroca.VENCIDA.cor()
}

// Card do índice de cuidado no Painel: a nota, a faixa, a barra e o que fazer pra subir.
// Tocar abre as partes da nota (domain/IndiceDeCuidado.kt).
@Composable
fun CardIndiceDeCuidado(indice: IndiceDeCuidado, expandido: Boolean, onAlternar: () -> Unit) {
    val cor = corDoIndice(indice.nota)
    MlCard(onClick = onAlternar) {
        SectionLabel("Índice de cuidado", direita = { Text(if (expandido) "fechar" else "ver detalhes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) })
        Row(verticalAlignment = Alignment.Bottom) {
            Text("${indice.nota}", style = chakra(40.sp), color = cor)
            Text("/100", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
            Spacer(Modifier.width(12.dp))
            Text(indice.faixa, style = MaterialTheme.typography.titleMedium, color = cor, modifier = Modifier.padding(bottom = 8.dp))
        }
        BarraDeProgresso(indice.nota / 100f, cor, altura = 6.dp)
        indice.dica?.let {
            Spacer(Modifier.height(8.dp))
            Text("Pra subir: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (expandido) {
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                indice.partes.forEach { parte ->
                    Column {
                        Row {
                            Text(parte.nome, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text("${parte.nota}", style = chakra(14.sp), color = corDoIndice(parte.nota))
                        }
                        Spacer(Modifier.height(4.dp))
                        BarraDeProgresso(parte.nota / 100f, corDoIndice(parte.nota), altura = 4.dp)
                        parte.dica?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
                Text(
                    "A nota junta trocas em dia, manutenção registrada, revisão, cuidados de rotina e km atualizado. Vai no PDF do histórico.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
