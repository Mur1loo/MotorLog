package com.development.motorlog.ui.viewModels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Passeio
import com.development.motorlog.data.atualizarKm
import kotlinx.coroutines.launch

// Rolês e viagens da moto aberta (domain/Roles.kt).
class RoleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.passeioDao()

    // mais recente primeiro
    var roles by mutableStateOf<List<Passeio>>(emptyList())
        private set
    private var motoId: Long? = null

    fun carregar(motoId: Long) {
        if (this.motoId != motoId) roles = emptyList()   // não mostra a moto anterior
        this.motoId = motoId
        viewModelScope.launch { roles = dao.listarPorMoto(motoId) }
    }

    // id 0 = novo. Como no abastecimento: o km da volta maior que o da moto atualiza o km dela.
    // aoSalvar(true) = o km da moto mudou (a tela recarrega as motos)
    fun salvar(moto: Moto, role: Passeio, hoje: Long, aoSalvar: (Boolean) -> Unit) {
        viewModelScope.launch {
            val limpo = role.copy(destino = role.destino.trim(), companhia = role.companhia.trim(), nota = role.nota.trim())
            if (limpo.id == 0L) dao.inserir(limpo) else dao.atualizar(limpo)
            val maiorKm = maxOf(limpo.kmSaida, limpo.kmChegada)
            val kmMudou = maiorKm > moto.kilometragem
            if (kmMudou) db.atualizarKm(moto, maiorKm, hoje)
            roles = dao.listarPorMoto(moto.id)
            aoSalvar(kmMudou)
        }
    }

    fun excluir(role: Passeio) {
        viewModelScope.launch {
            dao.deletar(role)
            roles = dao.listarPorMoto(role.motoId)
        }
    }
}
