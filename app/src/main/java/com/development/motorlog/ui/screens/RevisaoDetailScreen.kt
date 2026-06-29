package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Servico
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.viewModels.RegistroViewModel

@Composable
fun RevisaoDetailScreen(
    servico: Servico,
    registroViewModel: RegistroViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onExcluido: () -> Unit,
) {
    val registros = registroViewModel.registrosDoServico
    val pecas = registroViewModel.pecas
    var confirmarExclusao by remember { mutableStateOf(false) }

    LaunchedEffect(servico) { registroViewModel.carregarRegistrosDoServico(servico.id) }

    val totalPecas = registros.sumOf { it.preco }
    val maoDeObra = (servico.custo - totalPecas).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── resumo ──
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LinhaInfo("Data", formatarData(servico.data))
                LinhaInfo("Oficina", servico.local)
                LinhaInfo("Quilometragem", "${servico.kilometragem} km")
            }
        }

        // ── peças trocadas ──
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "PEÇAS TROCADAS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (registros.isEmpty()) {
                    Text("Nenhuma peça registrada neste serviço.")
                } else {
                    registros.forEach { registro ->
                        val nome = pecas.find { it.id == registro.pecaId }?.nome
                            ?: "Peça #${registro.pecaId}"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(nome, modifier = Modifier.weight(1f))
                            Text("R$ ${registro.preco}", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // ── custo (peças + mão de obra = total) ──
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LinhaInfo("Peças", "R$ $totalPecas")
                LinhaInfo("Mão de obra", "R$ $maoDeObra")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("R$ ${servico.custo}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        TextButton(
            onClick = { confirmarExclusao = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Excluir serviço", color = MaterialTheme.colorScheme.error)
        }
    }

    if (confirmarExclusao) {
        ConfirmarExclusaoDialog(
            texto = "Excluir o serviço \"${servico.tipoServico}\"? As peças trocadas registradas nele perdem o vínculo.",
            onConfirmar = {
                confirmarExclusao = false
                registroViewModel.deletarServico(servico)
                onExcluido()
            },
            onCancelar = { confirmarExclusao = false },
        )
    }
}

@Composable
private fun LinhaInfo(rotulo: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(rotulo, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, fontWeight = FontWeight.Medium)
    }
}
