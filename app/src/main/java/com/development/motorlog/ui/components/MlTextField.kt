package com.development.motorlog.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import com.development.motorlog.ui.theme.MlBorderHi
import com.development.motorlog.ui.theme.MlFormas

// Campo do protótipo (Field): raio 13, borda sutil, accent no foco, ícone opcional à esquerda.
@Composable
fun MlTextField(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    icone: Int? = null,
    numerico: Boolean = false,
    erro: Boolean = false,
    textoApoio: (@Composable () -> Unit)? = null,
    // mensagem de erro do campo (domain/Validacao.kt): deixa o campo vermelho e diz o que corrigir
    ajuda: String? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions? = null,
) {
    val apoio: (@Composable () -> Unit)? = when {
        textoApoio != null -> textoApoio
        ajuda != null -> { { Text(ajuda) } }
        else -> null
    }
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        label = { Text(rotulo) },
        singleLine = true,
        isError = erro || ajuda != null,
        supportingText = apoio,
        leadingIcon = icone?.let { { Icon(painterResource(it), contentDescription = null) } },
        keyboardOptions = keyboardOptions ?: KeyboardOptions(keyboardType = if (numerico) KeyboardType.Number else KeyboardType.Text),
        keyboardActions = keyboardActions,
        shape = MlFormas.campo,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MlBorderHi,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
