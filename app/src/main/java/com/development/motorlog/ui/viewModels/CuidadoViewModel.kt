package com.development.motorlog.ui.viewModels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.Cuidado
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.TipoCuidado
import com.development.motorlog.domain.jaRegistradoHoje
import kotlinx.coroutines.launch

// Diário de cuidados da moto aberta (domain/Cuidados.kt).
class CuidadoViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).cuidadoDao()

    // mais recente primeiro
    var cuidados by mutableStateOf<List<Cuidado>>(emptyList())
        private set
    private var motoId: Long? = null

    fun carregar(motoId: Long) {
        if (this.motoId != motoId) cuidados = emptyList()   // não mostra a moto anterior
        this.motoId = motoId
        viewModelScope.launch { cuidados = dao.listarPorMoto(motoId) }
    }

    // 1 toque no Painel: hoje, no km atual. aoTerminar(false) = já tinha registrado hoje (toque repetido)
    fun registrar(moto: Moto, tipo: TipoCuidado, hoje: Long, aoTerminar: (Boolean) -> Unit) {
        viewModelScope.launch {
            val atuais = dao.listarPorMoto(moto.id)
            if (jaRegistradoHoje(atuais, tipo, hoje)) {
                aoTerminar(false)
                return@launch
            }
            dao.inserir(Cuidado(motoId = moto.id, tipo = tipo.codigo, data = hoje, km = moto.kilometragem))
            cuidados = dao.listarPorMoto(moto.id)
            aoTerminar(true)
        }
    }

    fun excluir(cuidado: Cuidado) {
        viewModelScope.launch {
            dao.deletar(cuidado)
            cuidados = dao.listarPorMoto(cuidado.motoId)
        }
    }
}
