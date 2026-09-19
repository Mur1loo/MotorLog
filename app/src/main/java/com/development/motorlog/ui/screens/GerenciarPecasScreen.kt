package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.development.motorlog.data.Peca
import com.development.motorlog.ui.components.IconBox
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.MlTextField
import com.development.motorlog.ui.components.RodapeDeForm
import com.development.motorlog.ui.theme.MlTextFaint
import com.development.motorlog.ui.util.contemSemAcento
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.iconeDaPeca
import com.development.motorlog.ui.viewModels.RegistroViewModel

@Composable
fun GerenciarPecasScreen(
    modifier: Modifier = Modifier,
    viewModel: RegistroViewModel = viewModel(),
    onSalvarPeca: () -> Unit,
    onEditarPeca: (Peca) -> Unit,
) {
    val pecas = viewModel.pecas
    var busca by rememberSaveable { mutableStateOf("") }
    val pecasFiltradas = pecas.filter { it.nome.contemSemAcento(busca) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            MlTextField(busca, { busca = it }, "Buscar peça", icone = R.drawable.ic_ml_wrench)
            Text(
                "${pecasFiltradas.size} de ${pecas.size} peças · toque pra editar o intervalo",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pecasFiltradas, key = { it.id }) { peca ->
                    MlCard(onClick = { onEditarPeca(peca) }, pad = 10.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconBox(iconeDaPeca(peca.nome))
                            Column(Modifier.weight(1f)) {
                                Text(peca.nome, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("a cada ${formatarKm(peca.intervaloKm)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(painterResource(R.drawable.ic_ml_chev_r), contentDescription = null, tint = MlTextFaint, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                item { Spacer(Modifier.height(4.dp)) }
            }
        }
        RodapeDeForm("Adicionar peça", onSalvarPeca, icone = R.drawable.ic_ml_plus)
    }
}
