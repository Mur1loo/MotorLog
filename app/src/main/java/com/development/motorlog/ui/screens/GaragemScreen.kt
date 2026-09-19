package com.development.motorlog.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.DIAS_PARA_LEMBRAR_KM
import com.development.motorlog.domain.ResumoAlertas
import com.development.motorlog.domain.StatusTroca
import com.development.motorlog.domain.diasEntre
import com.development.motorlog.ui.components.BikeBadge
import com.development.motorlog.ui.components.BotaoSecundario
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.LinhaDeTiles
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.Odometer
import com.development.motorlog.ui.components.Pill
import com.development.motorlog.ui.components.PillNeutra
import com.development.motorlog.ui.components.StatTile
import com.development.motorlog.ui.components.TamanhoOdometro
import com.development.motorlog.ui.theme.MlBorderHi
import com.development.motorlog.ui.theme.MlTextFaint
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.theme.cor
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.viewModels.MotoViewModel

@Composable
fun GaragemScreen(
    modifier: Modifier = Modifier,
    viewModel: MotoViewModel = viewModel(),
    onAdicionar: () -> Unit,
    onEditarMoto: (Moto) -> Unit,
    onEditarPeca: () -> Unit,
) {
    val motos = viewModel.motos
    val alertas = viewModel.alertas
    val ritmos = viewModel.ritmos
    val totalKm = motos.sumOf { it.kilometragem }
    val totalVencidas = alertas.values.sumOf { it.vencidas }
    val contexto = LocalContext.current

    // trocas/serviços registrados em outras telas mudam os alertas → recarrega ao entrar
    LaunchedEffect(Unit) { viewModel.carregarMotos() }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                if (!viewModel.carregou) {
                    // ainda lendo o banco: nada a mostrar (evita piscar o estado vazio)
                } else if (motos.isEmpty()) {
                    // ── primeira abertura: explica a tese em 2 linhas em vez de mostrar zeros ──
                    MlCard(pad = 20.dp) {
                        Text("Seu caderninho de manutenção", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Cadastre a moto com o km do painel. A cada troca de peça você registra o km, e o " +
                                "MotorLog calcula sozinho quando a próxima vence — basta manter o km atualizado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LinhaDeTiles {
                        StatTile("Motos", motos.size.toString(), Modifier.weight(1f))
                        StatTile("Km na frota", formatarNumero(totalKm), Modifier.weight(1.7f), unidade = "km")
                        StatTile(
                            "Vencidas", totalVencidas.toString(), Modifier.weight(1.1f),
                            cor = if (totalVencidas > 0) StatusTroca.VENCIDA.cor() else StatusTroca.OK.cor(),
                        )
                    }
                }
            }
            items(motos, key = { it.id }) { moto ->
                MotoCard(moto, alertas[moto.id], ritmos[moto.id]) { onEditarMoto(moto) }
            }
            if (viewModel.carregou) item { AdicionarMotoCard(onAdicionar) }
            item { Spacer(Modifier.height(4.dp)) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BotaoSecundario("Peças e intervalos", onEditarPeca, Modifier.weight(1f), icone = R.drawable.ic_ml_cog)
            BotaoSecundario(
                "Exportar", onClick = {
                    viewModel.exportar { csv ->
                        val enviar = Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_SUBJECT, "MotorLog — exportação")
                            .putExtra(Intent.EXTRA_TEXT, csv)
                        contexto.startActivity(Intent.createChooser(enviar, "Exportar dados"))
                    }
                },
                modifier = Modifier.weight(0.8f), icone = R.drawable.ic_ml_share, enabled = motos.isNotEmpty(),
            )
        }
    }
}

// Card de moto do protótipo (BikeCard): badge, nome em Chakra, ano · placa, odômetro pequeno,
// pills de alerta e ritmo.
@Composable
fun MotoCard(moto: Moto, alertas: ResumoAlertas?, ritmoKmMes: Int?, onClick: () -> Unit) {
    val accent = accentDaMoto(moto.id)
    val diasSemKm = if (moto.kmAtualizadoEm > 0) diasEntre(moto.kmAtualizadoEm, hojeUtcMillis()) else null
    MlCard(onClick = onClick, pad = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BikeBadge(accent, tamanho = 68.dp)
            Column(Modifier.weight(1f)) {
                Text(moto.modelo, style = MaterialTheme.typography.titleLarge)
                Text("${moto.anoFabricacao} · ${moto.placa}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(9.dp))
                Odometer(moto.kilometragem, tamanho = TamanhoOdometro.PEQUENO, accent = accent)
            }
            Icon(painterResource(R.drawable.ic_ml_chev_r), contentDescription = null, tint = MlTextFaint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(13.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            when {
                alertas == null -> {}
                alertas.vencidas > 0 -> Pill("${alertas.vencidas} vencida${if (alertas.vencidas > 1) "s" else ""}", StatusTroca.VENCIDA.cor(), icone = R.drawable.ic_ml_bell)
                alertas.perto > 0 -> Pill("${alertas.perto} perto de vencer", StatusTroca.PERTO.cor(), icone = R.drawable.ic_ml_bell)
                else -> Pill("Em dia", StatusTroca.OK.cor(), icone = R.drawable.ic_ml_check)
            }
            if (alertas != null && alertas.vencidas > 0 && alertas.perto > 0) {
                Pill("${alertas.perto} perto", StatusTroca.PERTO.cor())
            }
            if (ritmoKmMes != null) PillNeutra("${formatarNumero(ritmoKmMes)} km/mês", icone = R.drawable.ic_ml_road)
            if (diasSemKm != null && diasSemKm >= DIAS_PARA_LEMBRAR_KM) {
                Pill("km há $diasSemKm dias", StatusTroca.PERTO.cor(), icone = R.drawable.ic_ml_clock)
            }
        }
    }
}

// Botão "Adicionar moto" do protótipo: borda tracejada, caixinha com "+" em accent
@Composable
private fun AdicionarMotoCard(onAdicionar: () -> Unit) {
    val borda = MlBorderHi
    MlCard(
        onClick = onAdicionar, pad = 16.dp,
        cor = androidx.compose.ui.graphics.Color.Transparent, borda = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.fillMaxWidth().drawBehind {
            drawRoundRect(
                color = borda,
                cornerRadius = CornerRadius(18.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
            )
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconBox(R.drawable.ic_ml_plus, cor = MaterialTheme.colorScheme.primary, tamanho = 48.dp)
            Column {
                Text("Adicionar moto", style = MaterialTheme.typography.titleMedium)
                Text("Cadastre uma nova motocicleta", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
