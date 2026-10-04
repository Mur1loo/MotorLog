package com.development.motorlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.HistoricoKm
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.EventoDaHistoria
import com.development.motorlog.domain.TipoEvento
import com.development.motorlog.domain.descreverTempoJuntos
import com.development.motorlog.domain.escolherCapa
import com.development.motorlog.domain.estaVendida
import com.development.motorlog.domain.fimDaHistoria
import com.development.motorlog.domain.marcosDaMoto
import com.development.motorlog.domain.montarLinhaDoTempo
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.ui.components.AvatarDaMoto
import com.development.motorlog.ui.components.BotaoSecundario
import com.development.motorlog.ui.components.FotoArquivo
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.theme.MlFormas
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarMesAno
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.util.iconeDoEvento
import com.development.motorlog.ui.viewModels.CuidadoViewModel
import com.development.motorlog.ui.viewModels.DesejoViewModel
import com.development.motorlog.ui.viewModels.FotoViewModel
import com.development.motorlog.ui.viewModels.RegistroViewModel
import com.development.motorlog.ui.viewModels.RoleViewModel

// "A história da Pretinha": tudo o que o app guarda da moto numa linha só, por mês, do mais
// recente pro dia em que ela chegou (domain/LinhaDoTempo.kt). É a biografia da moto.
@Composable
fun LinhaDoTempoScreen(
    moto: Moto,
    historicoKm: List<HistoricoKm>,
    modifier: Modifier = Modifier,
    onAbrirRetrospectiva: () -> Unit = {},
    registroViewModel: RegistroViewModel = viewModel(),
    fotoViewModel: FotoViewModel = viewModel(),
    cuidadoViewModel: CuidadoViewModel = viewModel(),
    desejoViewModel: DesejoViewModel = viewModel(),
    roleViewModel: RoleViewModel = viewModel(),
) {
    LaunchedEffect(moto.id) {
        registroViewModel.carregarServicos(moto)
        registroViewModel.carregarRegistrosDaMoto(moto)
        fotoViewModel.carregar(moto.id)
        cuidadoViewModel.carregar(moto.id)
        desejoViewModel.carregar(moto.id)
        roleViewModel.carregar(moto.id)
    }
    val nome = nomeDaMoto(moto)
    val accent = accentDaMoto(moto)
    // moto vendida: a história (e os aniversários) para no dia da despedida
    val hoje = fimDaHistoria(moto, hojeUtcMillis())
    val eventos = remember(moto, historicoKm, registroViewModel.registrosDaMoto, registroViewModel.servicos, registroViewModel.pecas, fotoViewModel.fotos, cuidadoViewModel.cuidados, desejoViewModel.desejos, roleViewModel.roles) {
        montarLinhaDoTempo(
            moto = moto,
            pecas = registroViewModel.pecas,
            registros = registroViewModel.registrosDaMoto,
            servicos = registroViewModel.servicos,
            fotos = fotoViewModel.fotos,
            cuidados = cuidadoViewModel.cuidados,
            marcos = marcosDaMoto(nome, moto.chegouEm, historicoKm, hoje),
            desejos = desejoViewModel.desejos,
            roles = roleViewModel.roles,
        )
    }
    val capa = escolherCapa(fotoViewModel.fotos, moto.fotoCapaId)

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            MlCard(pad = 14.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    AvatarDaMoto(capa?.arquivo, accent, tamanho = 64.dp)
                    Column(Modifier.weight(1f)) {
                        Text("A história da $nome", style = MaterialTheme.typography.titleLarge)
                        val juntos = descreverTempoJuntos(moto.chegouEm, hoje)
                        Text(
                            listOfNotNull(
                                juntos?.let {
                                    when {
                                        estaVendida(moto) -> if (it == "hoje") "passou adiante no dia em que chegou" else "juntos por $it"
                                        it == "hoje" -> "chegou hoje"
                                        else -> "juntos há $it"
                                    }
                                },
                                "${eventos.size} momento${if (eventos.size == 1) "" else "s"}",
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (eventos.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                BotaoSecundario("Retrospectiva do ano", onAbrirRetrospectiva, Modifier.fillMaxWidth(), icone = R.drawable.ic_ml_calendar)
            }
            Spacer(Modifier.height(12.dp))
            if (eventos.isEmpty()) {
                Text(
                    "A história da $nome se escreve sozinha: fotos do álbum, trocas, visitas à oficina, cuidados, rolês, o que você instalou e os marcos " +
                        "aparecem aqui, em ordem. Conte também quando ela chegou, em Editar dados.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        itemsIndexed(eventos) { i, e ->
            // cabeçalho a cada mês novo
            val mes = formatarMesAno(e.data)
            if (i == 0 || formatarMesAno(eventos[i - 1].data) != mes) {
                SectionLabel(mes, Modifier.padding(top = if (i == 0) 0.dp else 12.dp))
            }
            LinhaDoEvento(e, accent, ultimo = i == eventos.lastIndex)
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun LinhaDoEvento(e: EventoDaHistoria, accent: Color, ultimo: Boolean) {
    val destaque = e.tipo in setOf(TipoEvento.CHEGADA, TipoEvento.MARCO_KM, TipoEvento.ANIVERSARIO, TipoEvento.DESPEDIDA)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // trilho da linha do tempo: o ícone e um fio até o próximo momento
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconBox(iconeDoEvento(e), cor = if (destaque) accent else null)
            if (!ultimo) Box(Modifier.size(width = 2.dp, height = 28.dp).background(MaterialTheme.colorScheme.outline))
        }
        Column(Modifier.weight(1f).padding(top = 2.dp, bottom = 12.dp)) {
            Text(e.titulo, style = MaterialTheme.typography.titleSmall, color = if (destaque) accent else MaterialTheme.colorScheme.onSurface)
            Text(
                listOfNotNull(formatarData(e.data), e.km?.let(::formatarKm), e.detalhe).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (e.foto != null) {
            FotoArquivo(e.foto, Modifier.size(64.dp).clip(MlFormas.campo), ladoMaxPx = 256, descricao = e.titulo)
        }
    }
}
