package com.development.motorlog.ui.screens

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Servico
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.viewModels.RegistroViewModel

@Composable
fun HistoricoScreen(
    moto: Moto,
    registroViewModel: RegistroViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val servicos = registroViewModel.servicos
    var servicoParaExcluir by remember { mutableStateOf<Servico?>(null) }

    // carrega (e recarrega ao trocar de moto) — efeito 1x ao entrar
    LaunchedEffect(moto) { registroViewModel.carregarServicos(moto) }

    // mais recentes primeiro
    val ordenados = servicos.sortedByDescending { it.data }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Histórico de serviços", fontWeight = FontWeight.Bold, fontSize = 22.sp)

        if (ordenados.isEmpty()) {
            Text(
                "Nenhum serviço registrado ainda.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                "${ordenados.size} serviço(s) · total R$ ${ordenados.sumOf { it.custo }}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(ordenados) { servico ->
                    ServicoCard(servico = servico, onExcluir = { servicoParaExcluir = servico })
                }
            }
        }
    }

    val alvo = servicoParaExcluir
    if (alvo != null) {
        ConfirmarExclusaoDialog(
            texto = "Excluir o serviço \"${alvo.tipoServico}\"?",
            onConfirmar = {
                registroViewModel.deletarServico(alvo)
                servicoParaExcluir = null
            },
            onCancelar = { servicoParaExcluir = null },
        )
    }
}

@Composable
private fun ServicoCard(servico: Servico, onExcluir: () -> Unit) {
    Card(onClick = onExcluir, modifier = Modifier.fillMaxWidth()) {
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
