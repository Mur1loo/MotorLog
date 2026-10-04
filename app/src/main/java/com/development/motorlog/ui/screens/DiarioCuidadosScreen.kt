package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.TipoCuidado
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.iconeDoCuidado
import com.development.motorlog.ui.viewModels.CuidadoViewModel

// Diário de cuidados: tudo o que o dono fez pela moto além das trocas, do mais recente pro mais
// antigo. Registrar é no Painel (1 toque); aqui dá pra rever e apagar um toque errado.
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

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                if (cuidados.isEmpty()) "Ainda nada por aqui. No Painel, toque em \"Lavagem simples\", \"Lavagem detalhada\" (com cera, " +
                    "polimento, corrente e rodas), \"Lubrifiquei a corrente\" ou \"Calibrei os pneus\" quando cuidar da ${nomeDaMoto(moto)}."
                else "${cuidados.size} cuidado${if (cuidados.size == 1) "" else "s"} com a ${nomeDaMoto(moto)}.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        items(cuidados, key = { it.id }) { c ->
            val tipo = TipoCuidado.doCodigo(c.tipo)
            MlCard(pad = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (tipo != null) IconBox(iconeDoCuidado(tipo), cor = accent)
                    Column(Modifier.weight(1f)) {
                        Text(tipo?.nome ?: c.tipo, style = MaterialTheme.typography.titleSmall)
                        Text("${formatarData(c.data)} · ${formatarKm(c.km)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { apagandoId = c.id }) { Text("Apagar", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp).fillMaxWidth()) }
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
