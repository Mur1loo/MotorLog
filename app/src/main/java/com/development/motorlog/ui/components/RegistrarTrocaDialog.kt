package com.development.motorlog.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.development.motorlog.R
import com.development.motorlog.data.Peca
import com.development.motorlog.ui.util.formatarKm

// "Troquei agora": registra a troca de UMA peça sem sair da tela. km nasce com o atual da moto.
// onEditarPeca é a saída secundária pra quem queria mexer no intervalo.
@Composable
fun RegistrarTrocaDialog(
    peca: Peca,
    kmAtual: Int,
    onConfirmar: (km: Int) -> Unit,
    onEditarPeca: () -> Unit,
    onCancelar: () -> Unit,
) {
    var km by rememberSaveable { mutableStateOf(kmAtual.toString()) }
    val kmInt = km.toIntOrNull()

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("Troquei: ${peca.nome}", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(
                    "A próxima vence ${formatarKm(peca.intervaloKm)} depois do km informado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                MlTextField(km, { km = it }, "Km da troca", icone = R.drawable.ic_ml_gauge, numerico = true, erro = kmInt == null)
                TextButton(onClick = onEditarPeca) { Text("Editar peça / intervalo") }
            }
        },
        confirmButton = {
            TextButton(onClick = { kmInt?.let(onConfirmar) }, enabled = kmInt != null) { Text("Registrar troca") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
}
