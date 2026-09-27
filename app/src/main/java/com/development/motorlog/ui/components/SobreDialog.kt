package com.development.motorlog.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.development.motorlog.R

// Chave Pix do autor pra apoio voluntário ao desenvolvimento. Não desbloqueia nada no app
// (importante pra política da Play Store): é só uma forma de agradecer.
const val CHAVE_PIX = "0a19a037-e6e6-46b7-9538-c529b6f70fc8"

// lembrete = true: o pedido de apoio que aparece sozinho ao abrir o app (só pra quem já usa o app,
// no máximo 1x por mês — domain/Apoio.kt) — título e botão de fechar mais leves; o conteúdo (Pix,
// copiar) é o mesmo do "Sobre".
@Composable
fun SobreDialog(onFechar: () -> Unit, onMensagem: (String) -> Unit, lembrete: Boolean = false) {
    val clipboard = LocalClipboard.current
    val escopo = rememberCoroutineScope()
    val contexto = LocalContext.current
    val versao = runCatching { contexto.packageManager.getPackageInfo(contexto.packageName, 0).versionName }.getOrNull() ?: ""

    AlertDialog(
        onDismissRequest = onFechar,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                if (lembrete) "O MotorLog te ajuda? 🏍️" else "MotorLog${if (versao.isNotBlank()) " · v$versao" else ""}",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column {
                Text(
                    "O caderninho de manutenção que faz a conta sozinho. Feito por um motoboy, de graça e sem " +
                        "anúncios. Seus dados ficam só no seu celular.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(14.dp))
                SectionLabel("Apoio solidário")
                Text(
                    "Se o app te ajuda e você quiser colaborar com o desenvolvimento, qualquer valor via Pix é " +
                        "bem-vindo. Não libera nada no app — é só um agradecimento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                MlCard(pad = 12.dp) {
                    Text("Chave Pix (aleatória)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(CHAVE_PIX, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(6.dp))
                    BotaoSecundario(
                        "Copiar chave Pix",
                        onClick = {
                            escopo.launch {
                                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Chave Pix MotorLog", CHAVE_PIX)))
                                onMensagem("Chave Pix copiada. Cole no app do seu banco.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icone = R.drawable.ic_ml_share,
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onFechar) { Text(if (lembrete) "Agora não" else "Fechar") } },
    )
}
