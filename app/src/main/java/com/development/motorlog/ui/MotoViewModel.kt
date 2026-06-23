package com.development.motorlog.ui

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.Moto

import kotlinx.coroutines.launch

class MotoViewModel(application : Application) : AndroidViewModel(application = application) {

    private val dao = AppDatabase.getDatabase(application).motoDao()

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
            dao.inserir(moto)
            carregarMotos()
        }
    }

    fun atualizarMoto(moto: Moto) {
        viewModelScope.launch {
            dao.atualizar(moto)
            carregarMotos()
        }
    }
}