package com.development.motorlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CadastroScreen(
    viewModel: MotoViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onSalvar: () -> Unit
) {
    // --- ESTADO DO FORMULÁRIO: um pra cada campo (tudo String enquanto
    var modelo by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }
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
                    onValueChange = { novaPlaca -> placa = novaPlaca},
                    label = { Text("placa") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = km,
                    onValueChange = { novoKm -> km = novoKm},
                    label = {Text("kilometragem")},
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ano,
                    onValueChange = { novoAno -> ano = novoAno},
                    label = { Text("ano")},
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val newAno : Int? = ano.toIntOrNull()
                        val newKm : Int? = km.toIntOrNull()

                        if (newAno == null || newKm == null || modelo.isBlank() || placa.isBlank()){
                            print("Campos não podem ser null")
                            return@Button
                        }

                        val novaMoto: Moto = Moto(
                            modelo = modelo,
                                        anoFabricacao = newAno,
                                        placa = placa,
                                        kilometragem = newKm)
                        viewModel.inserirMoto(novaMoto)

                        onSalvar()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Salvar moto")
                }
            }
}
