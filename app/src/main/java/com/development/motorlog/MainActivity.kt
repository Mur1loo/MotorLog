package com.development.motorlog

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.lembrete.LembreteWorker
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
import com.development.motorlog.ui.screens.TrocasScreen
import com.development.motorlog.ui.theme.MotorLogTheme
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.viewModels.MotoViewModel
import com.development.motorlog.ui.viewModels.RegistroViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // o app é dark fixo: ícones da status/navigation bar sempre claros, independente do modo do sistema
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        LembreteWorker.agendar(this)
        if (intent.getBooleanExtra(LembreteWorker.EXTRA_RODAR_AGORA, false)) LembreteWorker.rodarAgora(this)
        // vindo da notificação: abre direto o Painel daquela moto
        val motoDaNotificacao = intent.getLongExtra(LembreteWorker.EXTRA_MOTO_ID, -1L).takeIf { it > 0 }
        setContent {
            MotorLogTheme {
                // Navegação por estado. Tudo aqui é rememberSaveable (sobrevive ao giro e à morte
                // do processo): os "passageiros" são só ids (Long, Bundle-friendly); o objeto é
                // resolvido nas listas dos ViewModels, que já sobrevivem ao config change.
                var telaAtual by rememberSaveable { mutableStateOf(if (motoDaNotificacao != null) "Painel" else "Garagem") }
                var motoId by rememberSaveable { mutableStateOf(motoDaNotificacao) }
                var pecaId by rememberSaveable { mutableStateOf<Long?>(null) }   // null = peça nova
                var servicoId by rememberSaveable { mutableStateOf<Long?>(null) }
                // de qual tela o usuário abriu o detalhe/edição (pra voltar pro lugar certo)
                var origemDetalhe by rememberSaveable { mutableStateOf("Garagem") }

                val motoViewModel: MotoViewModel = viewModel()
                val registroViewModel: RegistroViewModel = viewModel()
                val motoSelecionada = motoId?.let { id -> motoViewModel.motos.find { it.id == id } }
                val pecaSelecionada = pecaId?.let { id -> registroViewModel.pecas.find { it.id == id } }
                val servicoSelecionado = servicoId?.let { id -> registroViewModel.servicos.find { it.id == id } }

                val irParaTras: () -> Unit = {
                    telaAtual = when (telaAtual) {
                        "Registro" -> "Painel"
                        "AtualizarKm" -> "Painel"
                        "EditarPeca" -> origemDetalhe
                        "RegistrarServico" -> "Painel"
                        "Historico" -> "Painel"
                        "Trocas" -> "Painel"
                        "EditarMoto" -> "Painel"
                        "EditarServico" -> "RevisaoDetail"
                        "RevisaoDetail" -> origemDetalhe
                        else -> "Garagem"
                    }
                }

                val titulo = when (telaAtual) {
                    "Cadastro" -> "Nova moto"
                    "EditarMoto" -> "Editar moto"
                    "EditarServico" -> "Editar serviço"
                    "Painel" -> motoSelecionada?.modelo ?: "Painel"
                    "AtualizarKm" -> "Atualizar km"
                    "Registro" -> "Troquei uma peça"
                    "RegistrarServico" -> "Fui à oficina"
                    "Historico" -> "Histórico"
                    "Trocas" -> "Quando troca cada peça"
                    "RevisaoDetail" -> servicoSelecionado?.tipoServico ?: "Serviço"
                    "GerenciarPecas" -> "Peças e intervalos"
                    "EditarPeca" -> if (pecaId != null) "Editar peça" else "Nova peça"
                    else -> "Garagem"
                }

                BackHandler(enabled = telaAtual != "Garagem") { irParaTras() }

                // feedback imediato depois de salvar (pilar de UX: nunca silêncio)
                val snackbar = remember { SnackbarHostState() }
                var mensagem by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(mensagem) {
                    mensagem?.let { snackbar.showSnackbar(it); mensagem = null }
                }

                // Android 13+: notificação exige permissão em runtime. Pede uma vez, ao abrir.
                val pedirPermissao = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !LembreteWorker.podeNotificar(this@MainActivity)) {
                        pedirPermissao.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbar) },
                    topBar = {
                        TopAppBar(
                            title = { Text(titulo) },
                            navigationIcon = {
                                if (telaAtual != "Garagem") {
                                    IconButton(onClick = irParaTras) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
                                    motoId = moto.id
                                    telaAtual = "Painel"
                                },
                                onEditarPeca = {
                                    telaAtual = "GerenciarPecas"
                                })
                        }
                        "Cadastro" -> {
                            CadastroScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSalvar = { mensagem = "Moto cadastrada. Toque nela pra ver o painel."; telaAtual = "Garagem" })
                        }
                        "EditarMoto" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null) {
                                CadastroScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    moto = motoSel,
                                    onSalvar = { mensagem = "Dados da moto salvos."; telaAtual = "Painel" })
                            }
                        }
                        "EditarServico" -> {
                            val motoSel = motoSelecionada
                            val servicoSel = servicoSelecionado
                            if (motoSel != null && servicoSel != null) {
                                FormServicoScreen(
                                    moto = motoSel,
                                    servico = servicoSel,
                                    modifier = Modifier.padding(innerPadding),
                                    onSalvar = { mensagem = "Serviço atualizado."; telaAtual = "RevisaoDetail" }
                                )
                            }
                        }
                        "Painel" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null){
                                PainelScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    moto = motoSel,
                                    ritmoKmMes = motoViewModel.ritmos[motoSel.id],
                                    onAtualizarKm = { telaAtual = "AtualizarKm" },
                                    onRegistrarTroca = { telaAtual = "Registro" },
                                    onRegistrarServico = { telaAtual = "RegistrarServico" },
                                    onVerHistorico = { telaAtual = "Historico" },
                                    onVerTrocas = { telaAtual = "Trocas" },
                                    onEditarMoto = { telaAtual = "EditarMoto" },
                                    onExcluirMoto = {
                                        motoViewModel.deletarMoto(motoSel)
                                        motoId = null
                                        telaAtual = "Garagem"
                                    },
                                    onEditarPeca = { peca ->
                                        pecaId = peca.id
                                        origemDetalhe = "Painel"
                                        telaAtual = "EditarPeca"
                                    },
                                    onAbrirServico = { servico ->
                                        servicoId = servico.id
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
                                    // a lista do MotoViewModel recarrega sozinha; o Painel lê dela
                                    onSalvar = { km -> mensagem = "Km atualizado: ${formatarKm(km)}"; telaAtual = "Painel" }
                                )
                            }
                        }
                        "Registro" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null){
                                RegistroScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    moto = motoSel,
                                    onSalvar = { mensagem = "Troca registrada. Já recalculei a próxima."; telaAtual = "Painel" }
                                )
                            }
                        }
                        "RegistrarServico" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null){
                                FormServicoScreen(
                                    moto = motoSel,
                                    modifier = Modifier.padding(innerPadding),
                                    onSalvar = { mensagem = "Serviço salvo no histórico."; telaAtual = "Painel" }
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
                                        servicoId = servico.id
                                        origemDetalhe = "Historico"
                                        telaAtual = "RevisaoDetail"
                                    }
                                )
                            }
                        }
                        "Trocas" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null){
                                TrocasScreen(
                                    moto = motoSel,
                                    ritmoKmMes = motoViewModel.ritmos[motoSel.id],
                                    modifier = Modifier.padding(innerPadding),
                                    onEditarPeca = { peca ->
                                        pecaId = peca.id
                                        origemDetalhe = "Trocas"
                                        telaAtual = "EditarPeca"
                                    }
                                )
                            }
                        }
                        "RevisaoDetail" -> {
                            val servicoSel = servicoSelecionado
                            val motoSel = motoSelecionada
                            if (servicoSel != null){
                                RevisaoDetailScreen(
                                    servico = servicoSel,
                                    modifier = Modifier.padding(innerPadding),
                                    onEditar = { telaAtual = "EditarServico" },
                                    onExcluido = {
                                        servicoId = null
                                        telaAtual = origemDetalhe
                                    }
                                )
                            } else if (motoSel != null) {
                                // voltou da morte do processo direto no detalhe: a lista ainda não carregou
                                LaunchedEffect(motoSel) { registroViewModel.carregarServicos(motoSel) }
                            }
                        }
                        "GerenciarPecas" -> {
                            GerenciarPecasScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSalvarPeca = {
                                    pecaId = null
                                    origemDetalhe = "GerenciarPecas"
                                    telaAtual = "EditarPeca"},
                                onEditarPeca = { peca ->
                                    pecaId = peca.id
                                    origemDetalhe = "GerenciarPecas"
                                    telaAtual = "EditarPeca"}
                            )
                        }
                        "EditarPeca" -> {
                            // modo edição só renderiza quando a peça já foi resolvida (evita abrir em branco)
                            if (pecaId == null || pecaSelecionada != null) {
                                FormPecaScreen(
                                    peca = pecaSelecionada,
                                    modifier = Modifier.padding(innerPadding),
                                    onSalvar = {
                                        mensagem = "Peça salva."
                                        pecaId = null
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
}
