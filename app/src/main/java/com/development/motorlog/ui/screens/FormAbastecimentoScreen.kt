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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Abastecimento
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.lerLitros
import com.development.motorlog.domain.lerReais
import com.development.motorlog.domain.litrosParaTexto
import com.development.motorlog.domain.litrosPeloValor
import com.development.motorlog.domain.reaisParaTexto
import com.development.motorlog.domain.validarAbastecimento
import com.development.motorlog.domain.validarAbastecimentoPorValor
import com.development.motorlog.ui.components.CampoData
import com.development.motorlog.ui.components.CampoReais
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.MlTextField
import com.development.motorlog.ui.components.RodapeDeForm
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.components.reaisOpcional
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.viewModels.AbastecimentoViewModel

// "Abasteci": km do painel (nasce com o atual), o que foi colocado e o dia. Dois jeitos de dizer
// quanto entrou: "Sei o valor" (paguei R$ 30, litro a R$ 6,29 — o jeito mais comum; o preço do
// último abastecimento já vem preenchido) ou "Sei os litros". Tanque cheio é opcional: só deixa a
// conta exata pra quem às vezes completa (domain/Consumo.kt).
// O km do abastecimento também atualiza o km da moto quando é maior (AbastecimentoViewModel).
// abastecimento != null → edição, com "Excluir".
@Composable
fun FormAbastecimentoScreen(
    moto: Moto,
    modifier: Modifier = Modifier,
    abastecimento: Abastecimento? = null,
    viewModel: AbastecimentoViewModel = viewModel(),
    onSalvar: (kmAtualizado: Boolean) -> Unit,
    onExcluido: () -> Unit = {},
) {
    var km by rememberSaveable { mutableStateOf((abastecimento?.km ?: moto.kilometragem).toString()) }
    var litros by rememberSaveable { mutableStateOf(abastecimento?.let { litrosParaTexto(it.mililitros) } ?: "") }
    var valor by rememberSaveable { mutableStateOf(abastecimento?.valor?.takeIf { it > 0 }?.let(::reaisParaTexto) ?: "") }
    // novo: começa em "Sei o valor"; edição mostra os litros guardados
    var porValor by rememberSaveable { mutableStateOf(abastecimento == null) }
    var preco by rememberSaveable { mutableStateOf(viewModel.resumo?.precoPorLitro?.let(::reaisParaTexto) ?: "") }
    // o resumo pode chegar depois da tela abrir: preenche o preço do último abastecimento se ainda vazio
    LaunchedEffect(viewModel.resumo?.precoPorLitro) {
        val ultimo = viewModel.resumo?.precoPorLitro
        if (preco.isBlank() && ultimo != null) preco = reaisParaTexto(ultimo)
    }
    var tanqueCheio by rememberSaveable { mutableStateOf(abastecimento?.tanqueCheio ?: false) }
    var data by rememberSaveable { mutableLongStateOf(abastecimento?.data ?: hojeUtcMillis()) }
    // os erros só aparecem depois da 1ª tentativa de salvar
    var tentouSalvar by rememberSaveable { mutableStateOf(false) }
    // remember (não Saveable): se a tela for recriada no meio, o botão não fica travado
    var salvando by remember { mutableStateOf(false) }
    var confirmarExclusao by rememberSaveable { mutableStateOf(false) }
    val erros = if (porValor) validarAbastecimentoPorValor(km, valor, preco) else validarAbastecimento(km, litros, valor)
    val litrosCalculados = if (porValor) lerReais(valor)?.let { v -> lerReais(preco)?.let { p -> litrosPeloValor(v, p) } } else null
    fun ajuda(msg: String?) = msg.takeIf { tentouSalvar }

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            SectionLabel("Na bomba")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = porValor, onClick = { porValor = true }, label = { Text("Sei o valor pago") })
                FilterChip(selected = !porValor, onClick = { porValor = false }, label = { Text("Sei os litros") })
            }
            Spacer(Modifier.height(6.dp))
            if (porValor) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CampoReais(valor, { valor = it }, "Valor pago", Modifier.weight(1f), icone = R.drawable.ic_ml_dollar, ajuda = ajuda(erros.valor))
                    CampoReais(preco, { preco = it }, "Preço do litro", Modifier.weight(1f), icone = R.drawable.ic_ml_fuel, ajuda = ajuda(erros.preco))
                }
                Text(
                    litrosCalculados?.let { "= ${litrosParaTexto(it)} litros" } ?: "O preço do litro está na bomba. Eu calculo os litros.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MlTextField(
                        litros, { litros = it }, "Litros", Modifier.weight(1f), icone = R.drawable.ic_ml_fuel,
                        ajuda = ajuda(erros.litros),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    CampoReais(valor, { valor = it }, "Valor (opcional)", Modifier.weight(1f), icone = R.drawable.ic_ml_dollar)
                }
            }
            Spacer(Modifier.height(10.dp))
            SectionLabel("No painel da moto")
            MlTextField(km, { km = it }, "Km na hora de abastecer", icone = R.drawable.ic_ml_gauge, numerico = true, ajuda = ajuda(erros.km))
            Spacer(Modifier.height(10.dp))
            MlCard(pad = 14.dp, cor = MaterialTheme.colorScheme.surfaceContainerHigh, borda = Color.Transparent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Completei o tanque (opcional)", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (tanqueCheio) "Entre dois tanques cheios a conta do km/l fica exata."
                            else "Não precisa encher: o km/l sai da soma dos abastecimentos e fica mais preciso a cada um.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(checked = tanqueCheio, onCheckedChange = { tanqueCheio = it })
                }
            }
            Spacer(Modifier.height(10.dp))
            CampoData(data, { data = it }, rotulo = "Quando abasteceu")
            if (abastecimento != null) {
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { confirmarExclusao = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Excluir abastecimento", color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        RodapeDeForm(
            textoBotao = if (abastecimento != null) "Salvar alterações" else "Salvar abastecimento",
            erro = if (tentouSalvar && !erros.ok) "Corrija os campos em vermelho." else null,
            icone = R.drawable.ic_ml_check,
            enabled = !salvando,
            onClick = {
                tentouSalvar = true
                if (!erros.ok || salvando) return@RodapeDeForm
                // a validação já garantiu que km, litros/valor e preço leem
                val mililitros = (if (porValor) litrosCalculados else lerLitros(litros)) ?: return@RodapeDeForm
                salvando = true
                val novo = Abastecimento(
                    id = abastecimento?.id ?: 0,
                    motoId = moto.id,
                    km = km.trim().toInt(),
                    mililitros = mililitros,
                    valor = (if (porValor) lerReais(valor) else reaisOpcional(valor)) ?: 0,
                    tanqueCheio = tanqueCheio,
                    data = data,
                )
                viewModel.registrar(moto, novo, hojeUtcMillis(), onSalvar)
            },
        )
    }

    if (confirmarExclusao && abastecimento != null) {
        ConfirmarExclusaoDialog(
            texto = "Excluir este abastecimento? O consumo é recalculado sem ele.",
            onConfirmar = {
                confirmarExclusao = false
                viewModel.excluir(abastecimento)
                onExcluido()
            },
            onCancelar = { confirmarExclusao = false },
        )
    }
}
