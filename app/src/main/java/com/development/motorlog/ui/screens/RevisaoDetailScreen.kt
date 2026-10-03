package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Servico
import com.development.motorlog.ui.components.BotaoSecundario
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.theme.chakra
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.util.formatarReais
import com.development.motorlog.ui.util.iconeDaPeca
import com.development.motorlog.ui.viewModels.RegistroViewModel

// Detalhe da visita à oficina (RevisaoDetailScreen do protótipo): resumo em 3 colunas com ícones,
// peças com ícone e preço em Chakra, e o card escuro de custo com o total em accent.
@Composable
fun RevisaoDetailScreen(
    servico: Servico,
    accent: Color,
    modifier: Modifier = Modifier,
    registroViewModel: RegistroViewModel = viewModel(),
    onEditar: () -> Unit,
    onExcluido: () -> Unit,
) {
    val registros = registroViewModel.registrosDoServico
    val pecas = registroViewModel.pecas
    var confirmarExclusao by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(servico) { registroViewModel.carregarRegistrosDoServico(servico.id) }

    val totalPecas = registros.sumOf { it.preco }
    val maoDeObra = (servico.custo - totalPecas).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ── resumo: km · data · oficina ──
        MlCard {
            Row(Modifier.fillMaxWidth()) {
                ColunaResumo(R.drawable.ic_ml_gauge, formatarNumero(servico.kilometragem), "km", accent, Modifier.weight(1f), numero = true)
                ColunaResumo(R.drawable.ic_ml_calendar, formatarData(servico.data), null, accent, Modifier.weight(1f))
                ColunaResumo(R.drawable.ic_ml_pin, servico.local.ifBlank { "Oficina não informada" }, null, accent, Modifier.weight(1f))
            }
        }

        // ── peças trocadas ──
        Column {
            SectionLabel("Peças trocadas · ${registros.size}")
            MlCard(pad = 4.dp) {
                if (registros.isEmpty()) {
                    Text(
                        "Nenhuma peça registrada nesta visita (só mão de obra ou serviço).",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                    )
                } else {
                    registros.forEachIndexed { i, registro ->
                        val nome = pecas.find { it.id == registro.pecaId }?.nome ?: "Peça #${registro.pecaId}"
                        if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(13.dp),
                        ) {
                            IconBox(iconeDaPeca(nome), tamanho = 40.dp)
                            Text(nome, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(formatarReais(registro.preco), style = chakra(14.sp))
                        }
                    }
                }
            }
        }

        // ── custo: peças + mão de obra = total (card escuro) ──
        MlCard(cor = Color(0xFF0B0D11), borda = Color.Transparent) {
            LinhaCusto("Peças", formatarReais(totalPecas))
            Spacer(Modifier.height(7.dp))
            LinhaCusto("Mão de obra", formatarReais(maoDeObra))
            Spacer(Modifier.height(11.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
            Spacer(Modifier.height(11.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
                Text(formatarReais(servico.custo), style = chakra(24.sp), color = accent)
            }
        }

        BotaoSecundario("Editar esta visita", onEditar, Modifier.fillMaxWidth(), icone = R.drawable.ic_ml_edit)
        TextButton(onClick = { confirmarExclusao = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Excluir", color = MaterialTheme.colorScheme.error)
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
private fun ColunaResumo(icone: Int, valor: String, unidade: String?, accent: Color, modifier: Modifier, numero: Boolean = false) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(painterResource(icone), contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                valor,
                style = if (numero) chakra(14.sp) else MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            if (unidade != null) Text(" $unidade", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LinhaCusto(rotulo: String, valor: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
        Text(valor, style = chakra(14.sp), color = Color.White)
    }
}
