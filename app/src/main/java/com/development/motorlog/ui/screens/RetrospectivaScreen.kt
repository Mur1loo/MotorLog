package com.development.motorlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.anoDe
import com.development.motorlog.domain.anosComHistoria
import com.development.motorlog.domain.escolherCapa
import com.development.motorlog.domain.litrosParaTexto
import com.development.motorlog.domain.montarRetrospectiva
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.relatorio.compartilharCartao
import com.development.motorlog.relatorio.gerarCartaoDaRetrospectiva
import com.development.motorlog.ui.components.BotaoPrimario
import com.development.motorlog.ui.components.FotoArquivo
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.theme.MlFormas
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.theme.chakra
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarKmPorLitro
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.util.formatarReais
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.viewModels.RetrospectivaViewModel
import kotlinx.coroutines.launch

// "Seu 2026 com a Pretinha": o ano da moto em blocos — km, rolês, cuidado, manutenção, combustível,
// marcos, o que ela ganhou e as fotos — e um cartão pra compartilhar (domain/Retrospectiva.kt).
// Dá pra rever os anos anteriores nos chips do topo.
@Composable
fun RetrospectivaScreen(
    moto: Moto,
    anoInicial: Int?,
    onMensagem: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RetrospectivaViewModel = viewModel(),
) {
    LaunchedEffect(moto) { viewModel.carregar(moto) }
    val dados = viewModel.dados?.takeIf { it.moto.id == moto.id }
    val hoje = hojeUtcMillis()
    val anoAtual = anoDe(hoje)
    var ano by rememberSaveable { mutableIntStateOf(anoInicial ?: anoAtual) }
    val anos = remember(dados) { dados?.let { (anosComHistoria(it, hoje) + ano).distinct().sortedDescending() }.orEmpty() }
    val r = remember(dados, ano) { dados?.let { montarRetrospectiva(ano, it) } }
    val accent = accentDaMoto(moto)
    val nome = nomeDaMoto(moto)
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    var gerando by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (anos.size > 1) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                anos.forEach { a -> FilterChip(selected = a == ano, onClick = { ano = a }, label = { Text("$a") }, shape = MlFormas.pill) }
            }
        }
        if (r == null) return@Column
        val foto = r.fotos.firstOrNull()?.arquivo ?: escolherCapa(dados?.fotos.orEmpty(), moto.fotoCapaId)?.arquivo

        // ── abertura: a foto do ano e o título ──
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 10f).clip(MlFormas.card).background(accent.copy(alpha = 0.18f))) {
            if (foto != null) FotoArquivo(foto, Modifier.fillMaxSize(), ladoMaxPx = 1280, descricao = "Foto do ano")
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.35f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.8f))))
            Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text("Seu ${r.ano} com a $nome", style = chakra(26.sp), color = Color.White)
                if (r.ano == anoAtual) Text("até agora", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
            }
        }

        if (r.vazia) {
            Text(
                "Nada registrado em ${r.ano}. A retrospectiva se monta sozinha com o km, os rolês, os cuidados, as trocas e as fotos do ano.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }

        r.kmRodados?.takeIf { it > 0 }?.let { km ->
            Bloco("Rodaram juntos", formatarNumero(km), "km", accent) {
                Text("Uns ${formatarNumero(km / 12)} km por mês.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (r.roles.isNotEmpty()) {
            Bloco("Rolês e viagens", formatarNumero(r.roles.size), if (r.roles.size == 1) "rolê" else "rolês", accent) {
                Text(
                    r.roles.joinToString(" · ") { it.destino } + if (r.kmEmRoles > 0) "\n${formatarKm(r.kmEmRoles)} na estrada" else "",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (r.cuidados > 0) {
            Bloco("Cuidado", formatarNumero(r.cuidados), if (r.cuidados == 1) "cuidado anotado" else "cuidados anotados", accent) {
                if (r.lavagens > 0) Text("${r.lavagens} lavage${if (r.lavagens == 1) "m" else "ns"} — ela agradece.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (r.trocas + r.visitas > 0) {
            Bloco("Manutenção", formatarNumero(r.trocas), if (r.trocas == 1) "troca de peça" else "trocas de peça", accent) {
                Text(
                    "${r.visitas} visita${if (r.visitas == 1) "" else "s"} à oficina" + if (r.gastoManutencao > 0) " · ${formatarReais(r.gastoManutencao)} investidos" else "",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (r.abastecimentos > 0) {
            Bloco("Combustível", litrosParaTexto(r.mililitros), "litros", accent) {
                Text(
                    listOfNotNull(
                        "${r.abastecimentos} abastecimento${if (r.abastecimentos == 1) "" else "s"}",
                        r.gastoCombustivel.takeIf { it > 0 }?.let(::formatarReais),
                        r.kmPorLitro?.let { "${formatarKmPorLitro(it)} de média" },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (r.marcos.isNotEmpty() || r.instalados.isNotEmpty()) {
            MlCard {
                if (r.marcos.isNotEmpty()) {
                    SectionLabel("Marcos")
                    r.marcos.forEach { Text(it.titulo, style = MaterialTheme.typography.titleSmall, color = accent) }
                }
                if (r.instalados.isNotEmpty()) {
                    if (r.marcos.isNotEmpty()) Spacer(Modifier.height(10.dp))
                    SectionLabel("Ela ganhou")
                    Text(r.instalados.joinToString(" · ") { it.nome }, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
        if (r.fotos.isNotEmpty()) {
            MlCard {
                SectionLabel("Fotos do ano · ${r.fotos.size}")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    r.fotos.take(12).forEach { f ->
                        FotoArquivo(f.arquivo, Modifier.size(96.dp).clip(MlFormas.campo), ladoMaxPx = 320, descricao = f.legenda.ifBlank { null })
                    }
                }
            }
        }

        BotaoPrimario(
            if (gerando) "Montando o cartão…" else "Compartilhar meu ${r.ano}", {
                gerando = true
                escopo.launch {
                    val arquivo = runCatching { gerarCartaoDaRetrospectiva(contexto, moto, r, foto) }.getOrNull()
                    gerando = false
                    if (arquivo != null) compartilharCartao(contexto, arquivo, "Meu ${r.ano} com a $nome 🏍️")
                    else onMensagem("Não consegui montar o cartão. Tente de novo.")
                }
            },
            icone = R.drawable.ic_ml_share, enabled = !gerando,
        )
        Spacer(Modifier.height(16.dp))
    }
}

// um bloco da retrospectiva: rótulo, número grande na cor da moto e uma linha de apoio
@Composable
private fun Bloco(rotulo: String, valor: String, unidade: String, accent: Color, apoio: @Composable () -> Unit) {
    MlCard {
        SectionLabel(rotulo)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(valor, style = chakra(40.sp), color = accent)
            Text(unidade, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        }
        apoio()
    }
}
