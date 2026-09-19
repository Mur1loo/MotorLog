package com.development.motorlog.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.development.motorlog.data.Peca

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
        title = { Text("Troquei: ${peca.nome}") },
        text = {
            Column {
                Text(
                    "A próxima vence ${peca.intervaloKm} km depois do km informado.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = km,
                    onValueChange = { km = it },
                    label = { Text("Km da troca") },
                    singleLine = true,
                    isError = kmInt == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = onEditarPeca) { Text("Editar peça / intervalo") }
            }
        },
        confirmButton = {
            TextButton(onClick = { kmInt?.let(onConfirmar) }, enabled = kmInt != null) { Text("Registrar troca") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
}
