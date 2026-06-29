package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Peca
import com.development.motorlog.ui.viewModels.RegistroViewModel

@Composable
fun GerenciarPecasScreen(
    modifier: Modifier = Modifier,
    viewModel: RegistroViewModel = viewModel(),
    onSalvarPeca: () -> Unit,
    onEditarPeca: (Peca) -> Unit
    )  {

    val pecas = viewModel.pecas

    Column(modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally) {

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(pecas) { peca ->
                Card(onClick = { onEditarPeca(peca)}) {
                    Text(peca.nome)
                    Text("a cada ${peca.intervaloKm}")
                }
            }
        }
        
        Button(onClick = onSalvarPeca) {
            Text("Adicionar Peca")
        }
    }
}