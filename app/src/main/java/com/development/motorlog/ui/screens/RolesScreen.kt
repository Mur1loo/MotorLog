package com.development.motorlog.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.FotoMoto
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Passeio
import com.development.motorlog.domain.detalheDoRole
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.domain.resumirRoles
import com.development.motorlog.domain.validarRole
import com.development.motorlog.ui.components.BotaoPrimario
import com.development.motorlog.ui.components.CampoData
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.components.FotoArquivo
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.LinhaDeTiles
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.MlTextField
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.components.StatTile
import com.development.motorlog.ui.theme.MlFormas
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.viewModels.FotoViewModel
import com.development.motorlog.ui.viewModels.RoleViewModel

// Rolês e viagens: pra onde, quando, km na saída e na volta, quem foi junto e uma foto do álbum.
// Sem GPS: o km do painel basta. Cada rolê entra na linha do tempo (domain/Roles.kt).
@Composable
fun RolesScreen(
    moto: Moto,
    onKmAtualizado: () -> Unit,
    onMensagem: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoleViewModel = viewModel(),
    fotoViewModel: FotoViewModel = viewModel(),
) {
    LaunchedEffect(moto.id) {
        viewModel.carregar(moto.id)
        fotoViewModel.carregar(moto.id)
    }
    val roles = viewModel.roles
    val fotos = fotoViewModel.fotos
    val resumo = resumirRoles(roles)
    val accent = accentDaMoto(moto)
    // null = fechado; 0 = novo; id = editando
    var editandoId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editando = editandoId?.let { id -> roles.find { it.id == id } }

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            if (roles.isEmpty()) {
                Text(
                    "A primeira viagem, o rolê com a galera, a ida à praia. Anote pra onde foram, o km do painel na saída e na volta " +
                        "e escolha uma foto do álbum: vira um capítulo da história da ${nomeDaMoto(moto)}.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            } else {
                LinhaDeTiles {
                    StatTile("Rolês", formatarNumero(resumo.quantidade), Modifier.weight(1f), icone = R.drawable.ic_ml_road)
                    StatTile("Rodados", formatarNumero(resumo.kmTotal), Modifier.weight(1.3f), unidade = "km")
                }
                resumo.maior?.let { maior ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "O maior: ${maior.destino}, ${detalheDoRole(maior, ::formatarKm)?.substringBefore(" · ")}",
                        style = MaterialTheme.typography.bodySmall, color = accent,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            BotaoPrimario("Novo rolê", { editandoId = 0L }, icone = R.drawable.ic_ml_plus, altura = 52.dp)
        }
        if (roles.isNotEmpty()) item { SectionLabel("Rolês e viagens · ${roles.size}") }
        items(roles, key = { it.id }) { r ->
            val foto = fotos.find { it.id == r.fotoId }
            MlCard(onClick = { editandoId = r.id }, pad = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (foto != null) FotoArquivo(foto.arquivo, Modifier.size(56.dp).clip(MlFormas.campo), ladoMaxPx = 256, descricao = r.destino)
                    else IconBox(R.drawable.ic_ml_road, cor = accent)
                    Column(Modifier.weight(1f)) {
                        Text(r.destino, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            listOfNotNull(formatarData(r.data), detalheDoRole(r, ::formatarKm)).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis,
                        )
                        if (r.nota.isNotBlank()) Text(r.nota, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    if (editandoId != null && (editandoId == 0L || editando != null)) {
        RoleDialog(
            role = editando,
            kmAtual = moto.kilometragem,
            fotos = fotos,
            onSalvar = { r ->
                viewModel.salvar(moto, r.copy(motoId = moto.id), hojeUtcMillis()) { kmMudou ->
                    if (kmMudou) onKmAtualizado()
                    onMensagem(if (kmMudou) "Rolê salvo. Km da moto atualizado." else "Rolê salvo na história da ${nomeDaMoto(moto)}.")
                }
                editandoId = null
            },
            onExcluir = editando?.let { r -> { viewModel.excluir(r); editandoId = null } },
            onCancelar = { editandoId = null },
        )
    }
}

// novo (role == null) ou edição: destino, dia, km na saída e na volta, companhia, nota e a foto
@Composable
private fun RoleDialog(
    role: Passeio?,
    kmAtual: Int,
    fotos: List<FotoMoto>,
    onSalvar: (Passeio) -> Unit,
    onExcluir: (() -> Unit)?,
    onCancelar: () -> Unit,
) {
    var destino by rememberSaveable { mutableStateOf(role?.destino ?: "") }
    var data by rememberSaveable { mutableLongStateOf(role?.data ?: hojeUtcMillis()) }
    var kmSaida by rememberSaveable { mutableStateOf((role?.kmSaida ?: kmAtual).toString()) }
    var kmChegada by rememberSaveable { mutableStateOf(role?.kmChegada?.takeIf { it >= 0 }?.toString() ?: "") }
    var companhia by rememberSaveable { mutableStateOf(role?.companhia ?: "") }
    var nota by rememberSaveable { mutableStateOf(role?.nota ?: "") }
    var fotoId by rememberSaveable { mutableLongStateOf(role?.fotoId ?: 0L) }
    var tentou by rememberSaveable { mutableStateOf(false) }
    var confirmarExclusao by rememberSaveable { mutableStateOf(false) }
    val erros = validarRole(destino, kmSaida, kmChegada)
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text(if (role == null) "Novo rolê" else "Editar rolê", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MlTextField(destino, { destino = it.take(60) }, "Pra onde? (ex.: Praia de Ubatuba)", icone = R.drawable.ic_ml_pin, ajuda = erros.destino.takeIf { tentou })
                CampoData(data, { data = it }, rotulo = "Quando", cor = MaterialTheme.colorScheme.surfaceContainerHighest)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MlTextField(kmSaida, { kmSaida = it }, "Km na saída", Modifier.weight(1f), numerico = true, ajuda = erros.kmSaida.takeIf { tentou })
                    MlTextField(kmChegada, { kmChegada = it }, "Km na volta", Modifier.weight(1f), numerico = true, ajuda = erros.kmChegada.takeIf { tentou })
                }
                MlTextField(companhia, { companhia = it.take(60) }, "Quem foi junto (opcional)", icone = R.drawable.ic_ml_moto)
                MlTextField(nota, { nota = it.take(200) }, "Como foi (opcional)", icone = R.drawable.ic_ml_edit)
                if (fotos.isNotEmpty()) {
                    Text("Foto do álbum (opcional)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(fotos, key = { it.id }) { f ->
                            val escolhida = f.id == fotoId
                            FotoArquivo(
                                f.arquivo,
                                Modifier.size(64.dp).clip(MlFormas.campo)
                                    .then(if (escolhida) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, MlFormas.campo) else Modifier)
                                    .clickable(onClickLabel = if (escolhida) "tirar a foto" else "escolher a foto") { fotoId = if (escolhida) 0L else f.id },
                                ladoMaxPx = 256, descricao = f.legenda.ifBlank { null },
                            )
                        }
                    }
                }
                if (onExcluir != null) TextButton(onClick = { confirmarExclusao = true }) { Text("Excluir rolê", color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                tentou = true
                if (erros.ok) {
                    val base = role ?: Passeio(motoId = 0, destino = "", data = data, kmSaida = 0)
                    onSalvar(
                        base.copy(
                            destino = destino, data = data, kmSaida = kmSaida.trim().toInt(),
                            kmChegada = kmChegada.trim().toIntOrNull() ?: -1, companhia = companhia, nota = nota, fotoId = fotoId,
                        ),
                    )
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
    if (confirmarExclusao && onExcluir != null) {
        ConfirmarExclusaoDialog(
            texto = "Excluir o rolê \"${role?.destino}\"? A foto continua no álbum.",
            onConfirmar = { confirmarExclusao = false; onExcluir() },
            onCancelar = { confirmarExclusao = false },
        )
    }
}
