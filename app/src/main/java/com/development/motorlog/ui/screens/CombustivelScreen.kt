package com.development.motorlog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Abastecimento
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.Confianca
import com.development.motorlog.domain.litrosParaTexto
import com.development.motorlog.ui.components.BotaoPrimario
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.LinhaDeTiles
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.PillNeutra
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.components.StatTile
import com.development.motorlog.ui.theme.chakra
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarKmPorLitro
import com.development.motorlog.ui.util.formatarReais
import com.development.motorlog.ui.util.formatarReaisCentavos
import com.development.motorlog.ui.util.formatarUmaCasa
import com.development.motorlog.ui.viewModels.AbastecimentoViewModel

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
