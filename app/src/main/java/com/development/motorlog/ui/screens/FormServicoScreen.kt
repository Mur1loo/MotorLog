package com.development.motorlog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Servico
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.viewModels.RegistroViewModel
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormServicoScreen(
    moto: Moto,
    viewModel: RegistroViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onSalvar: () -> Unit,
) {
    var tipoServico by rememberSaveable { mutableStateOf("") }
    var custo by rememberSaveable { mutableStateOf("") }
    var local by rememberSaveable { mutableStateOf("") }
    // km nasce do km atual da moto (o serviço normalmente é feito agora)
    var km by rememberSaveable { mutableStateOf(moto.kilometragem.toString()) }
    // data: o VALOR (Long em millis) — nasce "hoje". mostrarPicker: o calendário está ABERTO?
    var data by rememberSaveable { mutableStateOf(hojeUtcMillis()) }
    var mostrarPicker by remember { mutableStateOf(false) }
    var busca by rememberSaveable { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    val pecas = viewModel.pecas
    // peça marcada -> texto do preço digitado (presença na chave = selecionada)
    val selecionadas = remember { mutableStateMapOf<Long, String>() }

    val dataFormatada = remember(data) { formatarData(data) }
    val pecasFiltradas = pecas.filter { it.nome.contains(busca, ignoreCase = true) }

    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = tipoServico,
            onValueChange = { tipoServico = it },
            label = { Text("Tipo de serviço") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = custo,
            onValueChange = { custo = it },
            label = { Text("Custo (R$)") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = local,
            onValueChange = { local = it },
            label = { Text("Oficina") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = km,
            onValueChange = { km = it },
            label = { Text("Quilometragem") },
            modifier = Modifier.fillMaxWidth(),
        )

        // campo de DATA: um botão que mostra a data atual e abre o calendário
        OutlinedButton(
            onClick = { mostrarPicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Data: $dataFormatada")
        }

        // ── Peças trocadas neste serviço (opcional) ──
        Text(
            "Peças trocadas (opcional) · ${selecionadas.size} selecionada(s)",
            fontWeight = FontWeight.Medium,
        )
        OutlinedTextField(
            value = busca,
            onValueChange = { busca = it },
            label = { Text("Buscar peça") },
            modifier = Modifier.fillMaxWidth(),
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
        ) {
            items(pecasFiltradas) { peca ->
                val marcada = selecionadas.containsKey(peca.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (marcada) selecionadas.remove(peca.id) else selecionadas[peca.id] = ""
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = marcada,
                        onCheckedChange = { marcar ->
                            if (marcar) selecionadas[peca.id] = "" else selecionadas.remove(peca.id)
                        },
                    )
                    Text(peca.nome, modifier = Modifier.weight(1f))
                    if (marcada) {
                        OutlinedTextField(
                            value = selecionadas[peca.id] ?: "",
                            onValueChange = { selecionadas[peca.id] = it },
                            label = { Text("R$") },
                            singleLine = true,
                            modifier = Modifier.width(110.dp),
                        )
                    }
                }
            }
        }

        val erroAtual = erro
        if (erroAtual != null) {
            Text(erroAtual, color = MaterialTheme.colorScheme.error)
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val custoInt = custo.toIntOrNull()
                val kmInt = km.toIntOrNull()
                if (custoInt == null || kmInt == null || tipoServico.isBlank() || local.isBlank()) {
                    erro = "Preencha todos os campos corretamente!"
                    return@Button
                }
                erro = null
                val servico = Servico(
                    motoId = moto.id,
                    custo = custoInt,
                    kilometragem = kmInt,
                    tipoServico = tipoServico,
                    data = data,
                    local = local,
                )
                // texto do preço -> Int (vazio/invalid vira 0)
                val pecasComPreco = selecionadas.mapValues { it.value.toIntOrNull() ?: 0 }
                viewModel.inserirServicoComPecas(servico, pecasComPreco)
                onSalvar()
            }
        ) {
            Text("Salvar serviço")
        }
    }

    // O DIÁLOGO DO CALENDÁRIO — só existe na tela quando o booleano manda
    if (mostrarPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = data)
        DatePickerDialog(
            onDismissRequest = { mostrarPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // selectedDateMillis é um Long? em millis — grava no nosso 'data'
                    pickerState.selectedDateMillis?.let { data = it }
                    mostrarPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

// hoje à meia-noite UTC — MESMA base que o DatePicker usa (evita data "um dia errado")
private fun hojeUtcMillis(): Long =
    LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
