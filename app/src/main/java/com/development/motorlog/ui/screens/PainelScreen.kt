package com.development.motorlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Servico
import com.development.motorlog.domain.DIAS_PARA_LEMBRAR_KM
import com.development.motorlog.domain.Recomendacao
import com.development.motorlog.domain.StatusTroca
import com.development.motorlog.domain.custoPorKm
import com.development.motorlog.domain.gastoNoMes
import com.development.motorlog.domain.gastoTotal
import com.development.motorlog.domain.descreverDias
import com.development.motorlog.domain.diasEntre
import com.development.motorlog.domain.ehRevisao
import com.development.motorlog.domain.escolherCapa
import com.development.motorlog.domain.estimarDiasAteTroca
import com.development.motorlog.ui.components.AcaoDeSecao
import com.development.motorlog.ui.components.BarraDeProgresso
import com.development.motorlog.ui.components.BotaoPrimario
import com.development.motorlog.ui.components.BotaoHistoricoPdf
import com.development.motorlog.ui.components.BotaoSecundario
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.components.FotoArquivo
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.LinhaDeTiles
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.Odometer
import com.development.motorlog.ui.components.Pill
import com.development.motorlog.ui.components.PillNeutra
import com.development.motorlog.ui.components.RegistrarTrocaDialog
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.components.StatTile
import com.development.motorlog.ui.theme.MlFormas
import com.development.motorlog.ui.theme.MlTextFaint
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.theme.chakra
import com.development.motorlog.ui.theme.cor
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.util.formatarReais
import com.development.motorlog.ui.util.formatarReaisCentavos
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.util.iconeDaPeca
import com.development.motorlog.ui.viewModels.FotoViewModel
import com.development.motorlog.ui.viewModels.RegistroViewModel
import com.development.motorlog.ui.components.ConfirmarEstimativaDialog
import com.development.motorlog.ui.components.CardNaoLembra

// Painel da moto — variante "Foco no km" do protótipo (DashFoco): herói com odômetro e brilho,
// ação protagonista, pills de contexto, tiles, próximas trocas e últimas visitas à oficina.
@Composable
fun PainelScreen(
    modifier: Modifier = Modifier,
    moto: Moto,
    ritmoKmMes: Int?,
    kmRodados: Int,
    registroViewModel: RegistroViewModel = viewModel(),
    fotoViewModel: FotoViewModel = viewModel(),
    onAtualizarKm: () -> Unit,
    onRegistrarTroca: () -> Unit,
    onRegistrarServico: () -> Unit,
    onVerHistorico: () -> Unit,
    onVerTrocas: () -> Unit,
    onEditarMoto: () -> Unit,
    onExcluirMoto: () -> Unit,
    onEditarPeca: (Peca) -> Unit,
    onAbrirServico: (Servico) -> Unit,
    onAbrirFotos: () -> Unit,
    onMensagem: (String) -> Unit,
) {
    val recomendacoes = registroViewModel.recomendacoes
    val servicos = registroViewModel.servicos
    val pecas = registroViewModel.pecas
    val accent = accentDaMoto(moto)
    // no painel só interessam as peças JÁ com registro (sem as "nunca trocadas")
    val proximasTrocas = recomendacoes.filter { it.statusTroca != StatusTroca.NUNCA_TROCADA }
    val vencidas = proximasTrocas.count { it.statusTroca == StatusTroca.VENCIDA }
    val perto = proximasTrocas.count { it.statusTroca == StatusTroca.PERTO }
    val proxima = proximasTrocas.firstOrNull()
    var confirmarExclusao by rememberSaveable { mutableStateOf(false) }
    var trocandoPecaId by rememberSaveable { mutableStateOf<Long?>(null) }
    var confirmarEstimativa by rememberSaveable { mutableStateOf(false) }
    // peças que nunca tiveram troca registrada (sem a revisão, que se registra em "Fui à oficina")
    val semRegistro = recomendacoes.filter { it.statusTroca == StatusTroca.NUNCA_TROCADA && !it.ehRevisao }
    val trocandoPeca = trocandoPecaId?.let { id -> pecas.find { it.id == id } }

    LaunchedEffect(moto) {
        registroViewModel.carregarRecomendacoes(moto)
        registroViewModel.carregarServicos(moto)
        registroViewModel.carregarTrocasAvulsas(moto)
    }
    LaunchedEffect(moto.id) { fotoViewModel.carregar(moto.id) }
    val fotos = fotoViewModel.fotos
    val capa = escolherCapa(fotos, moto.fotoCapaId)
    val hoje = hojeUtcMillis()
    val total = gastoTotal(servicos, registroViewModel.trocasAvulsas)
    val porKm = custoPorKm(total, kmRodados)
    val noMes = gastoNoMes(servicos, registroViewModel.trocasAvulsas, hoje)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ── capa: a foto da moto numa faixa larga (abre o álbum) ──
        if (capa != null) {
            Box(Modifier.fillMaxWidth().height(136.dp).clip(MlFormas.card).clickable { onAbrirFotos() }) {
                FotoArquivo(capa.arquivo, Modifier.fillMaxSize(), ladoMaxPx = 1080, descricao = "Foto da moto")
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.4f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.7f))))
                Pill(
                    "${fotos.size} foto${if (fotos.size == 1) "" else "s"}", Color.White,
                    Modifier.align(Alignment.BottomEnd).padding(10.dp), fundo = Color.Black.copy(alpha = 0.45f),
                )
                if (capa.legenda.isNotBlank()) {
                    Text(
                        capa.legenda, style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 12.dp, end = 96.dp),
                    )
                }
            }
        }

        // ── HERO: brilho radial da cor da moto + odômetro + ação protagonista ──
        Box(
            Modifier.fillMaxWidth().drawBehind {
                drawRect(
                    Brush.radialGradient(
                        0f to accent.copy(alpha = 0.22f), 0.45f to accent.copy(alpha = 0.05f), 1f to Color.Transparent,
                        center = Offset(size.width / 2f, 0f),
                        radius = size.height * 1.05f,
                    ),
                )
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("QUILOMETRAGEM ATUAL", style = MaterialTheme.typography.labelSmall, color = MlTextFaint, letterSpacing = 1.6.sp)
                Spacer(Modifier.height(16.dp))
                Odometer(moto.kilometragem, accent = accent)
                Spacer(Modifier.height(12.dp))
                val diasSemKm = if (moto.kmAtualizadoEm > 0) diasEntre(moto.kmAtualizadoEm, hojeUtcMillis()) else null
                Text(
                    when (diasSemKm) {
                        null -> "Sem registro de quando o km foi atualizado"
                        0 -> "Atualizado hoje"
                        1 -> "Atualizado ontem"
                        else -> "Atualizado há $diasSemKm dias"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (diasSemKm != null && diasSemKm >= DIAS_PARA_LEMBRAR_KM) StatusTroca.PERTO.cor()
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                BotaoPrimario("Atualizar agora", onAtualizarKm, icone = R.drawable.ic_ml_gauge, altura = 58.dp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ritmoKmMes != null) PillNeutra("${formatarNumero(ritmoKmMes)} km/mês", icone = R.drawable.ic_ml_road)
                    when {
                        vencidas > 0 -> Pill("$vencidas vencida${if (vencidas > 1) "s" else ""}", StatusTroca.VENCIDA.cor(), icone = R.drawable.ic_ml_bell)
                        perto > 0 -> Pill("$perto perto de vencer", StatusTroca.PERTO.cor(), icone = R.drawable.ic_ml_wrench)
                        proximasTrocas.isNotEmpty() -> Pill("Tudo em dia", StatusTroca.OK.cor(), icone = R.drawable.ic_ml_check)
                    }
                }
            }
        }

        // ── tiles: próxima troca (com barra) + gasto + ritmo ──
        LinhaDeTiles {
            StatTile(
                "Próxima troca",
                valor = proxima?.kmRestante?.let { if (it < 0) "vencida" else formatarNumero(it) },
                unidade = if (proxima?.kmRestante != null && proxima.kmRestante >= 0) "km" else null,
                icone = R.drawable.ic_ml_wrench,
                cor = proxima?.statusTroca?.cor() ?: MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1.2f),
                onClick = onVerTrocas,
            ) {
                if (proxima?.kmUltimaTroca != null && proxima.kmProximaTroca != null && proxima.kmProximaTroca > proxima.kmUltimaTroca) {
                    Spacer(Modifier.height(8.dp))
                    val pct = ((moto.kilometragem - proxima.kmUltimaTroca).toFloat() / (proxima.kmProximaTroca - proxima.kmUltimaTroca)).coerceIn(0f, 1f)
                    BarraDeProgresso(pct, proxima.statusTroca.cor(), altura = 6.dp)
                    Spacer(Modifier.height(5.dp))
                    Text(proxima.pecaNome, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else if (proxima == null) {
                    Spacer(Modifier.height(4.dp))
                    Text("nenhuma registrada", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // dois tiles bastam: "Visitas" era redundante com a aba Histórico (o Gasto já leva pra lá)
            StatTile("Gasto · ${servicos.size} visita${if (servicos.size == 1) "" else "s"}", formatarReais(total), Modifier.weight(1f), icone = R.drawable.ic_ml_dollar, onClick = onVerHistorico) {
                Spacer(Modifier.height(6.dp))
                Text(
                    if (porKm != null) "${formatarReaisCentavos(porKm)} por km" else "— por km (rode mais)",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${formatarReais(noMes)} este mês",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // ── ações secundárias: na língua do motoboy ──
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BotaoSecundario("Troquei uma peça", onRegistrarTroca, Modifier.weight(1f), icone = R.drawable.ic_ml_wrench)
            BotaoSecundario("Fui à oficina", onRegistrarServico, Modifier.weight(1f), icone = R.drawable.ic_ml_pin)
        }

        // ── Card: próximas trocas ──
        MlCard {
            SectionLabel("Próximas trocas", direita = { AcaoDeSecao("Ver todas", onVerTrocas) })
            if (proximasTrocas.isEmpty()) {
                Text(
                    "Toque em \"Troquei uma peça\" pra registrar a primeira. A partir daí eu aviso quando cada uma vence.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // primeiro uso: o atalho que tira o painel do vazio, sem ter que achar a aba Trocas
                if (semRegistro.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    CardNaoLembra(semRegistro.size, moto.kilometragem, { confirmarEstimativa = true }, Modifier.fillMaxWidth())
                }
            } else {
                proximasTrocas.take(4).forEachIndexed { i, rec ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    LinhaDeAlerta(rec, ritmoKmMes) { if (rec.ehRevisao) onRegistrarServico() else trocandoPecaId = rec.pecaId }
                }
            }
        }

        // ── Card: álbum da moto (sem fotos, o convite) ──
        MlCard {
            SectionLabel("Álbum da moto", direita = { AcaoDeSecao(if (fotos.isEmpty()) "Abrir" else "Ver todas", onAbrirFotos) })
            if (fotos.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBox(R.drawable.ic_ml_moto, cor = accent, tamanho = 44.dp)
                    Text(
                        "Guarde fotos da sua moto: o dia em que ela chegou, as viagens, o antes e depois. A melhor vira a capa.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(10.dp))
                BotaoSecundario("Adicionar a primeira foto", onAbrirFotos, Modifier.fillMaxWidth(), icone = R.drawable.ic_ml_plus)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val mostradas = fotos.take(4)
                    mostradas.forEachIndexed { i, foto ->
                        Box(Modifier.weight(1f).aspectRatio(1f).clip(MlFormas.campo).clickable { onAbrirFotos() }) {
                            FotoArquivo(foto.arquivo, Modifier.fillMaxSize(), ladoMaxPx = 256)
                            val resto = fotos.size - mostradas.size
                            if (i == mostradas.lastIndex && resto > 0) {
                                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
                                    Text("+$resto", style = chakra(18.sp), color = Color.White)
                                }
                            }
                        }
                    }
                    // menos de 4 fotos: completa a linha com espaço vazio (as miniaturas não esticam)
                    repeat(4 - mostradas.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        // ── Card: últimas visitas à oficina ──
        MlCard {
            SectionLabel("Últimas visitas à oficina", direita = { AcaoDeSecao("Histórico", onVerHistorico) })
            val ultimos = servicos.sortedByDescending { it.data }.take(3)
            if (ultimos.isEmpty()) {
                Text(
                    "Quando for à oficina, registre aqui: custo, peças trocadas e data ficam guardados.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ultimos.forEachIndexed { i, servico ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onAbrirServico(servico) }.padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        IconBox(R.drawable.ic_ml_wrench, cor = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f)) {
                            Text(servico.tipoServico, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${formatarKm(servico.kilometragem)} · ${formatarData(servico.data)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatarReais(servico.custo), style = chakra(14.sp))
                    }
                }
            }
        }

        BotaoHistoricoPdf(moto, onMensagem, Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onEditarMoto, modifier = Modifier.weight(1f)) { Text("Editar dados") }
            TextButton(onClick = { confirmarExclusao = true }, modifier = Modifier.weight(1f)) {
                Text("Excluir moto", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (trocandoPeca != null) {
        RegistrarTrocaDialog(
            peca = trocandoPeca,
            kmAtual = moto.kilometragem,
            onConfirmar = { km, preco, data ->
                registroViewModel.registrarTroca(moto, trocandoPeca, km, preco, data)
                trocandoPecaId = null
            },
            onEditarPeca = {
                trocandoPecaId = null
                onEditarPeca(trocandoPeca)
            },
            onCancelar = { trocandoPecaId = null },
        )
    }

    if (confirmarEstimativa) {
        ConfirmarEstimativaDialog(
            semRegistro.size, moto.kilometragem,
            onConfirmar = {
                confirmarEstimativa = false
                val pecasAlvo = semRegistro.mapNotNull { rec -> pecas.find { it.id == rec.pecaId } }
                registroViewModel.registrarTrocasEmLote(moto, pecasAlvo, moto.kilometragem)
                onMensagem("Pronto: ${pecasAlvo.size} peças marcadas aos ${formatarKm(moto.kilometragem)}. Corrija as que lembrar em Trocas.")
            },
            onCancelar = { confirmarEstimativa = false },
        )
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

// Linha de alerta do protótipo (AlertRow): caixinha com ícone da peça na cor do status,
// nome, "faltam X km · ~N dias" e pill "Trocar"/"Em breve"/"Em dia".
@Composable
fun LinhaDeAlerta(rec: Recomendacao, ritmoKmMes: Int?, onClick: () -> Unit) {
    val cor = rec.statusTroca.cor()
    val restante = rec.kmRestante ?: 0
    val dias = estimarDiasAteTroca(rec.kmRestante, ritmoKmMes)
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconBox(iconeDaPeca(rec.pecaNome), cor = cor)
        Column(Modifier.weight(1f)) {
            Text(rec.pecaNome, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                when {
                    rec.statusTroca == StatusTroca.NUNCA_TROCADA -> if (rec.ehRevisao) "registre a última revisão em \"Fui à oficina\"" else "sem registro"
                    restante == 0 -> "vence agora"
                    restante < 0 -> "${formatarKm(-restante)} em atraso"
                    else -> "Faltam ${formatarKm(restante)}" + if (dias != null) " · ${descreverDias(dias)}" else ""
                },
                style = MaterialTheme.typography.labelMedium, color = cor,
            )
        }
        Pill(
            when (rec.statusTroca) {
                StatusTroca.VENCIDA -> if (rec.ehRevisao) "Revisar" else "Trocar"
                StatusTroca.PERTO -> "Em breve"
                StatusTroca.OK -> "Em dia"
                StatusTroca.NUNCA_TROCADA -> "Registrar"
            },
            cor,
        )
    }
}
