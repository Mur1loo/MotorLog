package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.development.motorlog.data.Registro
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.util.contemSemAcento
import com.development.motorlog.ui.viewModels.RegistroViewModel

@Composable
fun RegistroScreen(
    moto: Moto,
    viewModel: RegistroViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onSalvar: () -> Unit,
) {
    val pecas = viewModel.pecas
    // só o id é salvo (sobrevive ao giro); a peça é resolvida no catálogo
    var pecaSelecionadaId by rememberSaveable { mutableStateOf<Long?>(null) }
    // a troca normalmente é registrada agora → nasce com o km atual da moto
    var km by rememberSaveable { mutableStateOf(moto.kilometragem.toString()) }
    var busca by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionLabel("Peça")
        OutlinedTextField(
            value = busca,
            onValueChange = { busca = it },
            label = { Text("Buscar peça") },
            modifier = Modifier.fillMaxWidth(),
        )

        val pecasFiltradas = pecas.filter { it.nome.contemSemAcento(busca) }

        if (pecas.isEmpty()) {
            Text("Carregando peças...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else if (pecasFiltradas.isEmpty()) {
            Text("Nenhuma peça encontrada", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            pecasFiltradas.forEach { peca ->
                val selecionada = peca.id == pecaSelecionadaId
                if (selecionada) {
                    Button(
                        onClick = { pecaSelecionadaId = peca.id },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(peca.nome) }
                } else {
                    OutlinedButton(
                        onClick = { pecaSelecionadaId = peca.id },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(peca.nome) }
                }
            }
        }

        SectionLabel("Quilometragem")
        OutlinedTextField(
            value = km,
            onValueChange = { km = it },
            label = { Text("Km da troca") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val novoKm = km.toIntOrNull() ?: return@Button
                val peca = pecas.find { it.id == pecaSelecionadaId } ?: return@Button
                viewModel.inserirRegistro(
                    Registro(motoId = moto.id, pecaId = peca.id, kmTroca = novoKm, servicoId = null)
                )
                onSalvar()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Salvar troca")
        }
    }
}
