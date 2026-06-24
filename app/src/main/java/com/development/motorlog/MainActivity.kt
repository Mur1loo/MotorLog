package com.development.motorlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.development.motorlog.data.Moto
import com.development.motorlog.ui.CadastroScreen
import com.development.motorlog.ui.EditarKmScreen
import com.development.motorlog.ui.GaragemScreen
import com.development.motorlog.ui.RegistroScreen
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

                    BackHandler(enabled = telaAtual != "Garagem") {
                        telaAtual = when (telaAtual) {
                            "Registro" -> "Editar"
                            else -> "Garagem"
                        }
                    }

                    when(telaAtual) {
                        "Garagem" -> {
                            GaragemScreen(
                                modifier = Modifier.padding(innerPadding),
                                onAdicionar = { telaAtual = "Cadastro" },
                                onEditarMoto = { moto ->
                                    motoSelecionada = moto
                                    telaAtual = "Editar"
                                })
                        }
                        "Cadastro" -> {
                            CadastroScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSalvar = { telaAtual = "Garagem" })
                        }
                        "Editar" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null){
                                EditarKmScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onSalvar = { telaAtual = "Garagem" },
                                    moto = motoSel,
                                    onRegistrarTroca = { telaAtual = "Registro" }
                                )
                            }
                        }
                        "Registro" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null){
                                RegistroScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    moto = motoSel,
                                    onSalvar = { telaAtual = "Garagem" }
                                )
                            }
                        }

                    }

                }
            }
        }
    }
}