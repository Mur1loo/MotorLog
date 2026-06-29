package com.development.motorlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.ui.viewModels.MotoViewModel

@Composable
fun CadastroScreen(
    modifier: Modifier = Modifier,
    viewModel: MotoViewModel = viewModel(),
    onSalvar: () -> Unit
) {
    var modelo by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = modelo,
                    onValueChange = { novo -> modelo = novo },
                    label = { Text("Modelo") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = placa,
                    onValueChange = { novaPlaca -> placa = novaPlaca },
                    label = { Text("placa") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = km,
                    onValueChange = { novoKm -> km = novoKm },
                    label = { Text("kilometragem") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ano,
                    onValueChange = { novoAno -> ano = novoAno },
                    label = { Text("ano") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (erro != null) {
                    Text("${erro}", color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = {
                        val newAno: Int? = ano.toIntOrNull()
                        val newKm: Int? = km.toIntOrNull()

                        if (newAno == null || newKm == null || modelo.isBlank() || placa.isBlank()) {
                            erro = "Campos não podem ser vazios!"
                            return@Button
                        }

                        val novaMoto: Moto = Moto(
                            modelo = modelo,
                            anoFabricacao = newAno,
                            placa = placa,
                            kilometragem = newKm
                        )
                        viewModel.inserirMoto(novaMoto)

                        onSalvar()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Salvar moto")
                }

            }
}
