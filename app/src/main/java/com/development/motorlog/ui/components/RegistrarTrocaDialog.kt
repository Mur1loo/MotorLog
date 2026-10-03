package com.development.motorlog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.development.motorlog.R
import com.development.motorlog.data.Peca
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.domain.reaisParaTexto

// "Troquei agora" (troca por conta própria de UMA peça, sem sair da tela) e também a edição de
// uma troca já registrada (onExcluir != null). km nasce com o atual da moto; preço é opcional
// (vazio = não informado) e a data nasce hoje. onEditarPeca é a saída pra quem queria o intervalo.
@Composable
fun RegistrarTrocaDialog(
    peca: Peca,
    kmAtual: Int,
    onConfirmar: (km: Int, preco: Int, data: Long) -> Unit,
    onCancelar: () -> Unit,
    onEditarPeca: (() -> Unit)? = null,
    onExcluir: (() -> Unit)? = null,
    precoInicial: Int = 0,   // centavos
    dataInicial: Long = hojeUtcMillis(),
) {
    val editando = onExcluir != null
    var km by rememberSaveable { mutableStateOf(kmAtual.toString()) }
    var preco by rememberSaveable { mutableStateOf(if (precoInicial > 0) reaisParaTexto(precoInicial) else "") }
    var data by rememberSaveable { mutableLongStateOf(dataInicial) }
    val kmInt = km.toIntOrNull()
    val precoInt = reaisOpcional(preco)   // centavos

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text(if (editando) "Troca: ${peca.nome}" else "Troquei: ${peca.nome}", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "A próxima vence ${formatarKm(peca.intervaloKm)} depois do km informado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MlTextField(km, { km = it }, "Km da troca", Modifier.weight(1f), numerico = true, erro = kmInt == null)
                    CampoReais(preco, { preco = it }, "Valor da peça", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                CampoData(data, { data = it }, rotulo = "Quando", cor = MaterialTheme.colorScheme.surfaceContainerHighest)
                Spacer(Modifier.height(4.dp))
                Text(
                    "O valor é opcional — com ele, o gasto da moto fica completo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (onEditarPeca != null) TextButton(onClick = onEditarPeca) { Text("Editar peça / intervalo") }
                if (onExcluir != null) {
                    TextButton(onClick = onExcluir) { Text("Excluir esta troca", color = MaterialTheme.colorScheme.error) }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (kmInt != null && precoInt != null) onConfirmar(kmInt, precoInt, data) },
                enabled = kmInt != null && precoInt != null,
            ) { Text(if (editando) "Salvar" else "Registrar troca") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
}
