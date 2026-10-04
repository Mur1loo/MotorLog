package com.development.motorlog.ui.viewModels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.Desejo
import kotlinx.coroutines.launch

// Lista de desejos da moto aberta (domain/Desejos.kt).
class DesejoViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).desejoDao()

    var desejos by mutableStateOf<List<Desejo>>(emptyList())
        private set
    private var motoId: Long? = null

    fun carregar(motoId: Long) {
        if (this.motoId != motoId) desejos = emptyList()   // não mostra a moto anterior
        this.motoId = motoId
        viewModelScope.launch { desejos = dao.listarPorMoto(motoId) }
    }

    // id 0 = novo; senão atualiza (inclui marcar como instalado ou voltar pra lista)
    fun salvar(desejo: Desejo) {
        viewModelScope.launch {
            val limpo = desejo.copy(nome = desejo.nome.trim(), nota = desejo.nota.trim())
            if (limpo.id == 0L) dao.inserir(limpo) else dao.atualizar(limpo)
            desejos = dao.listarPorMoto(desejo.motoId)
        }
    }

    fun excluir(desejo: Desejo) {
        viewModelScope.launch {
            dao.deletar(desejo)
            desejos = dao.listarPorMoto(desejo.motoId)
        }
    }
}
