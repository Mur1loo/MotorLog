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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.motorlog.R
import com.development.motorlog.data.HistoricoKm
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.StatusTroca
import com.development.motorlog.domain.avisoDeKmForaDeOrdem
import com.development.motorlog.domain.erroDeKm
import com.development.motorlog.domain.kmDesdeOAnterior
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.MlTextField
import com.development.motorlog.ui.theme.chakra
import com.development.motorlog.ui.theme.cor
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm

// Registros de km: cada dia em que o km foi atualizado (1 por dia), do mais recente pro mais
// antigo, com quanto rodou desde o anterior. Tocar corrige ou apaga um km digitado errado — as
// médias de km/mês, os marcos e as previsões se refazem (domain/RegistrosDeKm.kt).
@Composable
fun RegistrosDeKmScreen(
    moto: Moto,
    pontos: List<HistoricoKm>,
    onCorrigir: (HistoricoKm, novoKm: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lista = pontos.sortedWith(compareByDescending<HistoricoKm> { it.data }.thenByDescending { it.id })
    val rodados = kmDesdeOAnterior(pontos)
    var editandoId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editando = editandoId?.let { id -> pontos.find { it.id == id } }

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Text(
                if (lista.isEmpty()) "Ainda não há km registrado da ${nomeDaMoto(moto)}."
                else "Cada dia em que o km da ${nomeDaMoto(moto)} foi atualizado. Errou um número? Toque pra corrigir: " +
                    "as médias, os marcos e as previsões se refazem.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
            )
            Spacer(Modifier.height(8.dp))
        }
        item {
            MlCard(pad = 4.dp) {
                Column(Modifier.padding(horizontal = 12.dp)) {
                    lista.forEachIndexed { i, p ->
                        if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        val delta = rodados[p.id]
                        Row(
                            Modifier.fillMaxWidth().clickable(onClickLabel = "corrigir") { editandoId = p.id }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            IconBox(R.drawable.ic_ml_gauge, cor = MaterialTheme.colorScheme.primary)
                            Column(Modifier.weight(1f)) {
                                Text(formatarData(p.data), style = MaterialTheme.typography.titleSmall)
                                if (delta != null) {
                                    Text(
                                        if (delta >= 0) "+${formatarKm(delta)} desde o anterior" else "${formatarKm(delta)}: abaixo do anterior",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (delta < 0) StatusTroca.VENCIDA.cor() else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Text(formatarKm(p.km), style = chakra(15.sp))
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }

    if (editando != null) {
        CorrigirKmDialog(
            ponto = editando,
            pontos = pontos,
            ehOUnico = pontos.size == 1,
            onSalvar = { km -> onCorrigir(editando, km); editandoId = null },
            onApagar = { onCorrigir(editando, null); editandoId = null },
            onCancelar = { editandoId = null },
        )
    }
}

@Composable
private fun CorrigirKmDialog(
    ponto: HistoricoKm,
    pontos: List<HistoricoKm>,
    ehOUnico: Boolean,
    onSalvar: (Int) -> Unit,
    onApagar: () -> Unit,
    onCancelar: () -> Unit,
) {
    var km by rememberSaveable { mutableStateOf(ponto.km.toString()) }
    var tentou by rememberSaveable { mutableStateOf(false) }
    var confirmarApagar by rememberSaveable { mutableStateOf(false) }
    val erro = erroDeKm(km)
    val aviso = km.trim().toIntOrNull()?.let { avisoDeKmForaDeOrdem(pontos, ponto, it, ::formatarData) }
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("Km de ${formatarData(ponto.data)}", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                MlTextField(km, { km = it }, "Km do painel nesse dia", icone = R.drawable.ic_ml_gauge, numerico = true, ajuda = erro.takeIf { tentou })
                if (aviso != null && erro == null) {
                    Spacer(Modifier.height(6.dp))
                    Text(aviso, style = MaterialTheme.typography.bodySmall, color = StatusTroca.PERTO.cor())
                }
                // o único registro é o ponto de partida da moto: corrigir sim, apagar não
                if (!ehOUnico) {
                    TextButton(onClick = { confirmarApagar = true }) { Text("Apagar este registro", color = MaterialTheme.colorScheme.error) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                tentou = true
                if (erro == null) onSalvar(km.trim().toInt())
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
    if (confirmarApagar) {
        ConfirmarExclusaoDialog(
            texto = "Apagar o km de ${formatarData(ponto.data)}? As médias e os marcos são recalculados sem ele.",
            onConfirmar = { confirmarApagar = false; onApagar() },
            onCancelar = { confirmarApagar = false },
        )
    }
}
