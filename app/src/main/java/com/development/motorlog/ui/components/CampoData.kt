package com.development.motorlog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.development.motorlog.R
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.hojeUtcMillis

// Campo de DATA: um card que mostra o dia e abre o calendário. 'data' é meia-noite UTC (a mesma
// convenção do DatePicker); hoje e ontem aparecem por extenso, que é o caso de quase toda troca.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoData(
    data: Long,
    aoMudar: (Long) -> Unit,
    modifier: Modifier = Modifier,
    rotulo: String = "Data",
    cor: Color = MaterialTheme.colorScheme.surface,
) {
    var mostrarPicker by rememberSaveable { mutableStateOf(false) }
    val hoje = hojeUtcMillis()
    val texto = when (data) {
        hoje -> "Hoje · ${formatarData(data)}"
        hoje - 86_400_000L -> "Ontem · ${formatarData(data)}"
        else -> formatarData(data)
    }
    MlCard(onClick = { mostrarPicker = true }, pad = 12.dp, cor = cor, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBox(R.drawable.ic_ml_calendar, cor = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(rotulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(texto, style = MaterialTheme.typography.titleSmall)
            }
            Text("alterar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }

    if (mostrarPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = data)
        DatePickerDialog(
            onDismissRequest = { mostrarPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(aoMudar)
                    mostrarPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { mostrarPicker = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
