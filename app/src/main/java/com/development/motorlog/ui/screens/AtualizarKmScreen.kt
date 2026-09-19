package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.viewModels.MotoViewModel

// atalhos de km: um dia de trabalho de motoboy fica entre 100 e 300 km
private val ATALHOS_KM = listOf(50, 100, 200, 500)

// A ação nº 1 do app. Meta: 2 toques (um atalho + Salvar, ou digitar + Done no teclado).
@Composable
fun AtualizarKmScreen(
    modifier: Modifier = Modifier,
    moto: Moto,
    viewModel: MotoViewModel = viewModel(),
    onSalvar: (kmSalvo: Int) -> Unit,
) {
    // nasce com o km atual já selecionado: digitar substitui, sem precisar apagar
    var campo by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        val texto = moto.kilometragem.toString()
        mutableStateOf(TextFieldValue(texto, selection = TextRange(0, texto.length)))
    }
    // km menor que o atual quase sempre é erro de digitação; pede um 2º toque pra confirmar
    var confirmarMenor by rememberSaveable { mutableStateOf(false) }
    val foco = remember { FocusRequester() }

    val novoKm = campo.text.toIntOrNull()
    val delta = novoKm?.minus(moto.kilometragem)

    fun salvar() {
        val km = novoKm ?: return
        if (km < moto.kilometragem && !confirmarMenor) {
            confirmarMenor = true
            return
        }
        viewModel.atualizarKm(moto, km)
        onSalvar(km)
    }

    LaunchedEffect(Unit) { foco.requestFocus() }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionLabel("Quilometragem atual")
        Text(formatarKm(moto.kilometragem), color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = campo,
            onValueChange = { campo = it; confirmarMenor = false },
            label = { Text("Nova quilometragem") },
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { salvar() }),
            isError = delta != null && delta < 0,
            supportingText = {
                when {
                    novoKm == null && campo.text.isNotEmpty() -> Text("Digite só números")
                    delta == null || delta == 0 -> Text("Some o que rodou ou digite o valor do painel")
                    delta > 0 -> Text("+${formatarKm(delta)} desde a última atualização", color = MaterialTheme.colorScheme.primary)
                    else -> Text(
                        if (confirmarMenor) "Toque em Salvar de novo pra confirmar o km menor."
                        else "Menor que o km atual (${formatarNumero(moto.kilometragem)}). Confira o painel da moto.",
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().focusRequester(foco),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ATALHOS_KM.forEach { passo ->
                OutlinedButton(
                    onClick = {
                        val base = novoKm ?: moto.kilometragem
                        val texto = (base + passo).toString()
                        campo = TextFieldValue(texto, selection = TextRange(texto.length))
                        confirmarMenor = false
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) { Text("+$passo", fontSize = 13.sp, textAlign = TextAlign.Center) }
            }
        }

        Spacer(Modifier.height(4.dp))
        Button(
            onClick = { salvar() },
            enabled = novoKm != null,
            modifier = Modifier.fillMaxWidth().height(54.dp),
        ) {
            Text(
                if (confirmarMenor) "Salvar mesmo assim" else "Salvar km",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
