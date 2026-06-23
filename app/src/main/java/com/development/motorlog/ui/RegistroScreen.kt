package com.development.motorlog.ui

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro

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

    Column(
        modifier = modifier.fillMaxSize().
            padding(16.dp).
            verticalScroll(rememberScrollState()).
            imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),

    ) {
        Text("Registrar troca — ${moto.modelo}", fontWeight = FontWeight.Bold)

        Text("Escolha a peça:")

        if (pecas.isEmpty()) {
            Text("Carregando peças...")
        } else {
            pecas.forEach { peca ->
                Button(onClick = { pecaSelecionada = peca }) { Text(peca.nome) }
            }
        }
        Text("Selecionada: ${pecaSelecionada?.nome ?: "nenhuma"}")

        OutlinedTextField(
            value = km,
            onValueChange = { novo -> km = novo },
            label = { Text("Km da troca") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val novoKm = km.toIntOrNull() ?: return@Button
                val peca = pecaSelecionada ?: return@Button
                val novoRegistro = Registro(
                    motoId = moto.id,
                    pecaId = peca.id,
                    kmTroca = novoKm
                )
                viewModel.inserirRegistro(novoRegistro)
                onSalvar()

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar troca")
        }
    }
}