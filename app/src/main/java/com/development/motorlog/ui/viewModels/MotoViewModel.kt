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
import com.development.motorlog.ui.util.hojeUtcMillis

import kotlinx.coroutines.launch

class MotoViewModel(application : Application) : AndroidViewModel(application = application) {

    private val dao = AppDatabase.getDatabase(application).motoDao()
    private val historicoDao = AppDatabase.getDatabase(application).historicoKmDao()

    var motos by mutableStateOf<List<Moto>>(emptyList())
        private set

    init {
        carregarMotos()
    }

    fun carregarMotos() {
        viewModelScope.launch {
            motos = dao.listarTodas()
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
            val hoje = hojeUtcMillis()
            dao.atualizar(moto.copy(kilometragem = novoKm, kmAtualizadoEm = hoje))
            if (historicoDao.atualizarDia(moto.id, hoje, novoKm) == 0) {
                historicoDao.inserir(HistoricoKm(motoId = moto.id, km = novoKm, data = hoje))
            }
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