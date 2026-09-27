package com.development.motorlog

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.lembrete.LembreteWorker
import com.development.motorlog.ui.components.AbaMoto
import com.development.motorlog.ui.components.BarraInferior
import com.development.motorlog.ui.components.BotaoDeCabecalho
import com.development.motorlog.ui.components.MlTopBar
import com.development.motorlog.ui.screens.AtualizarKmSheet
import com.development.motorlog.ui.screens.CadastroScreen
import com.development.motorlog.ui.screens.FormPecaScreen
import com.development.motorlog.ui.screens.FormServicoScreen
import com.development.motorlog.ui.screens.FotosScreen
import com.development.motorlog.ui.screens.GaragemScreen
import com.development.motorlog.ui.screens.GerenciarPecasScreen
import com.development.motorlog.ui.screens.HistoricoScreen
import com.development.motorlog.ui.screens.PainelScreen
import com.development.motorlog.ui.screens.RegistroScreen
import com.development.motorlog.ui.screens.RevisaoDetailScreen
import com.development.motorlog.ui.screens.TrocasScreen
import com.development.motorlog.ui.theme.MotorLogTheme
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.viewModels.MotoViewModel
import com.development.motorlog.ui.viewModels.RegistroViewModel

// telas que vivem "dentro de uma moto" e mostram a barra inferior com o FAB +KM
private val TELAS_DA_MOTO = setOf("Painel", "Historico", "Trocas", "Fotos")
// extra do atalho da tela inicial (res/xml/shortcuts.xml) e chave da última moto aberta
const val EXTRA_ABRIR_KM = "abrirKm"
private const val PREF_ULTIMA_MOTO = "ultima_moto"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // o app é dark fixo: ícones da status/navigation bar sempre claros, independente do modo do sistema
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        LembreteWorker.agendar(this)
        // só na criação de verdade (não em cada rotação)
        val primeiraCriacao = savedInstanceState == null
        if (primeiraCriacao && intent.getBooleanExtra(LembreteWorker.EXTRA_RODAR_AGORA, false)) LembreteWorker.rodarAgora(this)
        // vindo da notificação: abre direto o Painel daquela moto
        val motoDaNotificacao = intent.getLongExtra(LembreteWorker.EXTRA_MOTO_ID, -1L).takeIf { it > 0 }
        // vindo do atalho "+KM" da tela inicial: abre a folha de km da última moto usada
        val prefs = getSharedPreferences("motorlog", MODE_PRIVATE)
        val abrirKm = primeiraCriacao && intent.getBooleanExtra(EXTRA_ABRIR_KM, false)
        val ultimaMoto = prefs.getLong(PREF_ULTIMA_MOTO, -1L).takeIf { it > 0 }
        val motoInicial = motoDaNotificacao ?: if (abrirKm) ultimaMoto else null
        setContent {
            MotorLogTheme {
                // Navegação por estado. Tudo aqui é rememberSaveable (sobrevive ao giro e à morte
                // do processo): os "passageiros" são só ids (Long, Bundle-friendly); o objeto é
                // resolvido nas listas dos ViewModels, que já sobrevivem ao config change.
                var telaAtual by rememberSaveable { mutableStateOf(if (motoInicial != null) "Painel" else "Garagem") }
                var motoId by rememberSaveable { mutableStateOf(motoInicial) }
                var pecaId by rememberSaveable { mutableStateOf<Long?>(null) }   // null = peça nova
                var servicoId by rememberSaveable { mutableStateOf<Long?>(null) }
                // de qual tela o usuário abriu o detalhe/edição (pra voltar pro lugar certo)
                var origemDetalhe by rememberSaveable { mutableStateOf("Garagem") }
                // a ação nº 1 é uma folha inferior sobre a tela atual, não uma tela
                var mostrarKm by rememberSaveable { mutableStateOf(abrirKm) }

                val motoViewModel: MotoViewModel = viewModel()
                val registroViewModel: RegistroViewModel = viewModel()
                val motoSelecionada = motoId?.let { id -> motoViewModel.motos.find { it.id == id } }
                val pecaSelecionada = pecaId?.let { id -> registroViewModel.pecas.find { it.id == id } }
                val servicoSelecionado = servicoId?.let { id -> registroViewModel.servicos.find { it.id == id } }

                val irParaTras: () -> Unit = {
                    telaAtual = when (telaAtual) {
                        "Registro" -> "Painel"
                        "EditarPeca" -> origemDetalhe
                        "RegistrarServico" -> "Painel"
                        "Historico" -> "Painel"
                        "Trocas" -> "Painel"
                        "EditarMoto" -> "Painel"
                        "EditarServico" -> "RevisaoDetail"
                        "RevisaoDetail" -> origemDetalhe
                        "Fotos" -> "Painel"
                        else -> "Garagem"
                    }
                }

                val titulo = when (telaAtual) {
                    "Cadastro" -> "Nova moto"
                    "EditarMoto" -> "Editar moto"
                    "EditarServico" -> "Editar serviço"
                    "Painel" -> motoSelecionada?.modelo ?: "Painel"
                    "Registro" -> "Troquei uma peça"
                    "RegistrarServico" -> "Fui à oficina"
                    "Historico" -> "Histórico"
                    "Trocas" -> "Quando troca cada peça"
                    "RevisaoDetail" -> servicoSelecionado?.tipoServico ?: "Serviço"
                    "GerenciarPecas" -> "Peças e intervalos"
                    "EditarPeca" -> if (pecaId != null) "Editar peça" else "Nova peça"
                    "Fotos" -> "Álbum da moto"
                    else -> "Garagem"
                }
                val subtitulo = when (telaAtual) {
                    "Painel" -> motoSelecionada?.let { "${it.anoFabricacao} · ${it.placa}" }
                    "Trocas" -> motoSelecionada?.let { "${it.modelo} · ${formatarKm(it.kilometragem)}" }
                    "Historico", "RevisaoDetail", "Registro", "RegistrarServico", "EditarServico", "Fotos" -> motoSelecionada?.modelo
                    "Cadastro" -> "Cadastre sua motocicleta"
                    else -> null
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
                    if (primeiraCriacao && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !LembreteWorker.podeNotificar(this@MainActivity)) {
                        pedirPermissao.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                // moto que não existe mais (ex.: notificação antiga de moto excluída): volta pra Garagem.
                // Atalho sem moto conhecida: usa a única/primeira moto. Lembra a última moto aberta.
                LaunchedEffect(motoId, motoViewModel.motos) {
                    val motos = motoViewModel.motos
                    if (motoId != null && motos.isNotEmpty() && motoSelecionada == null) {
                        motoId = null
                        telaAtual = "Garagem"
                    }
                    if (motoId == null && mostrarKm && motos.isNotEmpty()) {
                        motoId = motos.first().id
                        telaAtual = "Painel"
                    }
                    if (motoId == null && mostrarKm && motoViewModel.carregou && motos.isEmpty()) mostrarKm = false
                    motoId?.let { id -> prefs.edit { putLong(PREF_ULTIMA_MOTO, id) } }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbar) },
                    topBar = {
                        MlTopBar(
                            titulo = titulo,
                            subtitulo = subtitulo,
                            grande = telaAtual == "Garagem",
                            onVoltar = if (telaAtual != "Garagem") irParaTras else null,
                            acoes = when {
                                // catálogo de peças e intervalos: chave + texto. A engrenagem antiga
                                // (círculo com raios) era confundida com o botão de tema claro/escuro.
                                telaAtual == "Garagem" -> {
                                    { BotaoDeCabecalho("Peças", R.drawable.ic_ml_wrench, "Peças e intervalos") { telaAtual = "GerenciarPecas" } }
                                }
                                // a Garagem saiu da barra inferior (entrou Fotos): volta por aqui, 1 toque
                                telaAtual in TELAS_DA_MOTO -> {
                                    {
                                        IconButton(onClick = { telaAtual = "Garagem" }) {
                                            Icon(painterResource(R.drawable.ic_ml_moto), contentDescription = "Garagem", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                else -> null
                            },
                        )
                    },
                    bottomBar = {
                        if (telaAtual in TELAS_DA_MOTO && motoSelecionada != null) {
                            BarraInferior(
                                ativa = when (telaAtual) {
                                    "Historico" -> AbaMoto.HISTORICO
                                    "Trocas" -> AbaMoto.TROCAS
                                    "Fotos" -> AbaMoto.FOTOS
                                    else -> AbaMoto.PAINEL
                                },
                                aoNavegar = { aba ->
                                    telaAtual = when (aba) {
                                        AbaMoto.PAINEL -> "Painel"
                                        AbaMoto.HISTORICO -> "Historico"
                                        AbaMoto.TROCAS -> "Trocas"
                                        AbaMoto.FOTOS -> "Fotos"
                                    }
                                },
                                aoAtualizarKm = { mostrarKm = true },
                            )
                        }
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
                                onMensagem = { mensagem = it })
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
                                    kmRodados = motoViewModel.kmRodados[motoSel.id] ?: 0,
                                    onAtualizarKm = { mostrarKm = true },
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
                                    },
                                    onAbrirFotos = { telaAtual = "Fotos" },
                                    onMensagem = { mensagem = it },
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
                                    },
                                    onMensagem = { mensagem = it },
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
                                    },
                                    onRegistrarServico = { telaAtual = "RegistrarServico" },
                                )
                            }
                        }
                        "RevisaoDetail" -> {
                            val servicoSel = servicoSelecionado
                            val motoSel = motoSelecionada
                            if (servicoSel != null){
                                RevisaoDetailScreen(
                                    servico = servicoSel,
                                    accent = motoSel?.let(::accentDaMoto) ?: MaterialTheme.colorScheme.primary,
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
                        "Fotos" -> {
                            val motoSel = motoSelecionada
                            if (motoSel != null) {
                                FotosScreen(
                                    moto = motoSel,
                                    modifier = Modifier.padding(innerPadding),
                                    onDefinirCapa = { foto -> motoViewModel.atualizarMoto(motoSel.copy(fotoCapaId = foto.id)) },
                                    onMensagem = { mensagem = it },
                                )
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

                // folha "Atualizar km" por cima de qualquer tela da moto
                val motoKm = motoSelecionada
                if (mostrarKm && motoKm != null) {
                    AtualizarKmSheet(
                        moto = motoKm,
                        onFechar = { mostrarKm = false },
                        onSalvo = { km ->
                            mostrarKm = false
                            mensagem = "Km atualizado: ${formatarKm(km)}"
                            if (telaAtual !in TELAS_DA_MOTO) telaAtual = "Painel"
                        },
                    )
                }
            }
        }
    }
}
