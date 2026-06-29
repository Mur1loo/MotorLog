package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.viewModels.MotoViewModel

@Composable
fun CadastroScreen(
    modifier: Modifier = Modifier,
    viewModel: MotoViewModel = viewModel(),
    onSalvar: () -> Unit
) {
    var modelo by rememberSaveable { mutableStateOf("") }
    var placa by rememberSaveable { mutableStateOf("") }
    var ano by rememberSaveable { mutableStateOf("") }
    var km by rememberSaveable { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionLabel("Identificação")
        OutlinedTextField(
            value = modelo,
            onValueChange = { modelo = it },
            label = { Text("Modelo") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = placa,
            onValueChange = { placa = it },
            label = { Text("Placa") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = ano,
            onValueChange = { ano = it },
            label = { Text("Ano") },
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Quilometragem")
        OutlinedTextField(
            value = km,
            onValueChange = { km = it },
            label = { Text("Km atual") },
            modifier = Modifier.fillMaxWidth(),
        )

        val erroAtual = erro
        if (erroAtual != null) {
            Text(erroAtual, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                val newAno = ano.toIntOrNull()
                val newKm = km.toIntOrNull()
                if (newAno == null || newKm == null || modelo.isBlank() || placa.isBlank()) {
                    erro = "Campos não podem ser vazios!"
                    return@Button
                }
                viewModel.inserirMoto(
                    Moto(modelo = modelo, anoFabricacao = newAno, placa = placa, kilometragem = newKm)
                )
                onSalvar()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Salvar moto")
        }
    }
}
