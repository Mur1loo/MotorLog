package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.domain.Recomendacao
import com.development.motorlog.domain.StatusTroca
import com.development.motorlog.domain.descreverDias
import com.development.motorlog.domain.ehRevisao
import com.development.motorlog.domain.estimarDiasAteTroca
import com.development.motorlog.R
import com.development.motorlog.ui.components.BarraDeProgresso
import com.development.motorlog.ui.components.BotaoSecundario
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.LinhaDeTiles
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.RegistrarTrocaDialog
import com.development.motorlog.ui.components.StatusDot
import com.development.motorlog.ui.theme.MlTextFaint
import com.development.motorlog.ui.theme.chakra
import com.development.motorlog.ui.theme.cor
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.util.iconeDaPeca
import com.development.motorlog.ui.viewModels.RegistroViewModel

// "Quando troca cada peça" — variante 'grupos' do protótipo (RecsGrupos): resumo em 3 cards,
// depois grupos por urgência que mapeiam 1:1 no StatusTroca, + "Nunca registrei" recolhido (D6).
private data class Grupo(val status: StatusTroca, val titulo: String, val subtitulo: String)

private val GRUPOS = listOf(
    Grupo(StatusTroca.VENCIDA, "Vencidas", "troque assim que possível"),
    Grupo(StatusTroca.PERTO, "Perto de vencer", "planeje a troca"),
    Grupo(StatusTroca.OK, "Mais adiante", "tudo sob controle"),
    Grupo(StatusTroca.NUNCA_TROCADA, "Nunca registrei a troca", "toque numa peça pra registrar a primeira troca"),
)

@Composable
fun TrocasScreen(
    moto: Moto,
    ritmoKmMes: Int?,
    onEditarPeca: (Peca) -> Unit,
    onRegistrarServico: () -> Unit,
    modifier: Modifier = Modifier,
    registroViewModel: RegistroViewModel = viewModel(),
) {
    val recomendacoes = registroViewModel.recomendacoes
    val pecas = registroViewModel.pecas
    var mostrarSemRegistro by rememberSaveable { mutableStateOf(false) }
    // peça do diálogo "Troquei agora" (id → sobrevive ao giro)
    var trocandoPecaId by rememberSaveable { mutableStateOf<Long?>(null) }
    val trocandoPeca = trocandoPecaId?.let { id -> pecas.find { it.id == id } }
    var confirmarEstimativa by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(moto) { registroViewModel.carregarRecomendacoes(moto) }

    val porStatus = recomendacoes.groupBy { it.statusTroca }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            LinhaDeTiles(Modifier.padding(bottom = 6.dp)) {
                ResumoStatus(StatusTroca.VENCIDA, porStatus[StatusTroca.VENCIDA].orEmpty().size, "vencidas", Modifier.weight(1f))
                ResumoStatus(StatusTroca.PERTO, porStatus[StatusTroca.PERTO].orEmpty().size, "em breve", Modifier.weight(1f))
                ResumoStatus(StatusTroca.OK, porStatus[StatusTroca.OK].orEmpty().size, "em dia", Modifier.weight(1f))
            }
        }
        GRUPOS.forEach { grupo ->
            val itens = porStatus[grupo.status].orEmpty()
            if (itens.isEmpty()) return@forEach
            val recolhido = grupo.status == StatusTroca.NUNCA_TROCADA && !mostrarSemRegistro

            item(key = "cab-${grupo.status}") {
                CabecalhoGrupo(
                    grupo = grupo,
                    quantidade = itens.size,
                    acao = if (grupo.status == StatusTroca.NUNCA_TROCADA) {
                        { mostrarSemRegistro = !mostrarSemRegistro }
                    } else null,
                    recolhido = recolhido,
                )
            }
            if (!recolhido && grupo.status == StatusTroca.NUNCA_TROCADA) {
                item(key = "estimar") {
                    val semRegistro = itens.filter { !it.ehRevisao }
                    MlCard(pad = 14.dp, cor = MaterialTheme.colorScheme.surfaceContainerHigh, borda = androidx.compose.ui.graphics.Color.Transparent) {
                        Text("Não lembra quando trocou?", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Marque estas ${semRegistro.size} peças como trocadas hoje, aos ${formatarKm(moto.kilometragem)}. " +
                                "Eu passo a contar o intervalo a partir daí, e você corrige as que lembrar tocando nelas.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(10.dp))
                        BotaoSecundario("Marcar todas como trocadas agora", { confirmarEstimativa = true }, Modifier.fillMaxWidth(), icone = R.drawable.ic_ml_check, enabled = semRegistro.isNotEmpty())
                    }
                }
            }
            if (!recolhido) {
                items(itens, key = { it.pecaId }) { rec ->
                    TrocaCard(rec, kmAtual = moto.kilometragem, ritmoKmMes = ritmoKmMes) { if (rec.ehRevisao) onRegistrarServico() else trocandoPecaId = rec.pecaId }
                }
            }
        }
        if (pecas.isEmpty()) {
            item { Text("Carregando peças…", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }

    if (confirmarEstimativa) {
        val semRegistro = porStatus[StatusTroca.NUNCA_TROCADA].orEmpty().filter { !it.ehRevisao }
        AlertDialog(
            onDismissRequest = { confirmarEstimativa = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text("Marcar ${semRegistro.size} peças como trocadas?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Todas ficam registradas como trocadas aos ${formatarKm(moto.kilometragem)}. As que você trocou há mais tempo vão " +
                        "aparecer 'em dia' até você corrigir — é uma estimativa pra começar, não a verdade.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEstimativa = false
                    val pecasAlvo = semRegistro.mapNotNull { rec -> pecas.find { it.id == rec.pecaId } }
                    registroViewModel.registrarTrocasEmLote(moto, pecasAlvo, moto.kilometragem)
                    mostrarSemRegistro = false
                }) { Text("Marcar todas") }
            },
            dismissButton = { TextButton(onClick = { confirmarEstimativa = false }) { Text("Cancelar") } },
        )
    }

    if (trocandoPeca != null) {
        RegistrarTrocaDialog(
            peca = trocandoPeca,
            kmAtual = moto.kilometragem,
            onConfirmar = { km ->
                registroViewModel.registrarTroca(moto, trocandoPeca, km)
                trocandoPecaId = null
            },
            onEditarPeca = {
                trocandoPecaId = null
                onEditarPeca(trocandoPeca)
            },
            onCancelar = { trocandoPecaId = null },
        )
    }
}

@Composable
private fun ResumoStatus(status: StatusTroca, quantidade: Int, rotulo: String, modifier: Modifier) {
    MlCard(modifier = modifier, pad = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusDot(status.cor(), tamanho = 10.dp)
            Column {
                Text(quantidade.toString(), style = chakra(20.sp), color = status.cor())
                Text(rotulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CabecalhoGrupo(grupo: Grupo, quantidade: Int, acao: (() -> Unit)?, recolhido: Boolean) {
    val cor = grupo.status.cor()
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusDot(cor, tamanho = 9.dp)
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(grupo.titulo, style = MaterialTheme.typography.titleMedium)
            Text(grupo.subtitulo, style = MaterialTheme.typography.bodySmall, color = MlTextFaint)
        }
        Text("$quantidade", style = chakra(15.sp), color = cor)
        if (acao != null) {
            TextButton(onClick = acao) { Text(if (recolhido) "mostrar" else "ocultar") }
        }
    }
}

@Composable
private fun TrocaCard(rec: Recomendacao, kmAtual: Int, ritmoKmMes: Int?, onClick: () -> Unit) {
    val cor = rec.statusTroca.cor()
    val ultima = rec.kmUltimaTroca
    val proxima = rec.kmProximaTroca
    val restante = rec.kmRestante

    MlCard(onClick = onClick, pad = 13.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBox(iconeDaPeca(rec.pecaNome), cor = if (rec.statusTroca == StatusTroca.NUNCA_TROCADA) null else cor)
            Column(modifier = Modifier.weight(1f)) {
                Text(rec.pecaNome, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (ultima != null && proxima != null) {
                    Text("${if (rec.ehRevisao) "revisão" else "troca"} a cada ${formatarKm(proxima - ultima)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (rec.ehRevisao) {
                    Text("registre a última revisão em \"Fui à oficina\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (restante != null) {
                val dias = estimarDiasAteTroca(restante, ritmoKmMes)
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatarNumero(if (restante < 0) -restante else restante), style = chakra(15.sp), color = cor, textAlign = TextAlign.End)
                    Text(
                        when {
                            restante == 0 -> "vence agora"
                            restante < 0 -> "km em atraso"
                            dias != null -> "km · ${descreverDias(dias)}"
                            else -> "km restantes"
                        },
                        style = MaterialTheme.typography.labelSmall, color = MlTextFaint,
                    )
                }
            }
        }
        if (ultima != null && proxima != null && proxima > ultima) {
            Spacer(Modifier.height(10.dp))
            val pctUsado = ((kmAtual - ultima).toFloat() / (proxima - ultima)).coerceIn(0f, 1f)
            BarraDeProgresso(pctUsado, cor)
            Spacer(Modifier.height(5.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("trocou aos ${formatarNumero(ultima)}", style = MaterialTheme.typography.labelSmall, color = MlTextFaint)
                Text("vence aos ${formatarNumero(proxima)}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
