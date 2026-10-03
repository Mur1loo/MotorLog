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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.ui.components.CampoData
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.MlTextField
import com.development.motorlog.ui.components.RodapeDeForm
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.util.contemSemAcento
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.util.iconeDaPeca
import com.development.motorlog.ui.viewModels.RegistroViewModel
import com.development.motorlog.ui.components.CampoReais
import com.development.motorlog.ui.components.reaisOpcional

// "Troquei uma peça" (por conta própria: em casa, com um amigo): busca + lista de peças em cards
// (ícone, nome, intervalo); km, valor da peça e dia fixos embaixo e o Salvar no rodapé — sempre
// visíveis, mesmo com a lista longa. Visita à oficina (com mão de obra) é "Fui à oficina".
@Composable
fun RegistroScreen(
    moto: Moto,
    modifier: Modifier = Modifier,
    viewModel: RegistroViewModel = viewModel(),
    onSalvar: () -> Unit,
) {
    val pecas = viewModel.pecas
    // só o id é salvo (sobrevive ao giro); a peça é resolvida no catálogo
    var pecaSelecionadaId by rememberSaveable { mutableStateOf<Long?>(null) }
    // a troca normalmente é registrada agora → nasce com o km atual da moto
    var km by rememberSaveable { mutableStateOf(moto.kilometragem.toString()) }
    // quanto pagou na peça: opcional (vazio = não informado) — é o que entra no gasto da moto
    var preco by rememberSaveable { mutableStateOf("") }
    var data by rememberSaveable { mutableLongStateOf(hojeUtcMillis()) }
    var busca by rememberSaveable { mutableStateOf("") }
    val pecaSelecionada = pecas.find { it.id == pecaSelecionadaId }
    val pecasFiltradas = pecas.filter { it.nome.contemSemAcento(busca) }
    val precoInt = reaisOpcional(preco)   // centavos

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            SectionLabel("Qual peça?")
            MlTextField(busca, { busca = it }, "Buscar peça", icone = R.drawable.ic_ml_wrench)
            Spacer(Modifier.height(10.dp))
            if (pecas.isEmpty()) {
                Text("Carregando peças…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (pecasFiltradas.isEmpty()) {
                Text("Nenhuma peça com esse nome. Você pode criar em \"Peças e intervalos\".", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pecasFiltradas, key = { it.id }) { peca ->
                    LinhaPeca(peca, selecionada = peca.id == pecaSelecionadaId) { pecaSelecionadaId = peca.id }
                }
                item { Spacer(Modifier.height(4.dp)) }
            }
            Spacer(Modifier.height(8.dp))
            SectionLabel("Km, valor e dia")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MlTextField(km, { km = it }, "Km da troca", Modifier.weight(1f), icone = R.drawable.ic_ml_gauge, numerico = true, erro = km.toIntOrNull() == null)
                CampoReais(preco, { preco = it }, "Valor (opcional)", Modifier.weight(1f), icone = R.drawable.ic_ml_dollar)
            }
            Spacer(Modifier.height(8.dp))
            CampoData(data, { data = it }, rotulo = "Quando trocou")
            Spacer(Modifier.height(10.dp))
        }
        RodapeDeForm(
            textoBotao = if (pecaSelecionada == null) "Escolha a peça" else "Salvar: ${pecaSelecionada.nome}",
            onClick = {
                val novoKm = km.toIntOrNull() ?: return@RodapeDeForm
                val peca = pecaSelecionada ?: return@RodapeDeForm
                val novoPreco = precoInt ?: return@RodapeDeForm
                viewModel.registrarTroca(moto, peca, novoKm, novoPreco, data)
                onSalvar()
            },
            enabled = pecaSelecionada != null && km.toIntOrNull() != null && precoInt != null,
            icone = R.drawable.ic_ml_check,
        )
    }
}

// Linha de peça selecionável (borda accent quando escolhida)
@Composable
fun LinhaPeca(peca: Peca, selecionada: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    MlCard(
        onClick = onClick, pad = 10.dp,
        borda = if (selecionada) accent else MaterialTheme.colorScheme.outline,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBox(iconeDaPeca(peca.nome), cor = if (selecionada) accent else null)
            Column(Modifier.weight(1f)) {
                Text(peca.nome, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("troca a cada ${formatarKm(peca.intervaloKm)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selecionada) Icon(painterResource(R.drawable.ic_ml_check), contentDescription = "Selecionada", tint = accent, modifier = Modifier.size(20.dp))
        }
    }
}
