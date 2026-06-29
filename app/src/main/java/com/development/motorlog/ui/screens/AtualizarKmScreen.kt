package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.viewModels.MotoViewModel

@Composable
fun AtualizarKmScreen(
    modifier: Modifier = Modifier,
    moto: Moto,
    viewModel: MotoViewModel = viewModel(),
    onSalvar: (Moto) -> Unit,
) {
    var km by rememberSaveable { mutableStateOf(moto.kilometragem.toString()) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionLabel("Quilometragem atual")
        Text(
            "${moto.kilometragem} km",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = km,
            onValueChange = { km = it },
            label = { Text("Nova quilometragem") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                val novoKm = km.toIntOrNull() ?: return@Button
                val motoAtualizada = moto.copy(kilometragem = novoKm)
                viewModel.atualizarMoto(motoAtualizada)
                onSalvar(motoAtualizada)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Salvar km")
        }
    }
}
