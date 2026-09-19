package com.development.motorlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Moto
import com.development.motorlog.ui.components.BotaoPrimario
import com.development.motorlog.ui.theme.MlBgElev
import com.development.motorlog.ui.theme.MlBorder
import com.development.motorlog.ui.theme.MlOdoBg
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.theme.chakra
import com.development.motorlog.ui.theme.cor
import com.development.motorlog.domain.StatusTroca
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.viewModels.MotoViewModel

// atalhos de km: um dia de trabalho de motoboy fica entre 100 e 300 km
private val ATALHOS_KM = listOf(50, 100, 250, 500)
private val TECLAS = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "00", "0", "del")

// A ação nº 1 do app, como folha inferior (UpdateKmSheet do protótipo): número grande no
// visor escuro, delta colorido, atalhos e teclado próprio — nada de teclado do sistema.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtualizarKmSheet(
    moto: Moto,
    onFechar: () -> Unit,
    onSalvo: (kmSalvo: Int) -> Unit,
    viewModel: MotoViewModel = viewModel(),
) {
    var texto by rememberSaveable { mutableStateOf(moto.kilometragem.toString()) }
    // como numa calculadora: o 1º dígito digitado substitui o valor atual (em vez de virar 160001)
    var intocado by rememberSaveable { mutableStateOf(true) }
    // km menor que o atual quase sempre é erro de digitação; pede um 2º toque pra confirmar
    var confirmarMenor by rememberSaveable { mutableStateOf(false) }
    val accent = accentDaMoto(moto.id)
    val novoKm = texto.toIntOrNull() ?: 0
    val delta = novoKm - moto.kilometragem

    fun tecla(k: String) {
        confirmarMenor = false
        val base = if (intocado) "" else texto
        intocado = false
        texto = when (k) {
            "del" -> if (base.length <= 1) "0" else base.dropLast(1)
            else -> (if (base == "0") "" else base).plus(k).take(7).ifEmpty { "0" }
        }
    }
    fun salvar() {
        if (delta < 0 && !confirmarMenor) { confirmarMenor = true; return }
        viewModel.atualizarKm(moto, novoKm)
        onSalvo(novoKm)
    }

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MlBgElev,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 20.dp).navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Atualizar km", style = MaterialTheme.typography.titleLarge)
                    Text("${moto.modelo} · era ${formatarKm(moto.kilometragem)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    painterResource(R.drawable.ic_ml_close), contentDescription = "Fechar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).clickable { onFechar() }.padding(10.dp),
                )
            }
            Spacer(Modifier.height(12.dp))

            // visor
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MlOdoBg).padding(vertical = 18.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(formatarNumero(novoKm), style = chakra(46.sp), color = Color.White)
                    Spacer(Modifier.size(6.dp))
                    Text("km", style = chakra(18.sp), color = accent, modifier = Modifier.padding(bottom = 6.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    when {
                        confirmarMenor -> "Menor que o km atual. Toque em Confirmar de novo se estiver certo."
                        delta > 0 -> "+${formatarKm(delta)} rodados"
                        delta < 0 -> "${formatarKm(delta)} (abaixo do atual)"
                        intocado -> "digite o km do painel ou some o que rodou"
                        else -> "sem alteração"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = when {
                        delta > 0 -> StatusTroca.OK.cor()
                        delta < 0 -> StatusTroca.VENCIDA.cor()
                        else -> Color.White.copy(alpha = 0.5f)
                    },
                )
            }
            Spacer(Modifier.height(12.dp))

            // atalhos
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ATALHOS_KM.forEach { passo ->
                    Tecla("+$passo", Modifier.weight(1f), altura = 40.dp, fonte = 14.sp) {
                        confirmarMenor = false
                        intocado = false
                        texto = (novoKm + passo).toString()
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // teclado 3×4
            TECLAS.chunked(3).forEach { linha ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    linha.forEach { k ->
                        if (k == "del") {
                            Box(
                                modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable { tecla(k) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(painterResource(R.drawable.ic_ml_back), contentDescription = "Apagar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                            }
                        } else {
                            Tecla(k, Modifier.weight(1f)) { tecla(k) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))

            BotaoPrimario(
                texto = if (confirmarMenor) "Confirmar mesmo assim" else "Confirmar ${formatarKm(novoKm)}",
                onClick = { salvar() },
                icone = R.drawable.ic_ml_check,
                enabled = novoKm > 0,
            )
            if (delta < 0 && !confirmarMenor) {
                TextButton(onClick = onFechar, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
            }
        }
    }
}

@Composable
private fun Tecla(rotulo: String, modifier: Modifier, altura: androidx.compose.ui.unit.Dp = 48.dp, fonte: androidx.compose.ui.unit.TextUnit = 21.sp, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(altura).clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MlBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(rotulo, style = chakra(fonte, androidx.compose.ui.text.font.FontWeight.SemiBold))
    }
}
