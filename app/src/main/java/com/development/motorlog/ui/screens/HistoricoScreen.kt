package com.development.motorlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import com.development.motorlog.domain.StatusTroca
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.components.LinhaDeTiles
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.PillNeutra
import com.development.motorlog.ui.components.StatTile
import com.development.motorlog.ui.theme.MlBorder
import com.development.motorlog.ui.theme.MlFormas
import com.development.motorlog.ui.theme.MlTextFaint
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.theme.chakra
import com.development.motorlog.ui.theme.cor
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.util.formatarReais
import com.development.motorlog.ui.util.iconeDaPeca
import com.development.motorlog.ui.viewModels.RegistroViewModel

private enum class Filtro(val rotulo: String) { TODAS("Tudo"), SERVICOS("Oficina"), AVULSAS("Avulsas") }

// Histórico da moto no estilo do protótipo: tiles (registros/total), filtro em chips e uma linha
// do tempo com nó-ícone à esquerda. Serviço abre o detalhe; troca avulsa oferece excluir.
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
    val accent = accentDaMoto(moto.id)
    var filtro by rememberSaveable { mutableStateOf(Filtro.TODAS) }
    var excluindoId by rememberSaveable { mutableStateOf<Long?>(null) }
    val excluindo = trocas.find { it.id == excluindoId }

    LaunchedEffect(moto) {
        registroViewModel.carregarServicos(moto)
        registroViewModel.carregarTrocasAvulsas(moto)
    }

    // linha do tempo única, do mais recente pro mais antigo (por km, que é o eixo do app)
    val itens: List<Any> = buildList {
        if (filtro != Filtro.AVULSAS) addAll(servicos)
        if (filtro != Filtro.SERVICOS) addAll(trocas)
    }.sortedByDescending { if (it is Servico) it.kilometragem else (it as Registro).kmTroca }
    val totalGasto = servicos.sumOf { it.custo } + trocas.sumOf { it.preco }

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            LinhaDeTiles(Modifier.padding(bottom = 12.dp)) {
                StatTile("Registros", (servicos.size + trocas.size).toString(), Modifier.weight(1f), icone = R.drawable.ic_ml_doc)
                StatTile("Total investido", formatarReais(totalGasto), Modifier.weight(1.6f), icone = R.drawable.ic_ml_dollar)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                Filtro.entries.forEach { f ->
                    FilterChip(selected = filtro == f, onClick = { filtro = f }, label = { Text(f.rotulo) }, shape = MlFormas.pill)
                }
            }
        }
        if (itens.isEmpty()) {
            item {
                MlCard {
                    Text(
                        when (filtro) {
                            Filtro.AVULSAS -> "Nenhuma troca registrada fora da oficina. Use \"Troquei uma peça\" no painel."
                            Filtro.SERVICOS -> "Nenhuma visita à oficina registrada. Use \"Fui à oficina\" no painel."
                            Filtro.TODAS -> "Nada registrado ainda. Cada troca e cada visita à oficina aparecem aqui, na ordem em que aconteceram."
                        },
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        items(itens, key = { if (it is Servico) "s${it.id}" else "t${(it as Registro).id}" }) { item ->
            if (item is Servico) {
                LinhaDoTempo(icone = R.drawable.ic_ml_wrench, cor = accent) {
                    ServicoCard(item, pecas.size, registroViewModel, accent) { onAbrirServico(item) }
                }
            } else {
                val troca = item as Registro
                val nome = pecas.find { it.id == troca.pecaId }?.nome ?: "Peça #${troca.pecaId}"
                LinhaDoTempo(icone = iconeDaPeca(nome), cor = StatusTroca.OK.cor()) {
                    MlCard(onClick = { excluindoId = troca.id }, pad = 13.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(nome, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${formatarKm(troca.kmTroca)} · fora da oficina", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                PillNeutra("Avulsa")
                                if (troca.preco > 0) Text(formatarReais(troca.preco), style = chakra(13.5.sp))
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }

    if (excluindo != null) {
        val nome = pecas.find { it.id == excluindo.pecaId }?.nome ?: "esta peça"
        ConfirmarExclusaoDialog(
            texto = "Excluir a troca de $nome aos ${formatarKm(excluindo.kmTroca)}? A recomendação volta a valer pela troca anterior.",
            onConfirmar = {
                registroViewModel.deletarTrocaAvulsa(excluindo, moto)
                excluindoId = null
            },
            onCancelar = { excluindoId = null },
        )
    }
}

// nó da linha do tempo (TimelineNode): quadrado com borda na cor + ícone; a linha vertical fica à esquerda
@Composable
private fun LinhaDoTempo(icone: Int, cor: Color, conteudo: @Composable () -> Unit) {
    // a linha vertical atravessa o padding inferior → fica contínua entre os itens
    Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.width(40.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.width(2.dp).fillMaxHeight().background(MlBorder))
            Box(
                Modifier.size(40.dp).clip(MlFormas.caixaIcone).background(MaterialTheme.colorScheme.surface).border(2.dp, cor, MlFormas.caixaIcone),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(icone), contentDescription = null, tint = cor, modifier = Modifier.size(20.dp))
            }
        }
        Box(Modifier.weight(1f).padding(bottom = 12.dp)) { conteudo() }
    }
}

@Composable
private fun ServicoCard(servico: Servico, totalPecasCatalogo: Int, vm: RegistroViewModel, accent: Color, onClick: () -> Unit) {
    MlCard(onClick = onClick, pad = 14.dp) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(servico.tipoServico, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${formatarData(servico.data)} · ${servico.local}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatarNumero(servico.kilometragem), style = chakra(15.sp))
                Text("km", style = MaterialTheme.typography.labelSmall, color = MlTextFaint)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_ml_chev_r), contentDescription = null, tint = MlTextFaint, modifier = Modifier.size(16.dp))
            Text("ver peças e custo", style = MaterialTheme.typography.bodySmall, color = MlTextFaint, modifier = Modifier.weight(1f))
            Text(formatarReais(servico.custo), style = chakra(15.sp), color = accent)
        }
    }
}
