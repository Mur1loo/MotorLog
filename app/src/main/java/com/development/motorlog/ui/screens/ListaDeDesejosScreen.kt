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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Desejo
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.erroDeKm
import com.development.motorlog.domain.estaInstalado
import com.development.motorlog.domain.lerReais
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.domain.reaisParaTexto
import com.development.motorlog.domain.resumirDesejos
import com.development.motorlog.domain.validarDesejo
import com.development.motorlog.ui.components.BotaoPrimario
import com.development.motorlog.ui.components.CampoData
import com.development.motorlog.ui.components.CampoReais
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.MlTextField
import com.development.motorlog.ui.components.Pill
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.theme.chakra
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarReais
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.viewModels.DesejoViewModel

// Lista de desejos: o que o dono quer colocar na moto, com preço e nota (onde comprar, cor…).
// "Instalei" pede o dia, o km e quanto pagou, e o item passa pra "Já está na moto" e entra na
// linha do tempo. Personalização não entra no gasto de manutenção (domain/Desejos.kt).
@Composable
fun ListaDeDesejosScreen(
    moto: Moto,
    onMensagem: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DesejoViewModel = viewModel(),
) {
    LaunchedEffect(moto.id) { viewModel.carregar(moto.id) }
    val resumo = resumirDesejos(viewModel.desejos)
    val accent = accentDaMoto(moto)
    // null = fechado; 0 = novo; id = editando
    var editandoId by rememberSaveable { mutableStateOf<Long?>(null) }
    var instalandoId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editando = editandoId?.let { id -> viewModel.desejos.find { it.id == id } }
    val instalando = instalandoId?.let { id -> viewModel.desejos.find { it.id == id } }

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(
                "O que você quer colocar na ${nomeDaMoto(moto)}? Baú, protetor de motor, viseira, manopla… " +
                    "Anote aqui com o preço; quando instalar, vira um capítulo da história dela.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
            )
            Spacer(Modifier.height(8.dp))
            BotaoPrimario("Adicionar desejo", { editandoId = 0L }, icone = R.drawable.ic_ml_plus, altura = 52.dp)
        }
        if (resumo.pendentes.isNotEmpty()) {
            item {
                MlCard(pad = 4.dp) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        SectionLabel(
                            "Quero colocar · ${resumo.pendentes.size}",
                            direita = { if (resumo.faltaInvestir > 0) Text(formatarReais(resumo.faltaInvestir), style = chakra(13.sp), color = accent) },
                        )
                        resumo.pendentes.forEachIndexed { i, d ->
                            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Row(
                                Modifier.fillMaxWidth().clickable(onClickLabel = "editar") { editandoId = d.id }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                IconBox(R.drawable.ic_ml_spark, cor = accent)
                                Column(Modifier.weight(1f)) {
                                    Text(d.nome, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        listOfNotNull(d.preco.takeIf { it > 0 }?.let(::formatarReais), d.nota.ifBlank { null }).joinToString(" · ").ifBlank { "sem preço ainda" },
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                TextButton(onClick = { instalandoId = d.id }) { Text("Instalei") }
                            }
                        }
                    }
                }
            }
        }
        if (resumo.instalados.isNotEmpty()) {
            item {
                MlCard(pad = 4.dp) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        SectionLabel(
                            "Já está na moto · ${resumo.instalados.size}",
                            direita = { if (resumo.investido > 0) Text("${formatarReais(resumo.investido)} investidos", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        )
                        resumo.instalados.forEachIndexed { i, d ->
                            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Row(
                                Modifier.fillMaxWidth().clickable(onClickLabel = "editar") { editandoId = d.id }.padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                IconBox(R.drawable.ic_ml_check, cor = MaterialTheme.colorScheme.primary)
                                Column(Modifier.weight(1f)) {
                                    Text(d.nome, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        listOfNotNull(formatarData(d.instaladoEm), d.kmInstalado.takeIf { it >= 0 }?.let(::formatarKm)).joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (d.preco > 0) Text(formatarReais(d.preco), style = chakra(14.sp))
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    if (editandoId != null && (editandoId == 0L || editando != null)) {
        EditarDesejoDialog(
            desejo = editando,
            onSalvar = { nome, preco, nota ->
                val base = editando ?: Desejo(motoId = moto.id, nome = "", criadoEm = hojeUtcMillis())
                viewModel.salvar(base.copy(nome = nome, preco = preco, nota = nota))
                onMensagem(if (editando == null) "Anotado na lista de desejos." else "Desejo atualizado.")
                editandoId = null
            },
            onVoltarPraLista = editando?.takeIf(::estaInstalado)?.let { d ->
                { viewModel.salvar(d.copy(instaladoEm = 0, kmInstalado = -1)); editandoId = null }
            },
            onExcluir = editando?.let { d -> { viewModel.excluir(d); editandoId = null } },
            onCancelar = { editandoId = null },
        )
    }
    if (instalando != null) {
        InstalarDesejoDialog(
            desejo = instalando,
            kmAtual = moto.kilometragem,
            onConfirmar = { data, km, preco ->
                viewModel.salvar(instalando.copy(instaladoEm = data, kmInstalado = km, preco = preco))
                onMensagem("${instalando.nome} instalado! Entrou na história da ${nomeDaMoto(moto)}.")
                instalandoId = null
            },
            onCancelar = { instalandoId = null },
        )
    }
}

// novo (desejo == null) ou edição: nome, preço e nota. Instalado: dá pra voltar pra lista.
@Composable
private fun EditarDesejoDialog(
    desejo: Desejo?,
    onSalvar: (nome: String, preco: Int, nota: String) -> Unit,
    onVoltarPraLista: (() -> Unit)?,
    onExcluir: (() -> Unit)?,
    onCancelar: () -> Unit,
) {
    var nome by rememberSaveable { mutableStateOf(desejo?.nome ?: "") }
    var preco by rememberSaveable { mutableStateOf(desejo?.preco?.takeIf { it > 0 }?.let(::reaisParaTexto) ?: "") }
    var nota by rememberSaveable { mutableStateOf(desejo?.nota ?: "") }
    var tentou by rememberSaveable { mutableStateOf(false) }
    var confirmarExclusao by rememberSaveable { mutableStateOf(false) }
    val erros = validarDesejo(nome, preco)
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text(if (desejo == null) "Novo desejo" else desejo.nome, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MlTextField(nome, { nome = it.take(60) }, "O que você quer colocar", icone = R.drawable.ic_ml_spark, ajuda = erros.nome.takeIf { tentou })
                CampoReais(preco, { preco = it }, if (desejo != null && estaInstalado(desejo)) "Quanto pagou (opcional)" else "Preço (opcional)", icone = R.drawable.ic_ml_dollar)
                MlTextField(nota, { nota = it.take(120) }, "Nota: loja, modelo, cor (opcional)", icone = R.drawable.ic_ml_edit)
                if (onVoltarPraLista != null) TextButton(onClick = onVoltarPraLista) { Text("Não instalei: voltar pra lista") }
                if (onExcluir != null) TextButton(onClick = { confirmarExclusao = true }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                tentou = true
                if (erros.ok) onSalvar(nome, if (preco.isBlank()) 0 else lerReais(preco) ?: 0, nota)
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
    if (confirmarExclusao && onExcluir != null) {
        ConfirmarExclusaoDialog(
            texto = "Excluir \"${desejo?.nome}\" da lista?",
            onConfirmar = { confirmarExclusao = false; onExcluir() },
            onCancelar = { confirmarExclusao = false },
        )
    }
}

// "Instalei": dia, km e quanto pagou de verdade (vem com o preço anotado)
@Composable
private fun InstalarDesejoDialog(desejo: Desejo, kmAtual: Int, onConfirmar: (data: Long, km: Int, preco: Int) -> Unit, onCancelar: () -> Unit) {
    var data by rememberSaveable { mutableLongStateOf(hojeUtcMillis()) }
    var km by rememberSaveable { mutableStateOf(kmAtual.toString()) }
    var preco by rememberSaveable { mutableStateOf(desejo.preco.takeIf { it > 0 }?.let(::reaisParaTexto) ?: "") }
    var tentou by rememberSaveable { mutableStateOf(false) }
    val erroKm = erroDeKm(km)
    val precoOk = preco.isBlank() || lerReais(preco) != null
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("Instalei: ${desejo.nome}", style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Pill("Sai da lista e entra na história", MaterialTheme.colorScheme.primary)
                CampoData(data, { data = it }, rotulo = "Quando", cor = MaterialTheme.colorScheme.surfaceContainerHighest)
                MlTextField(km, { km = it }, "Km da moto", icone = R.drawable.ic_ml_gauge, numerico = true, ajuda = erroKm.takeIf { tentou })
                CampoReais(preco, { preco = it }, "Quanto pagou (opcional)", icone = R.drawable.ic_ml_dollar)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                tentou = true
                if (erroKm == null && precoOk) onConfirmar(data, km.trim().toInt(), if (preco.isBlank()) 0 else lerReais(preco) ?: 0)
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
}
