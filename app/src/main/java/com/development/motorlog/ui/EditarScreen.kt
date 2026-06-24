package com.development.motorlog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.StatusTroca


@Composable
fun EditarKmScreen(
    modifier: Modifier = Modifier,
    moto: Moto,
    viewModel: MotoViewModel = viewModel(),
    registroViewModel: RegistroViewModel = viewModel(),
    onSalvar: () -> Unit,
    onRegistrarTroca: () -> Unit
) {
    var km by remember { mutableStateOf(moto.kilometragem.toString()) }
    val recomendacoes = registroViewModel.recomendacoes

    LaunchedEffect(moto) { registroViewModel.carregarRecomendacoes(moto) }
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Editar km — ${moto.modelo}", fontWeight = FontWeight.Bold)
        Text("Quilometragem Atual: ${moto.kilometragem}", fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = km,
            onValueChange = { novo -> km = novo },
            label = { Text("Quilometragem atual") },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val novoKm: Int = km.toIntOrNull() ?: return@Button
                val motoAtualizada = moto.copy(kilometragem = novoKm)
                viewModel.atualizarMoto(motoAtualizada)
                onSalvar()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Salvar km")
        }
        Button(
            onClick = { onRegistrarTroca() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Registrar troca de peça")
        }

        Text("Próximas trocas", fontWeight = FontWeight.Bold)
        if (recomendacoes.isEmpty()) {
            Text("Nenhuma troca registrada ainda.")
        } else {
            recomendacoes.forEach { rec ->
                val cor = when(rec.statusTroca) {
                    StatusTroca.OK -> Color.Green
                    StatusTroca.PERTO -> Color.Yellow
                    StatusTroca.VENCIDA -> Color.Red
                    StatusTroca.NUNCA_TROCADA -> Color.Gray
                }
                val texto = when (rec.statusTroca) {
                    StatusTroca.NUNCA_TROCADA -> "sem histórico — registre a 1ª troca"
                    StatusTroca.VENCIDA       -> "vencido há ${-(rec.kmRestante ?: 0)} km"
                    else                      -> "faltam ${rec.kmRestante ?: 0} km"
                }
                Text("${rec.pecaNome} — $texto", color = cor)
            }
        }
    }
}