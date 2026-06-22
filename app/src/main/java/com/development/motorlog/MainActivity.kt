package com.development.motorlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.tooling.preview.Preview
import com.development.motorlog.ui.theme.MotorLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotorLogTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    var telaAtual by remember { mutableStateOf("Garagem") }
                    var motoSelecionada by remember { mutableStateOf<Moto?>(null) }

                    when(telaAtual) {
                        "Garagem" -> { GaragemScreen(
                            modifier = Modifier.padding(innerPadding),
                            onAdicionar = {telaAtual = "Cadastro"},
                            onEditarMoto = {moto ->
                                motoSelecionada = moto
                                telaAtual = "Editar"})}
                        "Cadastro" -> {CadastroScreen(
                            modifier = Modifier.padding(innerPadding),
                            onSalvar = {telaAtual = "Garagem"})}
                        "Editar" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null){
                                EditarKmScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onSalvar = {telaAtual = "Garagem"},
                                    moto = motoSel
                                )}
                            }

                    }

                }
            }
        }
    }
}