package com.development.motorlog.ui.viewModels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.development.motorlog.data.Abastecimento
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.Moto
import com.development.motorlog.data.atualizarKm
import com.development.motorlog.domain.ResumoConsumo
import com.development.motorlog.domain.resumirConsumo
import kotlinx.coroutines.launch

// Abastecimentos da moto aberta e o resumo de consumo (domain/Consumo.kt).
class AbastecimentoViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.abastecimentoDao()

    // mais recente primeiro
    var abastecimentos by mutableStateOf<List<Abastecimento>>(emptyList())
        private set
    var resumo by mutableStateOf<ResumoConsumo?>(null)
        private set
    private var motoId: Long? = null

    fun carregar(motoId: Long) {
        if (this.motoId != motoId) { abastecimentos = emptyList(); resumo = null }   // não mostra a moto anterior
        this.motoId = motoId
        viewModelScope.launch { recarregar(motoId) }
    }

    private suspend fun recarregar(motoId: Long) {
        val lista = dao.listarPorMoto(motoId)
        abastecimentos = lista
        resumo = resumirConsumo(lista)
    }

    // Abasteceu olhando o painel: o km do abastecimento também atualiza o km da moto (quando é
    // maior que o atual) — é o jeito de manter o km em dia sem um passo a mais.
    // aoSalvar(kmAtualizado): true quando o km da moto mudou (quem chama recarrega as motos).
    fun registrar(moto: Moto, novo: Abastecimento, hoje: Long, aoSalvar: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (novo.id == 0L) dao.inserir(novo.copy(motoId = moto.id)) else dao.atualizar(novo)
            val kmMudou = novo.km > moto.kilometragem
            if (kmMudou) db.atualizarKm(moto, novo.km, hoje)
            recarregar(moto.id)
            aoSalvar(kmMudou)
        }
    }

    fun excluir(abastecimento: Abastecimento) {
        viewModelScope.launch {
            dao.deletar(abastecimento)
            recarregar(abastecimento.motoId)
        }
    }
}
