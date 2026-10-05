package com.development.motorlog.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Como o backup automático funciona e como conferir se está ligado. O app não consegue ver nem
// ligar o backup do Google (é do sistema): só explicar e abrir as configurações.
@Composable
fun BackupDoGoogleDialog(onFechar: () -> Unit) {
    val contexto = LocalContext.current
    AlertDialog(
        onDismissRequest = onFechar,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("Seu histórico no backup do Google", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Tudo o que você registra (motos, trocas, km, abastecimentos, cuidados, rolês) e uma cópia menor das fotos " +
                        "vão sozinhos pro backup da sua conta Google, mais ou menos uma vez por dia, com o celular carregando e no Wi-Fi.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Celular roubado, quebrado ou novo? No celular novo, entre com a mesma conta Google, aceite restaurar o backup " +
                        "e instale o MotorLog pela Play Store: o histórico volta sozinho.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Pra isso funcionar, o backup do Google precisa estar ligado no celular. Confira em Configurações → Google → " +
                        "Backup (em alguns celulares: Configurações → Sistema → Backup) e deixe \"Backup do Google One\" ligado.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                try {
                    contexto.startActivity(Intent(Settings.ACTION_SETTINGS))
                } catch (_: ActivityNotFoundException) {
                }
                onFechar()
            }) { Text("Abrir configurações") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Fechar") } },
    )
}
