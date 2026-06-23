package com.development.motorlog.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto

@Composable
fun GaragemScreen(
    viewModel: MotoViewModel = viewModel(),
    modifier: Modifier,
    onAdicionar: () -> Unit,
    onEditarMoto: (Moto) -> Unit
) {

    val motos = viewModel.motos

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button( onClick = onAdicionar ) { Text("Adicionar Moto") }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(motos) { moto ->
                MotoCard(moto, {onEditarMoto(moto)})
            }

        }
    }

}

@Composable
fun MotoCard(moto: Moto, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = moto.modelo, fontWeight = FontWeight.Bold)
            Text(text = moto.placa)
            Text(text = moto.kilometragem.toString())
        }
    }
}
