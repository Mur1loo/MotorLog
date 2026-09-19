package com.development.motorlog.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.motorlog.ui.theme.MlFormas
import com.development.motorlog.ui.theme.chakra

// Botão de ação principal do protótipo: accent, raio 15, Chakra Petch, brilho laranja por baixo.
@Composable
fun BotaoPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icone: Int? = null,
    enabled: Boolean = true,
    altura: Dp = 54.dp,
) {
    val accent = MaterialTheme.colorScheme.primary
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MlFormas.botao,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(altura)
            .then(if (enabled) Modifier.shadow(10.dp, MlFormas.botao, ambientColor = accent, spotColor = accent) else Modifier),
    ) {
        if (icone != null) {
            Icon(painterResource(icone), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, style = chakra(16.sp))
    }
}

// Ação secundária: contorno, ícone + texto, 48dp
@Composable
fun BotaoSecundario(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, icone: Int? = null, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, enabled = enabled, shape = MlFormas.botao, modifier = modifier.height(48.dp)) {
        if (icone != null) {
            Icon(painterResource(icone), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}
