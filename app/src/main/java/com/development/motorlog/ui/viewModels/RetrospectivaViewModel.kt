package com.development.motorlog.ui.viewModels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.DadosDaMoto
import kotlinx.coroutines.launch

// Tudo o que a retrospectiva do ano precisa da moto, lido de uma vez (domain/Retrospectiva.kt).
class RetrospectivaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)

    var dados by mutableStateOf<DadosDaMoto?>(null)
        private set

    fun carregar(moto: Moto) {
        if (dados?.moto?.id != moto.id) dados = null   // não mostra a moto anterior
        viewModelScope.launch {
            dados = DadosDaMoto(
                moto = moto,
                historicoKm = db.historicoKmDao().listarPorMoto(moto.id),
                registros = db.registroDao().listarRegistros(moto.id),
                servicos = db.servicoDao().query(moto.id),
                abastecimentos = db.abastecimentoDao().listarPorMoto(moto.id),
                cuidados = db.cuidadoDao().listarPorMoto(moto.id),
                fotos = db.fotoMotoDao().listarPorMoto(moto.id),
                desejos = db.desejoDao().listarPorMoto(moto.id),
                roles = db.passeioDao().listarPorMoto(moto.id),
            )
        }
    }
}
