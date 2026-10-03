package com.development.motorlog.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.development.motorlog.domain.lerReais

// Campo de valor em R$: aceita "45", "45,90", "1.234,50" (domain/Dinheiro.kt). Teclado com vírgula.
// Vazio não é erro aqui (quem chama decide se o valor é opcional); texto que não dá pra ler fica
// vermelho e diz por quê — campo vermelho sem explicação parece defeito.
// explicar = false: só o vermelho, pra campo estreito (preço da peça dentro da lista da oficina).
@Composable
fun CampoReais(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    icone: Int? = null,
    explicar: Boolean = true,
) {
    val invalido = valor.isNotBlank() && lerReais(valor) == null
    MlTextField(
        valor, aoMudar, rotulo, modifier,
        icone = icone,
        erro = invalido,
        textoApoio = if (invalido && explicar) { { Text("Use só números, ex.: 45,90") } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

// Valor opcional: vazio = R$ 0; texto inválido = null (não deixa salvar)
fun reaisOpcional(texto: String): Int? = if (texto.isBlank()) 0 else lerReais(texto)
