package com.development.motorlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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


@Composable
fun EditarKmScreen(
    modifier: Modifier = Modifier,
    moto: Moto,
    viewModel: MotoViewModel = viewModel(),
    onSalvar: () -> Unit
) {
    var km by remember { mutableStateOf(moto.kilometragem.toString()) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Editar km — ${moto.modelo}", fontWeight = FontWeight.Bold)
        Text("Quilometragem Atual: ${moto.kilometragem}", fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = km,
            onValueChange = { novo -> km = novo },
            label = { Text("Quilometragem atual") },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val novoKm: Int = km.toIntOrNull() ?: return@Button
                val motoAtualizada = moto.copy(kilometragem = novoKm)
                viewModel.atualizarMoto(motoAtualizada)
                onSalvar()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Salvar km")
        }
    }
}