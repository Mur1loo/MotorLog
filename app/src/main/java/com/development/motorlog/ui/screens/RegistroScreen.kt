package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.util.contemSemAcento
import com.development.motorlog.ui.viewModels.RegistroViewModel

// Troca avulsa. Busca + lista rolável no meio; km e Salvar sempre visíveis embaixo
// (antes ficavam depois dos ~50 botões — era preciso rolar tudo pra salvar).
@Composable
fun RegistroScreen(
    moto: Moto,
    modifier: Modifier = Modifier,
    viewModel: RegistroViewModel = viewModel(),
    onSalvar: () -> Unit,
) {
    val pecas = viewModel.pecas
    // só o id é salvo (sobrevive ao giro); a peça é resolvida no catálogo
    var pecaSelecionadaId by rememberSaveable { mutableStateOf<Long?>(null) }
    // a troca normalmente é registrada agora → nasce com o km atual da moto
    var km by rememberSaveable { mutableStateOf(moto.kilometragem.toString()) }
    var busca by rememberSaveable { mutableStateOf("") }
    val pecaSelecionada = pecas.find { it.id == pecaSelecionadaId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionLabel("Peça")
        OutlinedTextField(
            value = busca,
            onValueChange = { busca = it },
            label = { Text("Qual peça você trocou?") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        val pecasFiltradas = pecas.filter { it.nome.contemSemAcento(busca) }

        if (pecas.isEmpty()) {
            Text("Carregando peças...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else if (pecasFiltradas.isEmpty()) {
            Text("Nenhuma peça encontrada", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(pecasFiltradas, key = { it.id }) { peca ->
                if (peca.id == pecaSelecionadaId) {
                    Button(onClick = { pecaSelecionadaId = peca.id }, modifier = Modifier.fillMaxWidth()) { Text(peca.nome) }
                } else {
                    OutlinedButton(onClick = { pecaSelecionadaId = peca.id }, modifier = Modifier.fillMaxWidth()) { Text(peca.nome) }
                }
            }
        }

        SectionLabel("Quilometragem")
        OutlinedTextField(
            value = km,
            onValueChange = { km = it },
            label = { Text("Km na hora da troca") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val novoKm = km.toIntOrNull() ?: return@Button
                val peca = pecaSelecionada ?: return@Button
                viewModel.registrarTroca(moto, peca, novoKm)
                onSalvar()
            },
            enabled = pecaSelecionada != null && km.toIntOrNull() != null,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) {
            Text(if (pecaSelecionada == null) "Escolha a peça" else "Salvar troca: ${pecaSelecionada.nome}")
        }
    }
}
