package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Peca
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.MlTextField
import com.development.motorlog.ui.components.RodapeDeForm
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.iconeDaPeca
import com.development.motorlog.ui.viewModels.RegistroViewModel

@Composable
fun FormPecaScreen(
    peca: Peca?,
    modifier: Modifier = Modifier,
    viewModel: RegistroViewModel = viewModel(),
    onSalvar: () -> Unit,
) {
    var nome by rememberSaveable { mutableStateOf(peca?.nome ?: "") }
    var intervalo by rememberSaveable { mutableStateOf(peca?.intervaloKm?.toString() ?: "") }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmarExclusao by rememberSaveable { mutableStateOf(false) }
    val intervaloInt = intervalo.toIntOrNull()

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            SectionLabel("Peça")
            MlTextField(nome, { nome = it; erro = null }, "Nome da peça", icone = iconeDaPeca(nome.ifBlank { "peça" }))
            Spacer(Modifier.height(12.dp))
            SectionLabel("Intervalo")
            MlTextField(intervalo, { intervalo = it; erro = null }, "Troca a cada quantos km?", icone = R.drawable.ic_ml_gauge, numerico = true)
            Spacer(Modifier.height(12.dp))
            MlCard(pad = 14.dp, cor = MaterialTheme.colorScheme.surfaceContainerHigh, borda = androidx.compose.ui.graphics.Color.Transparent) {
                Text(
                    if (intervaloInt != null && intervaloInt > 0)
                        "Cada vez que você registrar a troca desta peça, eu marco a próxima ${formatarKm(intervaloInt)} depois e aviso quando estiver perto."
                    else "Use o intervalo do manual da moto ou o que o mecânico recomenda. Dá pra ajustar depois.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (peca != null) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { confirmarExclusao = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Excluir peça", color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        RodapeDeForm(
            textoBotao = if (peca != null) "Salvar alterações" else "Salvar peça",
            erro = erro,
            icone = R.drawable.ic_ml_check,
            onClick = {
                // intervalo 0 marcaria VENCIDA no instante da troca; negativo inverte a conta
                if (intervaloInt == null || intervaloInt <= 0 || nome.isBlank()) {
                    erro = "Informe o nome e um intervalo em km maior que zero."
                    return@RodapeDeForm
                }
                erro = null
                if (peca != null) viewModel.atualizarPeca(peca.copy(nome = nome, intervaloKm = intervaloInt))
                else viewModel.inserirPeca(Peca(nome = nome, intervaloKm = intervaloInt))
                onSalvar()
            },
        )
    }

    if (peca != null && confirmarExclusao) {
        ConfirmarExclusaoDialog(
            texto = "Excluir a peça \"${peca.nome}\" e as trocas registradas dela?",
            onConfirmar = {
                confirmarExclusao = false
                viewModel.deletarPeca(peca)
                onSalvar()
            },
            onCancelar = { confirmarExclusao = false },
        )
    }
}
