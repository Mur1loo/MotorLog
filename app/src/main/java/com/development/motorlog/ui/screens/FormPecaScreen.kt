package com.development.motorlog.ui.screens

    import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Peca
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.viewModels.RegistroViewModel

@Composable
fun FormPecaScreen(
    peca : Peca?,
    viewModel: RegistroViewModel = viewModel(),
    modifier : Modifier = Modifier,
    onSalvar: () -> Unit,
    ){
    var nome by rememberSaveable { mutableStateOf(peca?.nome ?: "") }
    var intervalo by rememberSaveable { mutableStateOf( peca?.intervaloKm?.toString() ?: "")}
    var erro by remember { mutableStateOf<String?>(null) }
    var confirmarExclusao by remember { mutableStateOf(false) }

    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = nome,
            onValueChange = { novo -> nome = novo },
            label = { Text("Peça") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = intervalo,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Intervalo de troca") },
            onValueChange = { novo -> intervalo = novo }
        )

        if (erro != null) {
            Text("${erro}", color = MaterialTheme.colorScheme.error)
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val newIntervalo = intervalo.toIntOrNull()

                if (newIntervalo == null || nome.isBlank()) {
                    erro = "Campos não podem ficar vazios!"
                    return@Button
                }
                erro = null

                if (peca != null) {
                    val newPeca = peca.copy(nome = nome, intervaloKm = newIntervalo)
                    viewModel.atualizarPeca(newPeca)
                    onSalvar()
                    return@Button
                }

                val novaPeca = Peca(nome = nome, intervaloKm = newIntervalo)
                viewModel.inserirPeca(novaPeca)
                onSalvar()
            }) {
            Text("Salvar a Peça")
        }

        if (peca != null) {
            TextButton(
                onClick = { confirmarExclusao = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Excluir peça", color = MaterialTheme.colorScheme.error)
            }
            if (confirmarExclusao) {
                ConfirmarExclusaoDialog(
                    texto = "Excluir a peça \"${peca.nome}\" e as trocas registradas dela?",
                    onConfirmar = {
                        confirmarExclusao = false
                        viewModel.deletarPeca(peca)
                        onSalvar()
                    },
                    onCancelar = { confirmarExclusao = false },
                )
            }
        }
    }
}