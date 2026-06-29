package com.development.motorlog.ui.viewModels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import kotlinx.coroutines.launch
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.Recomendacao
import com.development.motorlog.domain.calcularRecomendacoes

class RegistroViewModel(application: Application): AndroidViewModel(application) {
    private val registroDao = AppDatabase.getDatabase(application).registroDao()
    private val pecaDao = AppDatabase.getDatabase(application).pecaDao()
    private val servicoDao = AppDatabase.getDatabase(application).servicoDao()

    var pecas by mutableStateOf<List<Peca>>(emptyList())
        private set

    var recomendacoes by mutableStateOf<List<Recomendacao>>(emptyList())
        private set

    var servicos by mutableStateOf<List<Servico>>(emptyList())
        private set

    var registrosDoServico by mutableStateOf<List<Registro>>(emptyList())
        private set

    init {
        carregarPecas()
    }

    fun carregarRecomendacoes(moto: Moto) {
        viewModelScope.launch {
            val pecasAtuais = pecaDao.listarPecas()
            val registros = registroDao.listarRegistros(moto.id)

            recomendacoes = calcularRecomendacoes(
                kmAtual = moto.kilometragem,
                pecas = pecasAtuais,
                registros = registros
            )
        }
    }


    private fun carregarPecas() {
        viewModelScope.launch {
            if (pecaDao.listarPecas().isEmpty()){
                val defaults = listOf(
                    // --- Lubrificação e Filtros ---
                    Peca(nome = "Óleo do motor", intervaloKm = 3000),
                    Peca(nome = "Filtro de óleo", intervaloKm = 9000),
                    Peca(nome = "O-ring da tampa do filtro de óleo", intervaloKm = 9000),
                    Peca(nome = "Filtro de ar", intervaloKm = 10000),
                    Peca(nome = "Filtro de combustível", intervaloKm = 10000),
                    Peca(nome = "Tela filtrante de óleo (Limpeza interna)", intervaloKm = 20000),
                    Peca(nome = "Filtro centrífugo de óleo (Limpeza interna)", intervaloKm = 20000),

                    // --- Motor (Distribuição e Cabeçote) ---
                    Peca(nome = "Vela de ignição", intervaloKm = 10000),
                    Peca(nome = "Limpeza do corpo de injeção (TBI)", intervaloKm = 15000),
                    Peca(nome = "Regulagem de válvulas", intervaloKm = 15000),
                    Peca(nome = "Guarnição da tampa de válvulas", intervaloKm = 15000),
                    Peca(nome = "Acionador/Tensor da corrente de comando", intervaloKm = 30000),
                    Peca(nome = "Corrente de comando", intervaloKm = 50000),
                    Peca(nome = "Guias/Sapatas da corrente de comando", intervaloKm = 50000),
                    Peca(nome = "Retentores de válvula", intervaloKm = 50000),
                    Peca(nome = "Válvulas de admissão e escape", intervaloKm = 60000),

                    // --- Motor (Força, Compressão e Parte Baixa) ---
                    Peca(nome = "Bomba de óleo interna", intervaloKm = 60000),
                    Peca(nome = "Anéis de segmento (Pistão)", intervaloKm = 60000),
                    Peca(nome = "Kit Pistão e Cilindro", intervaloKm = 90000),
                    Peca(nome = "Kit Biela (Biela, Pino e Gaiola)", intervaloKm = 100000),
                    Peca(nome = "Rolamentos do virabrequim", intervaloKm = 100000),
                    Peca(nome = "Rolamentos do eixo de transmissão/câmbio", intervaloKm = 100000),

                    // --- Sistema de Partida Elétrica ---
                    Peca(nome = "Escovas do motor de partida", intervaloKm = 30000),
                    Peca(nome = "Anéis de vedação do motor de partida", intervaloKm = 30000),
                    Peca(nome = "Relé de partida", intervaloKm = 40000),
                    Peca(nome = "Bucha/Rolamento do motor de partida", intervaloKm = 40000),
                    Peca(nome = "Placa de partida (Embreagem unidirecional)", intervaloKm = 50000),

                    // --- Transmissão e Embreagem ---
                    Peca(nome = "Kit Relação (Coroa, Corrente e Pinhão)", intervaloKm = 20000),
                    Peca(nome = "Guia da corrente (Saboneteira)", intervaloKm = 20000),
                    Peca(nome = "Coxim da coroa (Borrachas do cubo)", intervaloKm = 20000),
                    Peca(nome = "Discos e Separadores de embreagem", intervaloKm = 40000),
                    Peca(nome = "Molas da embreagem", intervaloKm = 40000),

                    // --- Sistema de Freios ---
                    Peca(nome = "Pastilha de freio dianteira", intervaloKm = 10000),
                    Peca(nome = "Pastilha/Lona de freio traseira", intervaloKm = 10000),
                    Peca(nome = "Lubrificação dos pinos da pinça", intervaloKm = 10000),
                    Peca(nome = "Fluido de freio", intervaloKm = 20000),
                    Peca(nome = "Reparo do cilindro mestre (Burrinho)", intervaloKm = 40000),

                    // --- Rodas e Pneus ---
                    Peca(nome = "Raios (Reaperto e Alinhamento)", intervaloKm = 10000),
                    Peca(nome = "Pneu traseiro", intervaloKm = 12000),
                    Peca(nome = "Pneu dianteiro", intervaloKm = 20000),
                    Peca(nome = "Câmara de ar (se aplicável)", intervaloKm = 20000),
                    Peca(nome = "Rolamentos das rodas (Dianteira e Traseira)", intervaloKm = 20000),

                    // --- Cabos e Comandos ---
                    Peca(nome = "Cabo de embreagem", intervaloKm = 15000),
                    Peca(nome = "Cabo do acelerador (Cabos A e B)", intervaloKm = 15000),

                    // --- Suspensão e Direção ---
                    Peca(nome = "Lubrificação do Link da suspensão traseira", intervaloKm = 15000),
                    Peca(nome = "Óleo da suspensão dianteira (Bengalas)", intervaloKm = 20000),
                    Peca(nome = "Retentor e Guarda-pó da suspensão", intervaloKm = 20000),
                    Peca(nome = "Rolamento da caixa de direção", intervaloKm = 20000),
                    Peca(nome = "Sanfonas da bengala", intervaloKm = 30000),
                    Peca(nome = "Bucha da balança traseira (Quadro elástico)", intervaloKm = 30000),

                    // --- Sistema Elétrico Geral ---
                    Peca(nome = "Bateria", intervaloKm = 30000)
                )
                defaults.forEach { peca -> pecaDao.inserir(peca) }
            }
            pecas = pecaDao.listarPecas()
        }
    }

    fun inserirRegistro(registro: Registro){
        viewModelScope.launch {
            registroDao.inserirRegistro(registro = registro)
        }
    }

    fun inserirServico(servico: Servico){
        viewModelScope.launch {
            servicoDao.inserir(servico = servico)
        }
    }

    fun carregarServicos(moto: Moto){
        viewModelScope.launch {
            servicos = servicoDao.query(moto.id)
        }
    }

    fun carregarRegistrosDoServico(servicoId: Long){
        viewModelScope.launch {
            registrosDoServico = registroDao.listarPorServico(servicoId)
        }
    }

    // insere o serviço e, com o id gerado, grava uma troca (Registro) por peça trocada
    fun inserirServicoComPecas(servico: Servico, pecasComPreco: Map<Long, Int>){
        viewModelScope.launch {
            val servicoId = servicoDao.inserir(servico)
            pecasComPreco.forEach { (pecaId, preco) ->
                registroDao.inserirRegistro(
                    Registro(
                        motoId = servico.motoId,
                        pecaId = pecaId,
                        kmTroca = servico.kilometragem,
                        servicoId = servicoId,
                        preco = preco
                    )
                )
            }
        }
    }

    fun inserirPeca(peca: Peca){
        viewModelScope.launch {
            pecaDao.inserir(peca = peca)
            pecas = pecaDao.listarPecas()
        }
    }

    fun atualizarPeca(peca: Peca){
        viewModelScope.launch {
            pecaDao.atualizar(peca = peca)
            pecas = pecaDao.listarPecas()
        }
    }

    fun deletarPeca(peca: Peca){
        viewModelScope.launch {
            pecaDao.deletar(peca = peca)
            pecas = pecaDao.listarPecas()
        }
    }

    fun deletarServico(servico: Servico){
        viewModelScope.launch {
            servicoDao.remover(servico = servico)
            servicos = servicoDao.query(servico.motoId)
        }
    }
}
