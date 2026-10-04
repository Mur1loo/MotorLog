package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.descreverTempoJuntos
import com.development.motorlog.domain.escolherCapa
import com.development.motorlog.domain.kmJuntos
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.relatorio.compartilharAlbum
import com.development.motorlog.ui.components.AvatarDaMoto
import com.development.motorlog.ui.components.BotaoHistoricoPdf
import com.development.motorlog.ui.components.BotaoSecundario
import com.development.motorlog.ui.components.CampoData
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.RodapeDeForm
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.viewModels.FotoViewModel
import com.development.motorlog.ui.viewModels.RegistroViewModel
import kotlinx.coroutines.launch

// Despedida (moto vendida): antes de entregar a chave, o histórico em PDF e as fotos pro novo
// dono; depois, a moto sai da garagem do dia a dia e vira lembrança — nada é apagado e dá pra
// desfazer (domain/Despedida.kt). Quem quer apagar tudo usa "Excluir moto" em Editar dados.
@Composable
fun DespedidaScreen(
    moto: Moto,
    onConfirmar: (vendidaEm: Long) -> Unit,
    onMensagem: (String) -> Unit,
    modifier: Modifier = Modifier,
    registroViewModel: RegistroViewModel = viewModel(),
    fotoViewModel: FotoViewModel = viewModel(),
) {
    LaunchedEffect(moto.id) {
        registroViewModel.carregarServicos(moto)
        registroViewModel.carregarRegistrosDaMoto(moto)
        fotoViewModel.carregar(moto.id)
    }
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    val nome = nomeDaMoto(moto)
    val accent = accentDaMoto(moto)
    val fotos = fotoViewModel.fotos
    var data by rememberSaveable { mutableLongStateOf(hojeUtcMillis()) }
    var mandandoFotos by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                AvatarDaMoto(escolherCapa(fotos, moto.fotoCapaId)?.arquivo, accent, tamanho = 64.dp)
                Column(Modifier.weight(1f)) {
                    Text("Hora de passar a $nome adiante", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Ela sai da garagem do dia a dia, mas a história fica guardada como lembrança: trocas, fotos e cuidados. " +
                            "Se ela voltar, dá pra desfazer.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // o que vocês viveram: tempo, km e o que ficou registrado
            MlCard(cor = accent.copy(alpha = 0.10f), borda = accent.copy(alpha = 0.5f)) {
                SectionLabel("Vocês dois")
                val tempo = descreverTempoJuntos(moto.chegouEm, data)
                val km = kmJuntos(moto.kmChegada, moto.kilometragem)?.takeIf { it > 0 }
                Text(
                    listOfNotNull(tempo?.takeIf { it != "hoje" }?.let { "Juntos por $it" }, km?.let { "${formatarKm(it)} rodados" })
                        .joinToString(" · ").ifBlank { "${formatarKm(moto.kilometragem)} no painel" },
                    style = MaterialTheme.typography.titleMedium, color = accent,
                )
                val trocas = registroViewModel.registrosDaMoto.size
                val visitas = registroViewModel.servicos.size
                Text(
                    "$trocas troca${if (trocas == 1) "" else "s"} registrada${if (trocas == 1) "" else "s"} · " +
                        "$visitas visita${if (visitas == 1) "" else "s"} à oficina · ${fotos.size} foto${if (fotos.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // pro novo dono: histórico documentado passa confiança (e ajuda a negociar)
            SectionLabel("Para o novo dono")
            BotaoHistoricoPdf(moto, onMensagem, Modifier.fillMaxWidth())
            if (fotos.isNotEmpty()) {
                BotaoSecundario(
                    if (mandandoFotos) "Separando as fotos…" else "Mandar as fotos do álbum (${fotos.size})", {
                        mandandoFotos = true
                        escopo.launch {
                            val ok = runCatching { compartilharAlbum(contexto, fotos, "Fotos da $nome · MotorLog") }.getOrDefault(false)
                            mandandoFotos = false
                            if (!ok) onMensagem("Não consegui separar as fotos. Tente de novo.")
                        }
                    },
                    Modifier.fillMaxWidth(), icone = R.drawable.ic_ml_camera, enabled = !mandandoFotos,
                )
            }
            Text(
                "As fotos continuam no seu álbum; vai só uma cópia.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SectionLabel("A despedida", Modifier.padding(top = 4.dp))
            CampoData(data, { data = it }, rotulo = "Quando ela foi embora")
            Text(
                "Prefere apagar tudo? Em Editar dados, use \"Excluir moto\".",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
        }
        RodapeDeForm(
            textoBotao = "Guardar a $nome como lembrança",
            onClick = { onConfirmar(data) },
            icone = R.drawable.ic_ml_check,
        )
    }
}
