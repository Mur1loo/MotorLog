package com.development.motorlog.ui.viewModels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.HistoricoKm
import com.development.motorlog.data.Moto
import com.development.motorlog.data.atualizarKm
import com.development.motorlog.domain.ResumoAlertas
import com.development.motorlog.domain.calcularRecomendacoes
import com.development.motorlog.domain.calcularRitmoKmMes
import com.development.motorlog.domain.montarExportacao
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.domain.resumirAlertas
import com.development.motorlog.ui.util.hojeUtcMillis

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MotoViewModel(application : Application) : AndroidViewModel(application = application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.motoDao()
    private val historicoDao = AppDatabase.getDatabase(application).historicoKmDao()
    private val pecaDao = AppDatabase.getDatabase(application).pecaDao()
    private val registroDao = AppDatabase.getDatabase(application).registroDao()
    private val servicoDao = AppDatabase.getDatabase(application).servicoDao()

    var motos by mutableStateOf<List<Moto>>(emptyList())
        private set

    // false até a 1ª leitura do banco terminar: a Garagem não mostra "cadastre sua moto" antes disso
    var carregou by mutableStateOf(false)
        private set

    // motoId -> quantas trocas vencidas/perto (alertas da Garagem). Recalculado junto com a lista.
    var alertas by mutableStateOf<Map<Long, ResumoAlertas>>(emptyMap())
        private set

    // motoId -> km/mês estimado pelo HistoricoKm (null = ainda sem dados suficientes)
    var ritmos by mutableStateOf<Map<Long, Int?>>(emptyMap())
        private set

    init {
        carregarMotos()
    }

    // cargas disparadas em sequência (init, entrar na Garagem, depois de cada escrita): a anterior é
    // cancelada pra uma leitura antiga não sobrescrever estados com dados pré-escrita
    private var carga: Job? = null

    fun carregarMotos() {
        carga?.cancel()
        carga = viewModelScope.launch {
            val lista = dao.listarTodas()
            val pecas = pecaDao.listarPecas()
            alertas = lista.associate { moto ->
                moto.id to resumirAlertas(
                    calcularRecomendacoes(moto.kilometragem, pecas, registroDao.listarRegistros(moto.id))
                )
            }
            val hoje = hojeUtcMillis()
            ritmos = lista.associate { moto -> moto.id to calcularRitmoKmMes(historicoDao.listarPorMoto(moto.id), hoje) }
            motos = lista
            carregou = true
        }
    }

    // Tudo que há no banco, em CSV, pra compartilhar (backup legível fora do celular)
    fun exportar(aoPronto: (String) -> Unit) {
        viewModelScope.launch {
            aoPronto(
                montarExportacao(
                    motos = dao.listarTodas(),
                    pecas = pecaDao.listarPecas(),
                    registros = registroDao.listarTodos(),
                    servicos = servicoDao.listarTodos(),
                    formatarData = ::formatarData,
                )
            )
        }
    }

    fun inserirMoto(moto: Moto) {
        viewModelScope.launch {
            val hoje = hojeUtcMillis()
            val id = dao.inserir(moto.copy(kmAtualizadoEm = hoje))
            historicoDao.inserir(HistoricoKm(motoId = id, km = moto.kilometragem, data = hoje))
            carregarMotos()
        }
    }

    // A ação nº 1: salva o km, marca o dia e guarda 1 ponto/dia no histórico (ritmo e lembrete).
    fun atualizarKm(moto: Moto, novoKm: Int) {
        viewModelScope.launch {
            db.atualizarKm(moto, novoKm, hojeUtcMillis())
            carregarMotos()
        }
    }

    fun atualizarMoto(moto: Moto) {
        viewModelScope.launch {
            dao.atualizar(moto)
            carregarMotos()
        }
    }

    fun deletarMoto(moto: Moto) {
        viewModelScope.launch {
            dao.deletar(moto)
            carregarMotos()
        }
    }
}