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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.viewModels.RegistroViewModel

// Histórico da moto: serviços (oficina) + trocas avulsas (feitas por fora). Tocar num serviço abre o
// detalhe; tocar numa troca avulsa oferece excluir (ela não tem detalhe — é uma linha só).
@Composable
fun HistoricoScreen(
    moto: Moto,
    onAbrirServico: (Servico) -> Unit,
    modifier: Modifier = Modifier,
    registroViewModel: RegistroViewModel = viewModel(),
) {
    val servicos = registroViewModel.servicos
    val trocas = registroViewModel.trocasAvulsas
    val pecas = registroViewModel.pecas
    var excluindoId by rememberSaveable { mutableStateOf<Long?>(null) }
    val excluindo = trocas.find { it.id == excluindoId }

    // carrega (e recarrega ao trocar de moto) — efeito 1x ao entrar
    LaunchedEffect(moto) {
        registroViewModel.carregarServicos(moto)
        registroViewModel.carregarTrocasAvulsas(moto)
    }

    val ordenados = servicos.sortedByDescending { it.data }
    val trocasOrdenadas = trocas.sortedByDescending { it.kmTroca }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionLabel("Serviços na oficina")
            Text(
                if (ordenados.isEmpty()) "Nenhum serviço registrado ainda."
                else "${ordenados.size} serviço(s) · total R$ ${ordenados.sumOf { it.custo }}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(ordenados, key = { "s${it.id}" }) { servico ->
            ServicoCard(servico = servico, onClick = { onAbrirServico(servico) })
        }
        item {
            SectionLabel("Trocas avulsas", modifier = Modifier.padding(top = 8.dp))
            Text(
                if (trocasOrdenadas.isEmpty()) "Nenhuma troca registrada fora de serviço."
                else "${trocasOrdenadas.size} troca(s) · toque pra excluir uma registrada errada",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(trocasOrdenadas, key = { "t${it.id}" }) { troca ->
            val nome = pecas.find { it.id == troca.pecaId }?.nome ?: "Peça #${troca.pecaId}"
            Card(modifier = Modifier.fillMaxWidth().clickable { excluindoId = troca.id }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(nome, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text("${troca.kmTroca} km", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        }
    }

    if (excluindo != null) {
        val nome = pecas.find { it.id == excluindo.pecaId }?.nome ?: "esta peça"
        ConfirmarExclusaoDialog(
            texto = "Excluir a troca de $nome aos ${excluindo.kmTroca} km? A recomendação volta a valer pela troca anterior.",
            onConfirmar = {
                registroViewModel.deletarTrocaAvulsa(excluindo, moto)
                excluindoId = null
            },
            onCancelar = { excluindoId = null },
        )
    }
}

@Composable
private fun ServicoCard(servico: Servico, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(servico.tipoServico, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("R$ ${servico.custo}", fontWeight = FontWeight.Bold)
            }
            Text(
                "${formatarData(servico.data)} · ${servico.local}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "${servico.kilometragem} km",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
