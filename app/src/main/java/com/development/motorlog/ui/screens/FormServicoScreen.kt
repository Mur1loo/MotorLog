package com.development.motorlog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Servico
import com.development.motorlog.ui.util.contemSemAcento
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.viewModels.RegistroViewModel

// o que um motoboy faz na oficina, do mais ao menos frequente; texto livre continua valendo
private val TIPOS_SUGERIDOS = listOf("Revisão", "Troca de óleo", "Pneu", "Freios", "Relação", "Elétrica", "Alinhamento", "Outro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// servico == null → novo; servico != null → edição (peças marcadas nascem das trocas ligadas a ele)
fun FormServicoScreen(
    moto: Moto,
    modifier: Modifier = Modifier,
    servico: Servico? = null,
    viewModel: RegistroViewModel = viewModel(),
    onSalvar: () -> Unit,
) {
    var tipoServico by rememberSaveable { mutableStateOf(servico?.tipoServico ?: "") }
    var custo by rememberSaveable { mutableStateOf(servico?.custo?.toString() ?: "") }
    var local by rememberSaveable { mutableStateOf(servico?.local ?: "") }
    // km nasce do km atual da moto (o serviço normalmente é feito agora)
    var km by rememberSaveable { mutableStateOf((servico?.kilometragem ?: moto.kilometragem).toString()) }
    // data: o VALOR (Long em millis) — nasce "hoje". mostrarPicker: o calendário está ABERTO?
    var data by rememberSaveable { mutableLongStateOf(servico?.data ?: hojeUtcMillis()) }
    var mostrarPicker by remember { mutableStateOf(false) }
    var busca by rememberSaveable { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    val pecas = viewModel.pecas
    // peça marcada -> texto do preço digitado (presença na chave = selecionada).
    // Map imutável em rememberSaveable (HashMap é Serializable → sobrevive ao giro).
    var selecionadas by rememberSaveable { mutableStateOf<Map<Long, String>>(emptyMap()) }
    // edição: carrega as trocas do serviço uma vez e pré-marca (preço como texto)
    var carregouSelecao by rememberSaveable { mutableStateOf(servico == null) }
    if (servico != null) {
        LaunchedEffect(servico) { viewModel.carregarRegistrosDoServico(servico.id) }
        val registros = viewModel.registrosDoServico
        if (!carregouSelecao && registros.isNotEmpty() && registros.all { it.servicoId == servico.id }) {
            selecionadas = registros.associate { it.pecaId to it.preco.toString() }
            carregouSelecao = true
        }
    }

    val dataFormatada = remember(data) { formatarData(data) }
    val pecasFiltradas = pecas.filter { it.nome.contemSemAcento(busca) }

    // O formulário INTEIRO é uma lista rolável e só o Salvar fica fixo. Com campos fixos no topo, o
    // teclado espremia a lista de peças a zero e cobria o campo de preço que estava sendo digitado.
    Column(modifier.fillMaxSize().padding(16.dp).imePadding()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                // sugestões: 1 toque em vez de digitar no trânsito, e o histórico agrupa ("Revisão" ≠ "revisao")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy((-6).dp)) {
                    TIPOS_SUGERIDOS.forEach { tipo ->
                        FilterChip(
                            selected = tipoServico == tipo,
                            onClick = { tipoServico = tipo },
                            label = { Text(tipo) },
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = tipoServico,
                    onValueChange = { tipoServico = it },
                    label = { Text("O que foi feito?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = custo,
                    onValueChange = { custo = it },
                    label = { Text("Quanto pagou? (R$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = local,
                    onValueChange = { local = it },
                    label = { Text("Nome da oficina") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = km,
                    onValueChange = { km = it },
                    label = { Text("Quilometragem") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                // campo de DATA: um botão que mostra a data atual e abre o calendário
                OutlinedButton(
                    onClick = { mostrarPicker = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Text("Data: $dataFormatada")
                }
            }
            item {
                // ── Peças trocadas neste serviço (opcional) ──
                Text(
                    "Peças trocadas lá (opcional) · ${selecionadas.size} marcada(s)",
                    fontWeight = FontWeight.Medium,
                )
            }
            item {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    label = { Text("Buscar peça") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            items(pecasFiltradas, key = { it.id }) { peca ->
                val marcada = selecionadas.containsKey(peca.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selecionadas = if (marcada) selecionadas - peca.id else selecionadas + (peca.id to "")
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = marcada,
                        onCheckedChange = { marcar ->
                            selecionadas = if (marcar) selecionadas + (peca.id to "") else selecionadas - peca.id
                        },
                    )
                    Text(peca.nome, modifier = Modifier.weight(1f))
                    if (marcada) {
                        OutlinedTextField(
                            value = selecionadas[peca.id] ?: "",
                            onValueChange = { selecionadas = selecionadas + (peca.id to it) },
                            label = { Text("R$") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(120.dp),
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(4.dp)) }
        }

        val erroAtual = erro
        if (erroAtual != null) {
            Text(erroAtual, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 6.dp))
        }

        Button(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 52.dp),
            onClick = {
                val custoInt = custo.toIntOrNull()
                val kmInt = km.toIntOrNull()
                if (custoInt == null || kmInt == null || tipoServico.isBlank() || local.isBlank()) {
                    erro = "Preencha todos os campos corretamente!"
                    return@Button
                }
                erro = null
                val novo = Servico(
                    id = servico?.id ?: 0,
                    motoId = moto.id,
                    custo = custoInt,
                    kilometragem = kmInt,
                    tipoServico = tipoServico,
                    data = data,
                    local = local,
                )
                // texto do preço -> Int (vazio/invalid vira 0)
                val pecasComPreco = selecionadas.mapValues { it.value.toIntOrNull() ?: 0 }
                if (servico != null) viewModel.atualizarServicoComPecas(novo, pecasComPreco)
                else viewModel.inserirServicoComPecas(novo, pecasComPreco)
                onSalvar()
            }
        ) {
            Text(if (servico != null) "Salvar alterações" else "Salvar serviço")
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
