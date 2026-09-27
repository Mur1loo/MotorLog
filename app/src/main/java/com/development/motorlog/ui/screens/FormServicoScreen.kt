package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Moto
import com.development.motorlog.ui.components.CampoData
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.MlTextField
import com.development.motorlog.ui.components.RodapeDeForm
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.util.iconeDaPeca
import com.development.motorlog.data.Servico
import com.development.motorlog.ui.util.contemSemAcento
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.viewModels.RegistroViewModel

// o que um motoboy faz na oficina, do mais ao menos frequente; texto livre continua valendo
private val TIPOS_SUGERIDOS = listOf("Revisão", "Troca de óleo", "Pneu", "Freios", "Relação", "Elétrica", "Alinhamento", "Outro")

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
    // data: o VALOR (Long em millis) — nasce "hoje"; o CampoData abre o calendário
    var data by rememberSaveable { mutableLongStateOf(servico?.data ?: hojeUtcMillis()) }
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

    val pecasFiltradas = pecas.filter { it.nome.contemSemAcento(busca) }

    // O formulário INTEIRO é uma lista rolável e só o Salvar fica fixo. Com campos fixos no topo, o
    // teclado espremia a lista de peças a zero e cobria o campo de preço que estava sendo digitado.
    Column(modifier.fillMaxSize().imePadding()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SectionLabel("O que foi feito?") }
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
            item { MlTextField(tipoServico, { tipoServico = it; erro = null }, "Tipo do serviço", icone = R.drawable.ic_ml_wrench) }
            item { SectionLabel("Quanto e onde") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MlTextField(custo, { custo = it; erro = null }, "Valor pago (R$)", Modifier.weight(1f), icone = R.drawable.ic_ml_dollar, numerico = true)
                    MlTextField(km, { km = it; erro = null }, "Km", Modifier.weight(0.8f), icone = R.drawable.ic_ml_gauge, numerico = true)
                }
            }
            item { MlTextField(local, { local = it; erro = null }, "Nome da oficina", icone = R.drawable.ic_ml_pin) }
            item { CampoData(data, { data = it }) }
            item {
                // ── Peças trocadas neste serviço (opcional) ──
                SectionLabel("Peças trocadas lá (opcional) · ${selecionadas.size} marcada(s)", Modifier.padding(top = 6.dp))
            }
            item { MlTextField(busca, { busca = it }, "Buscar peça", icone = R.drawable.ic_ml_wrench) }
            items(pecasFiltradas, key = { it.id }) { peca ->
                val marcada = selecionadas.containsKey(peca.id)
                MlCard(
                    onClick = { selecionadas = if (marcada) selecionadas - peca.id else selecionadas + (peca.id to "") },
                    pad = 8.dp,
                    borda = if (marcada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(
                            checked = marcada,
                            onCheckedChange = { marcar ->
                                selecionadas = if (marcar) selecionadas + (peca.id to "") else selecionadas - peca.id
                            },
                        )
                        IconBox(iconeDaPeca(peca.nome), cor = if (marcada) MaterialTheme.colorScheme.primary else null, tamanho = 34.dp)
                        Text(peca.nome, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (marcada) {
                            MlTextField(
                                selecionadas[peca.id] ?: "", { selecionadas = selecionadas + (peca.id to it) }, "R$",
                                Modifier.width(112.dp), numerico = true,
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(4.dp)) }
        }

        RodapeDeForm(
            textoBotao = if (servico != null) "Salvar alterações" else "Salvar visita",
            erro = erro,
            icone = R.drawable.ic_ml_check,
            onClick = {
                val custoInt = custo.toIntOrNull()
                val kmInt = km.toIntOrNull()
                if (custoInt == null || kmInt == null || tipoServico.isBlank() || local.isBlank()) {
                    erro = "Preencha todos os campos corretamente!"
                    return@RodapeDeForm
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
                if (servico != null) viewModel.atualizarServicoComPecas(moto, novo, pecasComPreco)
                else viewModel.inserirServicoComPecas(moto, novo, pecasComPreco)
                onSalvar()
            },
        )
    }
}
