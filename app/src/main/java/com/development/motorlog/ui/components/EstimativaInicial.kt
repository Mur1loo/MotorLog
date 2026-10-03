package com.development.motorlog.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.development.motorlog.R
import com.development.motorlog.ui.util.formatarKm

// "Não lembra quando trocou?": marca todas as peças sem registro como trocadas no km atual. É o
// atalho que tira o app do vazio no primeiro uso — por isso aparece no Painel vazio e no grupo
// "Nunca registrei" da tela Trocas (antes, só lá no fim de Trocas, onde quase ninguém chegava).
@Composable
fun CardNaoLembra(quantidade: Int, kmAtual: Int, onMarcar: () -> Unit, modifier: Modifier = Modifier) {
    MlCard(modifier = modifier, pad = 14.dp, cor = MaterialTheme.colorScheme.surfaceContainerHigh, borda = Color.Transparent) {
        Text("Não lembra quando trocou?", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "Marque as $quantidade peças como trocadas hoje, aos ${formatarKm(kmAtual)}. " +
                "Eu passo a contar o intervalo a partir daí, e você corrige as que lembrar tocando nelas.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        BotaoSecundario("Marcar todas como trocadas agora", onMarcar, Modifier.fillMaxWidth(), icone = R.drawable.ic_ml_check, enabled = quantidade > 0)
    }
}

@Composable
fun ConfirmarEstimativaDialog(quantidade: Int, kmAtual: Int, onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("Marcar $quantidade peças como trocadas?", style = MaterialTheme.typography.titleLarge) },
        text = {
            Text(
                "Todas ficam registradas como trocadas aos ${formatarKm(kmAtual)}. As que você trocou há mais tempo vão " +
                    "aparecer 'em dia' até você corrigir — é uma estimativa pra começar, não a verdade.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = { TextButton(onClick = onConfirmar) { Text("Marcar todas") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
}
