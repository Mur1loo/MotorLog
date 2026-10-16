package com.development.motorlog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Abastecimento
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.Confianca
import com.development.motorlog.domain.Intervalo
import com.development.motorlog.domain.Periodo
import com.development.motorlog.domain.StatusTroca
import com.development.motorlog.domain.consumoNoPeriodo
import com.development.motorlog.domain.intervaloDoPeriodo
import com.development.motorlog.domain.litrosParaTexto
import com.development.motorlog.ui.components.BotaoPrimario
import com.development.motorlog.ui.components.CampoData
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.LinhaDeTiles
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.PillNeutra
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.components.StatTile
import com.development.motorlog.ui.theme.MlFormas
import com.development.motorlog.ui.theme.chakra
import com.development.motorlog.ui.theme.cor
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarKmPorLitro
import com.development.motorlog.ui.util.formatarReais
import com.development.motorlog.ui.util.formatarReaisCentavos
import com.development.motorlog.ui.util.formatarUmaCasa
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.viewModels.AbastecimentoViewModel
import kotlin.math.abs

// Combustível da moto: consumo médio (com o quanto confiar nele), média recente, preço do litro,
// custo por km e a lista de abastecimentos (tocar edita). Funciona só com abastecimentos parciais;
// tanque cheio, quando acontece, deixa a conta exata (domain/Consumo.kt).
@Composable
fun CombustivelScreen(
    moto: Moto,
    modifier: Modifier = Modifier,
    viewModel: AbastecimentoViewModel = viewModel(),
    onAbasteci: () -> Unit,
    onEditar: (Abastecimento) -> Unit,
) {
    LaunchedEffect(moto.id) { viewModel.carregar(moto.id) }
    val resumo = viewModel.resumo
    val lista = viewModel.abastecimentos

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            LinhaDeTiles {
                val media = resumo?.media
                StatTile(
                    "Média", media?.kmPorLitro?.let(::formatarUmaCasa) ?: "—",
                    Modifier.weight(1f), unidade = if (media != null) "km/l" else null, icone = R.drawable.ic_ml_fuel,
                ) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when {
                            media == null && (resumo?.faltamAbastecimentos ?: 0) > 0 ->
                                "faltam ${resumo?.faltamAbastecimentos} abastecimento${if (resumo?.faltamAbastecimentos == 1) "" else "s"} pra calcular"
                            media == null -> "rode mais um pouco pra calcular"
                            media.confianca == Confianca.EXATA -> "exata: de tanque cheio a tanque cheio"
                            media.confianca == Confianca.BOA -> "boa precisão (${litrosParaTexto(media.mililitros)} L na conta)"
                            else -> "aproximada: melhora a cada abastecimento"
                        },
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    resumo?.recente?.let {
                        Text("últimos: ${formatarKmPorLitro(it.kmPorLitro)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                StatTile(
                    "Combustível por km", resumo?.custoPorKm?.let(::formatarReaisCentavos) ?: "—",
                    Modifier.weight(1f), icone = R.drawable.ic_ml_dollar,
                ) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        resumo?.precoPorLitro?.let { "litro a ${formatarReaisCentavos(it / 100.0)}" } ?: "informe o valor pago",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item { BotaoPrimario("Abasteci", onAbasteci, icone = R.drawable.ic_ml_fuel) }
        if (lista.isNotEmpty()) item { CardConsumoNoPeriodo(lista, resumo?.media?.kmPorLitro) }
        item {
            MlCard(pad = 4.dp) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    SectionLabel("Abastecimentos · ${lista.size}")
                    if (lista.isEmpty()) {
                        Text(
                            "Registre cada vez que abastecer, mesmo um pouco, com o km do painel. Do 3º abastecimento em diante " +
                                "eu mostro o consumo médio — não precisa encher o tanque. E o km da moto se atualiza sozinho.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    lista.forEachIndexed { i, a ->
                        if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        LinhaAbastecimento(a) { onEditar(a) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun LinhaAbastecimento(a: Abastecimento, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "editar") { onClick() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconBox(R.drawable.ic_ml_fuel, cor = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f)) {
            Text("${litrosParaTexto(a.mililitros)} L · ${formatarKm(a.km)}", style = MaterialTheme.typography.titleSmall)
            Text(formatarData(a.data), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!a.tanqueCheio) PillNeutra("parcial")
        if (a.valor > 0) Text(formatarReais(a.valor), style = chakra(14.sp))
    }
}

// Consumo num período (este mês, mês passado, datas escolhidas) comparado com a média geral:
// mostra se a moto passou a beber mais (vela, filtro de ar, pneu murcho, trânsito) ou menos.
@Composable
private fun CardConsumoNoPeriodo(lista: List<Abastecimento>, mediaGeral: Double?) {
    val hoje = hojeUtcMillis()
    var periodo by rememberSaveable { mutableStateOf(Periodo.ESTE_MES) }
    val inicial = intervaloDoPeriodo(Periodo.PERSONALIZADO, hoje)
    var de by rememberSaveable { mutableLongStateOf(inicial.inicio) }
    var ate by rememberSaveable { mutableLongStateOf(inicial.fim) }
    val intervalo = if (periodo == Periodo.PERSONALIZADO) Intervalo(minOf(de, ate), maxOf(de, ate)) else intervaloDoPeriodo(periodo, hoje)
    val r = remember(lista, intervalo) { consumoNoPeriodo(lista, intervalo) }

    MlCard {
        SectionLabel("Consumo no período")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Periodo.entries.forEach { p ->
                FilterChip(selected = periodo == p, onClick = { periodo = p }, label = { Text(p.rotulo) }, shape = MlFormas.pill)
            }
        }
        if (periodo == Periodo.PERSONALIZADO) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoData(de, { de = it }, Modifier.weight(1f), rotulo = "De", cor = MaterialTheme.colorScheme.surfaceContainerHigh)
                CampoData(ate, { ate = it }, Modifier.weight(1f), rotulo = "Até", cor = MaterialTheme.colorScheme.surfaceContainerHigh)
            }
        } else {
            Text(
                "${formatarData(intervalo.inicio)} a ${formatarData(intervalo.fim)}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        if (r.abastecimentos == 0) {
            Text("Nenhum abastecimento nesse período.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val consumo = r.consumo
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(consumo?.kmPorLitro?.let(::formatarUmaCasa) ?: "—", style = chakra(32.sp))
                if (consumo != null) Text("km/l", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
            }
            val diferenca = if (consumo != null && mediaGeral != null) consumo.kmPorLitro - mediaGeral else null
            Text(
                when {
                    consumo == null -> "Abastecimentos ou km rodados de menos nesse período pra calcular o km/l."
                    diferenca == null -> if (consumo.confianca == Confianca.APROXIMADA) "aproximado: melhora com mais abastecimentos" else "no período"
                    abs(diferenca) < 0.5 -> "igual à média geral (${formatarKmPorLitro(mediaGeral!!)})"
                    diferenca > 0 -> "${formatarUmaCasa(diferenca)} km/l melhor que a média geral (${formatarKmPorLitro(mediaGeral!!)})"
                    else -> "${formatarUmaCasa(-diferenca)} km/l pior que a média geral (${formatarKmPorLitro(mediaGeral!!)})"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    diferenca == null || abs(diferenca) < 0.5 -> MaterialTheme.colorScheme.onSurfaceVariant
                    diferenca > 0 -> StatusTroca.OK.cor()
                    else -> StatusTroca.PERTO.cor()
                },
            )
            Spacer(Modifier.height(10.dp))
            LinhaDoPeriodo("Km rodados", r.kmRodados?.let(::formatarKm) ?: "—")
            LinhaDoPeriodo("Abastecimentos", "${r.abastecimentos} · ${litrosParaTexto(r.mililitros)} L")
            if (r.gasto > 0) LinhaDoPeriodo("Gasto com combustível", formatarReais(r.gasto))
            r.precoMedio?.let { LinhaDoPeriodo("Preço médio do litro", formatarReaisCentavos(it / 100.0)) }
            r.custoPorKm?.let { LinhaDoPeriodo("Combustível por km", formatarReaisCentavos(it)) }
        }
    }
}

@Composable
private fun LinhaDoPeriodo(rotulo: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(valor, style = chakra(14.sp))
    }
}
