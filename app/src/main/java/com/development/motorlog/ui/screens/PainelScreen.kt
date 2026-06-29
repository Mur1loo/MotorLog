package com.development.motorlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Servico
import com.development.motorlog.domain.StatusTroca
import com.development.motorlog.ui.viewModels.RegistroViewModel
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.theme.MlOk
import com.development.motorlog.ui.theme.MlOver
import com.development.motorlog.ui.theme.MlSoon
import com.development.motorlog.ui.theme.MlTextFaint


@Composable
fun PainelScreen(
    modifier: Modifier = Modifier,
    moto: Moto,
    registroViewModel: RegistroViewModel = viewModel(),
    onAtualizarKm: () -> Unit,
    onRegistrarTroca: () -> Unit,
    onRegistrarServico: () -> Unit,
    onVerHistorico: () -> Unit,
    onExcluirMoto: () -> Unit,
    onEditarPeca: (Peca) -> Unit,
    onAbrirServico: (Servico) -> Unit
) {
    val recomendacoes = registroViewModel.recomendacoes
    val servicos = registroViewModel.servicos
    val pecas = registroViewModel.pecas
    // no painel só interessam as peças JÁ com registro (sem as "nunca trocadas")
    val proximasTrocas = recomendacoes.filter { it.statusTroca != StatusTroca.NUNCA_TROCADA }
    var confirmarExclusao by remember { mutableStateOf(false) }

    LaunchedEffect(moto) {
        registroViewModel.carregarRecomendacoes(moto)
        registroViewModel.carregarServicos(moto)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── HERO: km em destaque + ação protagonista ──────────
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "QUILOMETRAGEM ATUAL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${moto.kilometragem}", fontSize = 52.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Text(
                    "km",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Button(
                onClick = { onAtualizarKm() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) { Text("Atualizar agora", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }

        // ── tiles: gasto + nº de serviços ──
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PainelStat("GASTO TOTAL", "R$ ${servicos.sumOf { it.custo }}", Modifier.weight(1f))
            PainelStat("SERVIÇOS", "${servicos.size}", Modifier.weight(1f))
        }

        // ── ações secundárias ──
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onRegistrarTroca() }, modifier = Modifier.weight(1f)) { Text("Troca") }
            OutlinedButton(onClick = { onRegistrarServico() }, modifier = Modifier.weight(1f)) { Text("Serviço") }
            OutlinedButton(onClick = { onVerHistorico() }, modifier = Modifier.weight(1f)) { Text("Histórico") }
        }

        // ── Card: próximas trocas ──────────────────────────────
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    "PRÓXIMAS TROCAS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (proximasTrocas.isEmpty()) {
                    Text("Nenhuma troca registrada ainda.")
                } else {
                    proximasTrocas.forEach { rec ->
                        val cor = when (rec.statusTroca) {
                            StatusTroca.OK -> MlOk
                            StatusTroca.PERTO -> MlSoon
                            StatusTroca.VENCIDA -> MlOver
                            StatusTroca.NUNCA_TROCADA -> MlTextFaint
                        }
                        val texto = when (rec.statusTroca) {
                            StatusTroca.NUNCA_TROCADA -> "sem histórico"
                            StatusTroca.VENCIDA -> "vencido há ${-(rec.kmRestante ?: 0)} km"
                            else -> "faltam ${rec.kmRestante ?: 0} km"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pecas.find { it.id == rec.pecaId }?.let { onEditarPeca(it) }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(cor),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                rec.pecaNome,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(texto, color = cor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ── Card: atividade recente (preview dos últimos serviços) ──
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    "ATIVIDADE RECENTE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val ultimosServicos = servicos.sortedByDescending { it.data }.take(3)
                if (ultimosServicos.isEmpty()) {
                    Text("Nenhum serviço registrado ainda.")
                } else {
                    ultimosServicos.forEach { servico ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAbrirServico(servico) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(servico.tipoServico, fontWeight = FontWeight.Medium)
                                Text(
                                    "${formatarData(servico.data)} · ${servico.kilometragem} km",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                "R$ ${servico.custo}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }

        TextButton(
            onClick = { confirmarExclusao = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Excluir moto", color = MaterialTheme.colorScheme.error)
        }
    }

    if (confirmarExclusao) {
        ConfirmarExclusaoDialog(
            texto = "Excluir esta moto e todo o histórico dela? Esta ação não pode ser desfeita.",
            onConfirmar = {
                confirmarExclusao = false
                onExcluirMoto()
            },
            onCancelar = { confirmarExclusao = false },
        )
    }
}

@Composable
private fun PainelStat(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                rotulo,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(valor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
