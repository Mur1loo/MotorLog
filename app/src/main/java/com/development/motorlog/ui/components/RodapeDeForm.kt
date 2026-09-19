package com.development.motorlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.development.motorlog.ui.theme.MlBgElev
import com.development.motorlog.ui.theme.MlBorder

// Rodapé fixo dos formulários (protótipo CadastroScreen): faixa elevada com borda em cima e o
// botão principal; a mensagem de erro aparece logo acima do botão, onde o olho já está.
@Composable
fun RodapeDeForm(textoBotao: String, onClick: () -> Unit, erro: String? = null, enabled: Boolean = true, icone: Int? = null) {
    Column(Modifier.fillMaxWidth().background(MlBgElev)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(MlBorder))
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            if (erro != null) {
                Text(erro, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
            }
            BotaoPrimario(textoBotao, onClick, icone = icone, enabled = enabled, altura = 52.dp)
        }
    }
}
