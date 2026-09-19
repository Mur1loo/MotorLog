package com.development.motorlog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Peca
import com.development.motorlog.ui.util.contemSemAcento
import com.development.motorlog.ui.viewModels.RegistroViewModel

@Composable
fun GerenciarPecasScreen(
    modifier: Modifier = Modifier,
    viewModel: RegistroViewModel = viewModel(),
    onSalvarPeca: () -> Unit,
    onEditarPeca: (Peca) -> Unit,
) {
    val pecas = viewModel.pecas
    var busca by rememberSaveable { mutableStateOf("") }
    val pecasFiltradas = pecas.filter { it.nome.contemSemAcento(busca) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = busca,
            onValueChange = { busca = it },
            label = { Text("Buscar peça") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "${pecasFiltradas.size} de ${pecas.size} peças",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(pecasFiltradas, key = { it.id }) { peca ->
                PecaCard(peca) { onEditarPeca(peca) }
            }
        }
        Button(onClick = onSalvarPeca, modifier = Modifier.fillMaxWidth()) {
            Text("Adicionar peça")
        }
    }
}

@Composable
private fun PecaCard(peca: Peca, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(peca.nome, fontWeight = FontWeight.Medium)
                Text(
                    "a cada ${peca.intervaloKm} km",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
