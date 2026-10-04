package com.development.motorlog.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
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
import com.development.motorlog.domain.kmJuntos
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.domain.separarGaragem
import com.development.motorlog.ui.components.AvatarDaMoto
import com.development.motorlog.ui.components.BotaoSecundario
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.LinhaDeTiles
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.Odometer
import com.development.motorlog.ui.components.Pill
import com.development.motorlog.ui.components.PillNeutra
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.components.SobreDialog
import com.development.motorlog.ui.components.StatTile
import com.development.motorlog.ui.components.TamanhoOdometro
import com.development.motorlog.ui.theme.MlBorderHi
import com.development.motorlog.ui.theme.MlTextFaint
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.theme.cor
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarMesAno
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.util.juntarComPonto
import com.development.motorlog.ui.viewModels.MotoViewModel
import com.development.motorlog.ui.viewModels.RegistroViewModel

@Composable
fun GaragemScreen(
    modifier: Modifier = Modifier,
    viewModel: MotoViewModel = viewModel(),
    registroViewModel: RegistroViewModel = viewModel(),
    onAdicionar: () -> Unit,
    onEditarMoto: (Moto) -> Unit,
    onMensagem: (String) -> Unit,
) {
    // as motos do dia a dia; as vendidas viram lembranças, no fim da lista (domain/Despedida.kt)
    val garagem = separarGaragem(viewModel.motos)
    val motos = garagem.ativas
    val alertas = viewModel.alertas
    val ritmos = viewModel.ritmos
    val totalKm = motos.sumOf { it.kilometragem }
    val totalVencidas = alertas.values.sumOf { it.vencidas }
    val contexto = LocalContext.current

    // trocas/serviços registrados em outras telas mudam os alertas → recarrega ao entrar
    LaunchedEffect(Unit) { viewModel.carregarMotos() }

    // Restaurar backup: escolhe o arquivo (CSV exportado pelo próprio app), lê e pede confirmação
    val escolherArquivo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val texto = runCatching {
            contexto.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
        if (texto == null) onMensagem("Não consegui ler esse arquivo.") else viewModel.prepararImportacao(texto)
    }
    var mostrarSobre by rememberSaveable { mutableStateOf(false) }
    if (mostrarSobre) SobreDialog(onFechar = { mostrarSobre = false }, onMensagem = onMensagem)
    val pendente = viewModel.importacaoPendente
    if (pendente != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarImportacao() },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text(if (pendente.vazio) "Arquivo sem dados do MotorLog" else "Restaurar backup?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    if (pendente.vazio) "Escolha o arquivo gerado em \"Exportar\" (texto com as seções MOTOS, TROCAS, SERVIÇOS)."
                    else "Encontrei ${pendente.motos.size} moto(s), ${pendente.trocas.size} troca(s), ${pendente.servicos.size} visita(s) à oficina " +
                        "e ${pendente.pecas.size} peça(s)" +
                        (if (pendente.abastecimentos.isNotEmpty()) ", ${pendente.abastecimentos.size} abastecimento(s)" else "") +
                        (if (pendente.cuidados.isNotEmpty()) ", ${pendente.cuidados.size} cuidado(s)" else "") +
                        ". O que já existir no app não será duplicado." +
                        if (pendente.avisos.isNotEmpty()) "\n\n${pendente.avisos.size} linha(s) não entendida(s) serão ignoradas." else "",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                if (!pendente.vazio) TextButton(onClick = {
                    viewModel.confirmarImportacao { r ->
                        registroViewModel.recarregarCatalogo()
                        onMensagem("Restaurado: ${r.motos} moto(s), ${r.trocas} troca(s), ${r.servicos} visita(s), ${r.pecas} peça(s)" +
                            (if (r.abastecimentos > 0) ", ${r.abastecimentos} abastecimento(s)" else "") +
                            (if (r.cuidados > 0) ", ${r.cuidados} cuidado(s)" else "") +
                            if (r.ignorados > 0) " · ${r.ignorados} já existiam" else "")
                    }
                }) { Text("Restaurar") }
            },
            dismissButton = { TextButton(onClick = { viewModel.cancelarImportacao() }) { Text(if (pendente.vazio) "Fechar" else "Cancelar") } },
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                if (!viewModel.carregou) {
                    // ainda lendo o banco: nada a mostrar (evita piscar o estado vazio)
                } else if (viewModel.motos.isEmpty()) {
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
                } else if (motos.size > 1) {
                    // resumo da garagem só com 2+ motos: com uma só, "Motos: 1" repete o card abaixo
                    LinhaDeTiles {
                        StatTile("Motos", motos.size.toString(), Modifier.weight(1f))
                        StatTile("Km somado", formatarNumero(totalKm), Modifier.weight(1.7f), unidade = "km")
                        StatTile(
                            "Vencidas", totalVencidas.toString(), Modifier.weight(1.1f),
                            cor = if (totalVencidas > 0) StatusTroca.VENCIDA.cor() else StatusTroca.OK.cor(),
                        )
                    }
                }
            }
            items(motos, key = { it.id }) { moto ->
                MotoCard(moto, alertas[moto.id], ritmos[moto.id], viewModel.capas[moto.id]) { onEditarMoto(moto) }
            }
            if (viewModel.carregou) item { AdicionarMotoCard(onAdicionar) }
            if (garagem.lembrancas.isNotEmpty()) {
                item { SectionLabel("Lembranças", Modifier.padding(top = 8.dp)) }
                items(garagem.lembrancas, key = { it.id }) { moto ->
                    LembrancaCard(moto, viewModel.capas[moto.id]) { onEditarMoto(moto) }
                }
            }
            item { Spacer(Modifier.height(4.dp)) }
        }

        // rodapé: backup pra fora (Exportar) e de volta (Restaurar). "Peças e intervalos" foi pro cabeçalho (engrenagem)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BotaoSecundario(
                "Exportar backup", onClick = {
                    viewModel.exportar { csv ->
                        val enviar = Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_SUBJECT, "MotorLog — exportação")
                            .putExtra(Intent.EXTRA_TEXT, csv)
                        contexto.startActivity(Intent.createChooser(enviar, "Exportar dados"))
                    }
                },
                modifier = Modifier.weight(1f), icone = R.drawable.ic_ml_share, enabled = viewModel.motos.isNotEmpty(),
            )
            BotaoSecundario(
                "Restaurar backup",
                onClick = { escolherArquivo.launch(arrayOf("text/*", "application/octet-stream")) },
                modifier = Modifier.weight(1f), icone = R.drawable.ic_ml_doc,
            )
        }
        // discreto, no fim da tela: quem é o app e como apoiar (opcional)
        TextButton(onClick = { mostrarSobre = true }, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Text("Sobre o MotorLog · apoiar o projeto", style = MaterialTheme.typography.labelMedium, color = MlTextFaint)
        }
    }
}

// Card de moto do protótipo (BikeCard): foto de capa (ou o badge), nome em Chakra, ano · placa,
// odômetro pequeno, pills de alerta e ritmo.
@Composable
fun MotoCard(moto: Moto, alertas: ResumoAlertas?, ritmoKmMes: Int?, capa: String?, onClick: () -> Unit) {
    val accent = accentDaMoto(moto)
    val diasSemKm = if (moto.kmAtualizadoEm > 0) diasEntre(moto.kmAtualizadoEm, hojeUtcMillis()) else null
    MlCard(onClick = onClick, pad = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AvatarDaMoto(capa, accent, tamanho = 68.dp)
            Column(Modifier.weight(1f)) {
                Text(nomeDaMoto(moto), style = MaterialTheme.typography.titleLarge)
                Text(juntarComPonto(if (moto.apelido.isNotBlank()) moto.modelo else "", moto.anoFabricacao.toString(), moto.placa), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(9.dp))
                Odometer(moto.kilometragem, tamanho = TamanhoOdometro.PEQUENO, accent = accent)
            }
            Icon(painterResource(R.drawable.ic_ml_chev_r), contentDescription = null, tint = MlTextFaint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(13.dp))
        // FlowRow: com 3-4 etiquetas (ou fonte grande) a última quebra a linha em vez de ser espremida
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

// Moto que passou adiante: card discreto, com o tempo e os km que viveram juntos. Abre o Painel dela
// (a história continua lá, e dá pra desfazer a despedida).
@Composable
private fun LembrancaCard(moto: Moto, capa: String?, onClick: () -> Unit) {
    val periodo = if (moto.chegouEm > 0) "De ${formatarMesAno(moto.chegouEm)} a ${formatarMesAno(moto.vendidaEm)}"
    else "Passou adiante em ${formatarMesAno(moto.vendidaEm)}"
    val km = kmJuntos(moto.kmChegada, moto.kilometragem)?.takeIf { it > 0 }
    MlCard(onClick = onClick, pad = 12.dp, cor = Color.Transparent) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AvatarDaMoto(capa, accentDaMoto(moto), tamanho = 48.dp)
            Column(Modifier.weight(1f)) {
                Text(nomeDaMoto(moto), style = MaterialTheme.typography.titleMedium)
                Text(
                    periodo + (km?.let { " · ${formatarKm(it)} juntos" } ?: ""),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(painterResource(R.drawable.ic_ml_chev_r), contentDescription = null, tint = MlTextFaint, modifier = Modifier.size(18.dp))
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
