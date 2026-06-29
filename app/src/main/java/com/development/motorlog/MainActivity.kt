package com.development.motorlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.ui.screens.AtualizarKmScreen
import com.development.motorlog.ui.screens.CadastroScreen
import com.development.motorlog.ui.screens.FormPecaScreen
import com.development.motorlog.ui.screens.FormServicoScreen
import com.development.motorlog.ui.screens.GaragemScreen
import com.development.motorlog.ui.screens.GerenciarPecasScreen
import com.development.motorlog.ui.screens.HistoricoScreen
import com.development.motorlog.ui.screens.PainelScreen
import com.development.motorlog.ui.screens.RegistroScreen
import com.development.motorlog.ui.theme.MotorLogTheme
import com.development.motorlog.ui.viewModels.MotoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotorLogTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    var telaAtual by rememberSaveable { mutableStateOf("Garagem") }
                    var motoSelecionada by remember { mutableStateOf<Moto?>(null) }
                    var pecaSelecionada by remember { mutableStateOf<Peca?>(null)}
                    val motoViewModel: MotoViewModel = viewModel()

                    BackHandler(enabled = telaAtual != "Garagem") {
                        telaAtual = when (telaAtual) {
                            "Registro" -> "Painel"
                            "AtualizarKm" -> "Painel"
                            "EditarPeca" -> "GerenciarPecas"
                            "RegistrarServico" -> "Painel"
                            "Historico" -> "Painel"
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
                                    telaAtual = "Painel"
                                },
                                onEditarPeca = {
                                    telaAtual = "GerenciarPecas"
                                })
                        }
                        "Cadastro" -> {
                            CadastroScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSalvar = { telaAtual = "Garagem" })
                        }
                        "Painel" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null){
                                PainelScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    moto = motoSel,
                                    onAtualizarKm = { telaAtual = "AtualizarKm" },
                                    onRegistrarTroca = { telaAtual = "Registro" },
                                    onRegistrarServico = { telaAtual = "RegistrarServico" },
                                    onVerHistorico = { telaAtual = "Historico" },
                                    onExcluirMoto = {
                                        motoViewModel.deletarMoto(motoSel)
                                        telaAtual = "Garagem"
                                    }
                                )
                            }
                        }
                        "AtualizarKm" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null){
                                AtualizarKmScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    moto = motoSel,
                                    onSalvar = { motoNova ->
                                        motoSelecionada = motoNova
                                        telaAtual = "Painel"
                                    }
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
                        "RegistrarServico" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null){
                                FormServicoScreen(
                                    moto = motoSel,
                                    modifier = Modifier.padding(innerPadding),
                                    onSalvar = { telaAtual = "Painel" }
                                )
                            }
                        }
                        "Historico" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null){
                                HistoricoScreen(
                                    moto = motoSel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                        "GerenciarPecas" -> {
                            GerenciarPecasScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSalvarPeca = {
                                    pecaSelecionada = null
                                    telaAtual = "EditarPeca"},
                                onEditarPeca = { peca ->
                                    pecaSelecionada = peca
                                    telaAtual = "EditarPeca"}
                            )
                        }
                        "EditarPeca" -> {
                            FormPecaScreen(
                                peca = pecaSelecionada,
                                modifier = Modifier.padding(innerPadding),
                                onSalvar = {
                                    pecaSelecionada = null
                                    telaAtual = "GerenciarPecas"
                                    }
                            )
                        }
                    }

                }
            }
        }
    }
}