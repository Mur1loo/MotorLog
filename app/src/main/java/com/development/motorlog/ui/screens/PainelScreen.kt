package com.development.motorlog.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.HistoricoKm
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Servico
import com.development.motorlog.domain.Confianca
import com.development.motorlog.domain.DIAS_PARA_LEMBRAR_KM
import com.development.motorlog.domain.DadosDaFicha
import com.development.motorlog.domain.MediasDeKm
import com.development.motorlog.domain.Recomendacao
import com.development.motorlog.domain.StatusTroca
import com.development.motorlog.domain.TipoMarco
import com.development.motorlog.domain.calcularIndiceDeCuidado
import com.development.motorlog.domain.custoPorKm
import com.development.motorlog.domain.descreverDias
import com.development.motorlog.domain.descreverTempoJuntos
import com.development.motorlog.domain.diasEntre
import com.development.motorlog.domain.ehRevisao
import com.development.motorlog.domain.escolherCapa
import com.development.motorlog.domain.estimarDiasAteTroca
import com.development.motorlog.domain.gastoNoMes
import com.development.motorlog.domain.gastoTotal
import com.development.motorlog.domain.kmJuntos
import com.development.motorlog.domain.marcoPraComemorar
import com.development.motorlog.domain.marcosDaMoto
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.domain.situacaoDosCuidados
import com.development.motorlog.relatorio.compartilharCartao
import com.development.motorlog.relatorio.gerarCartaoDoMarco
import com.development.motorlog.relatorio.gerarFichaDaMoto
import com.development.motorlog.ui.components.AcaoDeSecao
import com.development.motorlog.ui.components.BarraDeProgresso
import com.development.motorlog.ui.components.BotaoHistoricoPdf
import com.development.motorlog.ui.components.BotaoPrimario
import com.development.motorlog.ui.components.BotaoSecundario
import com.development.motorlog.ui.components.CardDeCuidados
import com.development.motorlog.ui.components.CardIndiceDeCuidado
import com.development.motorlog.ui.components.CardNaoLembra
import com.development.motorlog.ui.components.ConfirmarEstimativaDialog
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
import com.development.motorlog.ui.util.formatarKmPorLitro
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.util.formatarReais
import com.development.motorlog.ui.util.formatarReaisCentavos
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.util.iconeDaPeca
import com.development.motorlog.ui.viewModels.AbastecimentoViewModel
import com.development.motorlog.ui.viewModels.CuidadoViewModel
import com.development.motorlog.ui.viewModels.FotoViewModel
import com.development.motorlog.ui.viewModels.RegistroViewModel
import kotlinx.coroutines.launch

// Painel da moto — variante "Foco no km" do protótipo (DashFoco): herói com odômetro e brilho,
// ação protagonista, pills de contexto, tiles, próximas trocas e últimas visitas à oficina.
@Composable
fun PainelScreen(
    modifier: Modifier = Modifier,
    moto: Moto,
    ritmoKmMes: Int?,
    // média do último mês e geral, mostradas lado a lado (a previsão usa ritmoKmMes)
    medias: MediasDeKm?,
    kmRodados: Int,
    historicoKm: List<HistoricoKm>,
    lembretesLigados: Boolean,
    onLigarLembretes: () -> Unit,
    registroViewModel: RegistroViewModel = viewModel(),
    fotoViewModel: FotoViewModel = viewModel(),
    abastecimentoViewModel: AbastecimentoViewModel = viewModel(),
    cuidadoViewModel: CuidadoViewModel = viewModel(),
    onAtualizarKm: () -> Unit,
    onRegistrarTroca: () -> Unit,
    onRegistrarServico: () -> Unit,
    onVerHistorico: () -> Unit,
    onVerTrocas: () -> Unit,
    onEditarMoto: () -> Unit,
    onEditarPeca: (Peca) -> Unit,
    onAbrirServico: (Servico) -> Unit,
    onAbrirFotos: () -> Unit,
    onAbasteci: () -> Unit,
    onAbrirCombustivel: () -> Unit,
    onAbrirDiario: () -> Unit,
    onAbrirHistoria: () -> Unit,
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
    LaunchedEffect(moto.id) { abastecimentoViewModel.carregar(moto.id) }
    LaunchedEffect(moto.id) { cuidadoViewModel.carregar(moto.id) }
    val consumo = abastecimentoViewModel.resumo
    val indice = remember(recomendacoes, pecas, registroViewModel.registrosDaMoto, servicos, cuidadoViewModel.cuidados, moto) {
        calcularIndiceDeCuidado(
            recomendacoes = recomendacoes,
            pecas = pecas,
            registros = registroViewModel.registrosDaMoto,
            servicos = servicos,
            cuidados = cuidadoViewModel.cuidados,
            kmAtual = moto.kilometragem,
            kmAtualizadoEm = moto.kmAtualizadoEm,
            hoje = hojeUtcMillis(),
        )
    }
    var indiceAberto by rememberSaveable { mutableStateOf(false) }
    var gerandoFicha by remember { mutableStateOf(false) }

    // marcos ("Passou dos 50.000 km!", "1 ano com a Pretinha"): o mais recente ainda não visto
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    val prefs = remember { contexto.getSharedPreferences("motorlog", Context.MODE_PRIVATE) }
    val chaveVistos = "marcos_vistos_${moto.id}"
    var marcosVistos by remember(moto.id) { mutableStateOf(prefs.getStringSet(chaveVistos, emptySet()).orEmpty().toSet()) }
    val marcos = remember(moto, historicoKm) { marcosDaMoto(nomeDaMoto(moto), moto.chegouEm, historicoKm, hojeUtcMillis()) }
    val marcoAgora = marcoPraComemorar(marcos, hojeUtcMillis(), marcosVistos)
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
            Box(Modifier.fillMaxWidth().height(136.dp).clip(MlFormas.card).clickable(onClickLabel = "abrir o álbum") { onAbrirFotos() }) {
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
                // a moto como alguém: o tempo e os km de vida juntos (nome e chegada vêm do cadastro)
                val tempoJuntos = descreverTempoJuntos(moto.chegouEm, hojeUtcMillis())
                if (tempoJuntos != null) {
                    val km = kmJuntos(moto.kmChegada, moto.kilometragem)?.takeIf { it > 0 }
                    Text(
                        (if (tempoJuntos == "hoje") "Chegou hoje" else "Juntos há $tempoJuntos") + (km?.let { " · ${formatarKm(it)}" } ?: ""),
                        style = MaterialTheme.typography.labelLarge, color = accent,
                    )
                } else {
                    TextButton(onClick = onEditarMoto) {
                        Text("Quando ela chegou? Conte aqui", style = MaterialTheme.typography.labelMedium, color = accent)
                    }
                }
                Spacer(Modifier.height(10.dp))
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
                BotaoPrimario("Atualizar km", onAtualizarKm, icone = R.drawable.ic_ml_gauge, altura = 58.dp)
                Spacer(Modifier.height(8.dp))
                // abastecer também atualiza o km: o outro jeito de manter o painel em dia
                BotaoSecundario("Abasteci", onAbasteci, Modifier.fillMaxWidth(), icone = R.drawable.ic_ml_fuel)
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // as duas médias com nome: o ritmo de agora e o de sempre
                    medias?.ultimoMes?.let { PillNeutra("${formatarNumero(it)} km/mês no último mês", icone = R.drawable.ic_ml_road) }
                    medias?.geral?.let { PillNeutra("${formatarNumero(it)} km/mês ${if (medias.geralDesdeAChegada) "desde que chegou" else "desde o cadastro"}", icone = R.drawable.ic_ml_road) }
                    when {
                        vencidas > 0 -> Pill("$vencidas vencida${if (vencidas > 1) "s" else ""}", StatusTroca.VENCIDA.cor(), icone = R.drawable.ic_ml_bell)
                        perto > 0 -> Pill("$perto perto de vencer", StatusTroca.PERTO.cor(), icone = R.drawable.ic_ml_wrench)
                        proximasTrocas.isNotEmpty() -> Pill("Tudo em dia", StatusTroca.OK.cor(), icone = R.drawable.ic_ml_check)
                    }
                }
            }
        }

        // ── marco pra comemorar: celebra a história da moto (e dá orgulho de compartilhar) ──
        if (marcoAgora != null) {
            MlCard(cor = accent.copy(alpha = 0.12f), borda = accent) {
                Text(if (marcoAgora.tipo == TipoMarco.KM) "MARCO" else "ANIVERSÁRIO", style = MaterialTheme.typography.labelSmall, color = accent)
                Spacer(Modifier.height(4.dp))
                Text(marcoAgora.titulo, style = MaterialTheme.typography.titleLarge)
                Text(formatarData(marcoAgora.data), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    BotaoSecundario(
                        "Compartilhar", {
                            escopo.launch {
                                val arquivo = gerarCartaoDoMarco(contexto, moto, marcoAgora, capa?.arquivo)
                                compartilharCartao(contexto, arquivo, "${marcoAgora.titulo} 🏍️")
                            }
                        },
                        Modifier.weight(1f), icone = R.drawable.ic_ml_share,
                    )
                    TextButton(onClick = {
                        marcosVistos = marcosVistos + marcoAgora.chave
                        prefs.edit { putStringSet(chaveVistos, marcosVistos) }
                    }) { Text("Valeu!") }
                }
            }
        }

        // ── lembretes desligados: sem eles o app não avisa nada, e a pessoa nem sabe ──
        if (!lembretesLigados) {
            MlCard(borda = StatusTroca.PERTO.cor().copy(alpha = 0.5f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBox(R.drawable.ic_ml_bell, cor = StatusTroca.PERTO.cor())
                    Column(Modifier.weight(1f)) {
                        Text("Lembretes desligados", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Assim eu não consigo te avisar quando uma troca vencer.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                BotaoSecundario("Ligar lembretes", onLigarLembretes, Modifier.fillMaxWidth(), icone = R.drawable.ic_ml_bell)
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
                    when {
                        porKm != null -> "${formatarReaisCentavos(porKm)} por km"
                        total <= 0 -> "custo por km: sem gasto ainda"
                        else -> "custo por km: atualize o km"
                    },
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

        // ── Card: índice de cuidado (0–100: trocas, manutenção, revisão, cuidados e km em dia) ──
        if (indice != null) {
            CardIndiceDeCuidado(indice, indiceAberto) { indiceAberto = !indiceAberto }
        }

        // ── Card: cuidados (os rituais de quem trata bem a moto, em 1 toque) ──
        CardDeCuidados(
            titulo = "Cuidados com a ${nomeDaMoto(moto)}",
            situacoes = situacaoDosCuidados(cuidadoViewModel.cuidados, moto.kilometragem, hojeUtcMillis()),
            accent = accent,
            onRegistrar = { s ->
                cuidadoViewModel.registrar(moto, s.tipo, hojeUtcMillis()) { registrado ->
                    val oQue = s.tipo.nome.lowercase()
                    onMensagem(if (registrado) "Anotado no diário: $oQue." else "Já estava anotado hoje: $oQue.")
                }
            },
            onAbrirDiario = onAbrirDiario,
        )

        // ── Card: consumo (abre a tela Combustível) ──
        MlCard(onClick = onAbrirCombustivel, pad = 14.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBox(R.drawable.ic_ml_fuel, cor = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text("Consumo", style = MaterialTheme.typography.titleSmall)
                    Text(
                        consumo?.let { c ->
                            c.media?.let { media ->
                                "${formatarKmPorLitro(media.kmPorLitro)} em média" +
                                    (if (media.confianca == Confianca.APROXIMADA) " (aprox.)" else "") +
                                    (c.custoPorKm?.let { " · ${formatarReaisCentavos(it)} por km" } ?: "")
                            }
                        } ?: "Registre os abastecimentos (parciais valem) pra ver o km/l",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(painterResource(R.drawable.ic_ml_chev_r), contentDescription = null, tint = MlTextFaint, modifier = Modifier.size(18.dp))
            }
        }

        // o PDF é o que a pessoa mostra pro comprador e pro mecânico: logo depois das trocas,
        // não no fim da tela (antes vinha depois de cinco blocos)
        // a biografia da moto: fotos, trocas, cuidados e marcos numa linha só
        BotaoSecundario("A história da ${nomeDaMoto(moto)}", onAbrirHistoria, Modifier.fillMaxWidth(), icone = R.drawable.ic_ml_clock)
        // a ficha: imagem com foto, km e os quadros de orgulho (índice, km juntos, manutenção, consumo)
        BotaoSecundario(
            if (gerandoFicha) "Montando a ficha…" else "Compartilhar a ficha da moto", {
                gerandoFicha = true
                escopo.launch {
                    val dados = DadosDaFicha(
                        indice = indice,
                        kmJuntos = kmJuntos(moto.kmChegada, moto.kilometragem),
                        kmPorLitro = consumo?.media?.kmPorLitro,
                        trocas = registroViewModel.registrosDaMoto.size,
                        visitas = servicos.size,
                        cuidados = cuidadoViewModel.cuidados.size,
                    )
                    val arquivo = runCatching { gerarFichaDaMoto(contexto, moto, dados, capa?.arquivo) }.getOrNull()
                    gerandoFicha = false
                    if (arquivo != null) compartilharCartao(contexto, arquivo, "${nomeDaMoto(moto)} 🏍️")
                    else onMensagem("Não consegui montar a ficha. Tente de novo.")
                }
            },
            Modifier.fillMaxWidth(), icone = R.drawable.ic_ml_share, enabled = !gerandoFicha,
        )
        BotaoHistoricoPdf(moto, onMensagem, Modifier.fillMaxWidth())

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
                        modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "ver a visita") { onAbrirServico(servico) }.padding(vertical = 11.dp),
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // excluir fica dentro de "Editar dados": ação rara e sem volta, longe do dia a dia
            TextButton(onClick = onEditarMoto, modifier = Modifier.weight(1f)) { Text("Editar dados ou excluir a moto") }
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

}

// Linha de alerta do protótipo (AlertRow): caixinha com ícone da peça na cor do status,
// nome, "faltam X km · ~N dias" e pill "Trocar"/"Em breve"/"Em dia".
@Composable
fun LinhaDeAlerta(rec: Recomendacao, ritmoKmMes: Int?, onClick: () -> Unit) {
    val cor = rec.statusTroca.cor()
    val restante = rec.kmRestante ?: 0
    val dias = estimarDiasAteTroca(rec.kmRestante, ritmoKmMes)
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "registrar troca") { onClick() }.padding(vertical = 10.dp),
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
