package com.development.motorlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Moto
import com.development.motorlog.ui.components.AvatarDaMoto
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.MlTextField
import com.development.motorlog.ui.components.RodapeDeForm
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.theme.MlAccentsMoto
import com.development.motorlog.ui.theme.NOMES_DAS_CORES
import com.development.motorlog.ui.theme.indiceDaCor
import com.development.motorlog.ui.viewModels.MotoViewModel

// moto == null → cadastro; moto != null → edição de identificação (o km se edita em "Atualizar km")
@Composable
fun CadastroScreen(
    modifier: Modifier = Modifier,
    moto: Moto? = null,
    viewModel: MotoViewModel = viewModel(),
    onSalvar: () -> Unit
) {
    var modelo by rememberSaveable { mutableStateOf(moto?.modelo ?: "") }
    var placa by rememberSaveable { mutableStateOf(moto?.placa ?: "") }
    var ano by rememberSaveable { mutableStateOf(moto?.anoFabricacao?.toString() ?: "") }
    var km by rememberSaveable { mutableStateOf(moto?.kilometragem?.toString() ?: "") }
    var revisao by rememberSaveable { mutableStateOf(moto?.intervaloRevisaoKm?.takeIf { it > 0 }?.toString() ?: "") }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    // cor da moto no app: a atual (escolhida ou automática); moto nova ganha a próxima da fila
    var cor by rememberSaveable { mutableIntStateOf(moto?.let(::indiceDaCor) ?: (viewModel.motos.size % 4)) }
    val accent = MlAccentsMoto[cor]

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            // prévia da moto (badge com a cor que ela vai ter)
            MlCard(modifier = Modifier.fillMaxWidth(), pad = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    AvatarDaMoto(moto?.let { viewModel.capas[it.id] }, accent, tamanho = 64.dp)
                    Column {
                        Text(modelo.ifBlank { "Sua moto" }, style = MaterialTheme.typography.titleLarge)
                        Text(
                            listOfNotNull(ano.ifBlank { null }, placa.ifBlank { null }).joinToString(" · ").ifBlank { "modelo, ano e placa" },
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            SectionLabel("Identificação")
            MlTextField(modelo, { modelo = it; erro = null }, "Modelo (ex.: Fan 160, Crosser)", icone = R.drawable.ic_ml_moto)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MlTextField(placa, { placa = it.uppercase(); erro = null }, "Placa", Modifier.weight(1.3f), icone = R.drawable.ic_ml_tag)
                MlTextField(ano, { ano = it; erro = null }, "Ano", Modifier.weight(1f), numerico = true)
            }
            Spacer(Modifier.height(16.dp))
            SectionLabel("Cor da moto no app")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MlAccentsMoto.forEachIndexed { i, c ->
                    val escolhida = i == cor
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(c)
                            .then(if (escolhida) Modifier.border(3.dp, Color.White, CircleShape) else Modifier)
                            .clickable { cor = i }
                            .semantics { contentDescription = NOMES_DAS_CORES[i] + if (escolhida) " (escolhida)" else "" },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (escolhida) Icon(painterResource(R.drawable.ic_ml_check), contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Aparece no painel, no odômetro e no PDF do histórico. Ajuda a diferenciar quando você tem mais de uma moto.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
            Spacer(Modifier.height(16.dp))
            SectionLabel("Revisão na oficina")
            MlTextField(revisao, { revisao = it; erro = null }, "Revisão a cada quantos km? (opcional)", icone = R.drawable.ic_ml_wrench, numerico = true)
            Spacer(Modifier.height(6.dp))
            Text(
                "Ex.: 5.000. Eu aviso quando a próxima revisão estiver chegando, contando da última visita registrada como \"Revisão\". Vazio = não avisar.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
            if (moto == null) {
                Spacer(Modifier.height(16.dp))
                SectionLabel("Quilometragem")
                MlTextField(km, { km = it; erro = null }, "Km que aparece no painel", icone = R.drawable.ic_ml_gauge, numerico = true)
                Spacer(Modifier.height(8.dp))
                MlCard(pad = 14.dp, cor = MaterialTheme.colorScheme.surfaceContainerHigh, borda = Color.Transparent) {
                    Text(
                        "Esse número é o coração do app: tudo o que eu calculo parte dele. Depois, é só manter atualizado com o botão laranja.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        RodapeDeForm(
            textoBotao = if (moto != null) "Salvar alterações" else "Salvar moto",
            erro = erro,
            icone = R.drawable.ic_ml_check,
            onClick = {
                val newAno = ano.toIntOrNull()
                val newKm = km.toIntOrNull()
                if (newAno == null || (moto == null && newKm == null) || modelo.isBlank() || placa.isBlank()) {
                    erro = "Preencha modelo, placa, ano" + if (moto == null) " e o km do painel." else "."
                    return@RodapeDeForm
                }
                if ((newKm ?: 0) < 0 || newAno !in 1900..2100) {
                    erro = "Ano ou quilometragem inválidos."
                    return@RodapeDeForm
                }
                val newRevisao = if (revisao.isBlank()) 0 else revisao.toIntOrNull()
                if (newRevisao == null || newRevisao < 0) {
                    erro = "Intervalo de revisão inválido (deixe vazio pra não avisar)."
                    return@RodapeDeForm
                }
                erro = null
                if (moto != null) {
                    viewModel.atualizarMoto(moto.copy(modelo = modelo, placa = placa, anoFabricacao = newAno, intervaloRevisaoKm = newRevisao, cor = cor))
                } else {
                    viewModel.inserirMoto(Moto(modelo = modelo, anoFabricacao = newAno, placa = placa, kilometragem = newKm!!, intervaloRevisaoKm = newRevisao, cor = cor))
                }
                onSalvar()
            },
        )
    }
}
