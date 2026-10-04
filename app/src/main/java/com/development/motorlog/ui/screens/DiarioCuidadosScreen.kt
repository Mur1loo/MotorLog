package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Cuidado
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.TipoCuidado
import com.development.motorlog.domain.erroDeKm
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.ui.components.CampoData
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.MlTextField
import com.development.motorlog.ui.theme.MlFormas
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.iconeDoCuidado
import com.development.motorlog.ui.viewModels.CuidadoViewModel

// Diário de cuidados: tudo o que o dono fez pela moto além das trocas, do mais recente pro mais
// antigo. Registrar é no Painel (1 toque, hoje e no km atual); aqui dá pra rever, corrigir (tipo,
// dia, km, nota — o cuidado de ontem anotado hoje) e apagar um toque errado.
@Composable
fun DiarioCuidadosScreen(
    moto: Moto,
    modifier: Modifier = Modifier,
    viewModel: CuidadoViewModel = viewModel(),
) {
    LaunchedEffect(moto.id) { viewModel.carregar(moto.id) }
    val cuidados = viewModel.cuidados
    val accent = accentDaMoto(moto)
    var apagandoId by rememberSaveable { mutableStateOf<Long?>(null) }
    val apagando = apagandoId?.let { id -> cuidados.find { it.id == id } }
    var editandoId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editando = editandoId?.let { id -> cuidados.find { it.id == id } }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                if (cuidados.isEmpty()) "Ainda nada por aqui. No Painel, toque em \"Lavagem simples\", \"Lavagem detalhada\" (com cera, " +
                    "polimento, corrente e rodas), \"Lubrifiquei a corrente\" ou \"Calibrei os pneus\" quando cuidar da ${nomeDaMoto(moto)}."
                else "${cuidados.size} cuidado${if (cuidados.size == 1) "" else "s"} com a ${nomeDaMoto(moto)}. Toque em um pra corrigir o dia, o km ou anotar algo.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        items(cuidados, key = { it.id }) { c ->
            val tipo = TipoCuidado.doCodigo(c.tipo)
            MlCard(onClick = { editandoId = c.id }, pad = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (tipo != null) IconBox(iconeDoCuidado(tipo), cor = accent)
                    Column(Modifier.weight(1f)) {
                        Text(tipo?.nome ?: c.tipo, style = MaterialTheme.typography.titleSmall)
                        Text("${formatarData(c.data)} · ${formatarKm(c.km)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (c.nota.isNotBlank()) Text(c.nota, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { apagandoId = c.id }) { Text("Apagar", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp).fillMaxWidth()) }
    }

    if (editando != null) {
        EditarCuidadoDialog(
            cuidado = editando,
            onSalvar = { viewModel.atualizar(it); editandoId = null },
            onCancelar = { editandoId = null },
        )
    }

    if (apagando != null) {
        ConfirmarExclusaoDialog(
            texto = "Apagar \"${TipoCuidado.doCodigo(apagando.tipo)?.nome ?: apagando.tipo}\" de ${formatarData(apagando.data)}?",
            onConfirmar = {
                viewModel.excluir(apagando)
                apagandoId = null
            },
            onCancelar = { apagandoId = null },
        )
    }
}

// Corrigir um cuidado: o tipo (tocou em "simples" mas foi a detalhada), o dia, o km e uma nota
@Composable
private fun EditarCuidadoDialog(cuidado: Cuidado, onSalvar: (Cuidado) -> Unit, onCancelar: () -> Unit) {
    var tipo by rememberSaveable { mutableStateOf(cuidado.tipo) }
    var data by rememberSaveable { mutableLongStateOf(cuidado.data) }
    var km by rememberSaveable { mutableStateOf(cuidado.km.toString()) }
    var nota by rememberSaveable { mutableStateOf(cuidado.nota) }
    var tentou by rememberSaveable { mutableStateOf(false) }
    val erroKm = erroDeKm(km)
    // os tipos do Painel; um tipo antigo (cera) continua aparecendo se for o deste registro
    val tipos = TipoCuidado.entries.filter { it.noPainel || it.codigo == cuidado.tipo }
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("Editar cuidado", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tipos.forEach { t ->
                        FilterChip(selected = tipo == t.codigo, onClick = { tipo = t.codigo }, label = { Text(t.nome) }, shape = MlFormas.pill)
                    }
                }
                CampoData(data, { data = it }, rotulo = "Quando", cor = MaterialTheme.colorScheme.surfaceContainerHighest)
                MlTextField(km, { km = it }, "Km", icone = R.drawable.ic_ml_gauge, numerico = true, ajuda = erroKm.takeIf { tentou })
                MlTextField(nota, { nota = it.take(80) }, "Nota (opcional)", icone = R.drawable.ic_ml_edit)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                tentou = true
                if (erroKm == null) onSalvar(cuidado.copy(tipo = tipo, data = data, km = km.trim().toInt(), nota = nota))
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
}
