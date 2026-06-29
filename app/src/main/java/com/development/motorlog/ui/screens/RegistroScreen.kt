package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.viewModels.RegistroViewModel
import java.text.Normalizer

private fun semAcento(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")

@Composable
fun RegistroScreen(
    moto: Moto,
    viewModel: RegistroViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onSalvar: () -> Unit,
) {
    val pecas = viewModel.pecas
    var pecaSelecionada by remember { mutableStateOf<Peca?>(null) }
    var km by remember { mutableStateOf("") }
    var busca by remember { mutableStateOf("") }

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

        val pecasFiltradas = pecas.filter {
            semAcento(it.nome).contains(semAcento(busca), ignoreCase = true)
        }

        if (pecas.isEmpty()) {
            Text("Carregando peças...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else if (pecasFiltradas.isEmpty()) {
            Text("Nenhuma peça encontrada", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            pecasFiltradas.forEach { peca ->
                val selecionada = peca.id == pecaSelecionada?.id
                if (selecionada) {
                    Button(
                        onClick = { pecaSelecionada = peca },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(peca.nome) }
                } else {
                    OutlinedButton(
                        onClick = { pecaSelecionada = peca },
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
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val novoKm = km.toIntOrNull() ?: return@Button
                val peca = pecaSelecionada ?: return@Button
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
