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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.viewModels.MotoViewModel

@Composable
// moto == null → cadastro; moto != null → edição de identificação (o km se edita em "Atualizar km")
fun CadastroScreen(
    modifier: Modifier = Modifier,
    moto: Moto? = null,
    viewModel: MotoViewModel = viewModel(),
    onSalvar: () -> Unit
) {
    var modelo by rememberSaveable { mutableStateOf(moto?.modelo ?: "") }
    var placa by rememberSaveable { mutableStateOf(moto?.placa ?: "") }
    var ano by rememberSaveable { mutableStateOf(moto?.anoFabricacao?.toString() ?: "") }
    var km by rememberSaveable { mutableStateOf(moto?.kilometragem?.toString() ?: "") }
    var erro by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        if (moto == null) {
            SectionLabel("Quilometragem")
            OutlinedTextField(
                value = km,
                onValueChange = { km = it },
                label = { Text("Km atual") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }

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
                if (newKm < 0 || newAno !in 1900..2100) {
                    erro = "Ano ou quilometragem inválidos."
                    return@Button
                }
                erro = null
                if (moto != null) {
                    viewModel.atualizarMoto(moto.copy(modelo = modelo, placa = placa, anoFabricacao = newAno))
                } else {
                    viewModel.inserirMoto(
                        Moto(modelo = modelo, anoFabricacao = newAno, placa = placa, kilometragem = newKm)
                    )
                }
                onSalvar()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (moto != null) "Salvar alterações" else "Salvar moto")
        }
    }
}
