package com.development.motorlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Servico
import com.development.motorlog.ui.screens.AtualizarKmScreen
import com.development.motorlog.ui.screens.CadastroScreen
import com.development.motorlog.ui.screens.FormPecaScreen
import com.development.motorlog.ui.screens.FormServicoScreen
import com.development.motorlog.ui.screens.GaragemScreen
import com.development.motorlog.ui.screens.GerenciarPecasScreen
import com.development.motorlog.ui.screens.HistoricoScreen
import com.development.motorlog.ui.screens.PainelScreen
import com.development.motorlog.ui.screens.RegistroScreen
import com.development.motorlog.ui.screens.RevisaoDetailScreen
import com.development.motorlog.ui.theme.MotorLogTheme
import com.development.motorlog.ui.viewModels.MotoViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotorLogTheme {
                var telaAtual by rememberSaveable { mutableStateOf("Garagem") }
                var motoSelecionada by remember { mutableStateOf<Moto?>(null) }
                var pecaSelecionada by remember { mutableStateOf<Peca?>(null)}
                var servicoSelecionado by remember { mutableStateOf<Servico?>(null) }
                // de qual tela o usuário abriu o detalhe/edição (pra voltar pro lugar certo)
                var origemDetalhe by rememberSaveable { mutableStateOf("Garagem") }
                val motoViewModel: MotoViewModel = viewModel()

                val irParaTras: () -> Unit = {
                    telaAtual = when (telaAtual) {
                        "Registro" -> "Painel"
                        "AtualizarKm" -> "Painel"
                        "EditarPeca" -> origemDetalhe
                        "RegistrarServico" -> "Painel"
                        "Historico" -> "Painel"
                        "RevisaoDetail" -> origemDetalhe
                        else -> "Garagem"
                    }
                }

                val titulo = when (telaAtual) {
                    "Cadastro" -> "Nova moto"
                    "Painel" -> motoSelecionada?.modelo ?: "Painel"
                    "AtualizarKm" -> "Atualizar km"
                    "Registro" -> "Registrar troca"
                    "RegistrarServico" -> "Registrar serviço"
                    "Historico" -> "Histórico"
                    "RevisaoDetail" -> servicoSelecionado?.tipoServico ?: "Serviço"
                    "GerenciarPecas" -> "Peças"
                    "EditarPeca" -> if (pecaSelecionada != null) "Editar peça" else "Nova peça"
                    else -> "Garagem"
                }

                BackHandler(enabled = telaAtual != "Garagem") { irParaTras() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text(titulo) },
                            navigationIcon = {
                                if (telaAtual != "Garagem") {
                                    IconButton(onClick = irParaTras) {
                                        Text("←", fontSize = 24.sp)
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                            ),
                        )
                    },
                ) { innerPadding ->
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
                                    },
                                    onEditarPeca = { peca ->
                                        pecaSelecionada = peca
                                        origemDetalhe = "Painel"
                                        telaAtual = "EditarPeca"
                                    },
                                    onAbrirServico = { servico ->
                                        servicoSelecionado = servico
                                        origemDetalhe = "Painel"
                                        telaAtual = "RevisaoDetail"
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
                                    modifier = Modifier.padding(innerPadding),
                                    onAbrirServico = { servico ->
                                        servicoSelecionado = servico
                                        origemDetalhe = "Historico"
                                        telaAtual = "RevisaoDetail"
                                    }
                                )
                            }
                        }
                        "RevisaoDetail" -> {
                            val servicoSel = servicoSelecionado
                            if (servicoSel != null){
                                RevisaoDetailScreen(
                                    servico = servicoSel,
                                    modifier = Modifier.padding(innerPadding),
                                    onExcluido = {
                                        servicoSelecionado = null
                                        telaAtual = origemDetalhe
                                    }
                                )
                            }
                        }
                        "GerenciarPecas" -> {
                            GerenciarPecasScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSalvarPeca = {
                                    pecaSelecionada = null
                                    origemDetalhe = "GerenciarPecas"
                                    telaAtual = "EditarPeca"},
                                onEditarPeca = { peca ->
                                    pecaSelecionada = peca
                                    origemDetalhe = "GerenciarPecas"
                                    telaAtual = "EditarPeca"}
                            )
                        }
                        "EditarPeca" -> {
                            FormPecaScreen(
                                peca = pecaSelecionada,
                                modifier = Modifier.padding(innerPadding),
                                onSalvar = {
                                    pecaSelecionada = null
                                    telaAtual = origemDetalhe
                                    }
                            )
                        }
                    }

                }
            }
        }
    }
}