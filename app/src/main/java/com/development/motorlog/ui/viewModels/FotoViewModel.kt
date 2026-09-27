package com.development.motorlog.ui.viewModels

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.FotoMoto
import com.development.motorlog.data.Moto
import com.development.motorlog.fotos.ArmazemDeFotos
import com.development.motorlog.ui.util.hojeUtcMillis
import kotlinx.coroutines.launch

// Álbum da moto aberta: lista, adicionar (arquivo já salvo → linha no banco), legenda, excluir.
// A capa é decidida em escolherCapa(fotos, moto.fotoCapaId); trocar a capa é atualizar a Moto.
class FotoViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).fotoMotoDao()

    // fotos da moto carregada, mais recente primeiro
    var fotos by mutableStateOf<List<FotoMoto>>(emptyList())
        private set
    private var motoId: Long? = null

    // foto escolhida/tirada, já salva em arquivo, esperando a legenda (ou o "cancelar")
    var pendente by mutableStateOf<String?>(null)
        private set
    var processando by mutableStateOf(false)
        private set

    fun carregar(motoId: Long) {
        if (this.motoId != motoId) fotos = emptyList()   // não mostra o álbum da moto anterior
        this.motoId = motoId
        viewModelScope.launch { fotos = dao.listarPorMoto(motoId) }
    }

    // 1º passo: copia a imagem da Uri pro armazenamento do app (reduzida). aoFalhar: mensagem.
    fun receberImagem(uri: Uri, aoFalhar: () -> Unit) {
        viewModelScope.launch {
            processando = true
            val nome = ArmazemDeFotos.salvarDe(getApplication<Application>(), uri)
            ArmazemDeFotos.limparTemporariosDaCamera(getApplication<Application>())
            processando = false
            if (nome == null) aoFalhar() else pendente = nome
        }
    }

    // 2º passo: guarda no álbum com a legenda (o km e o dia de hoje vêm junto)
    fun confirmarPendente(moto: Moto, legenda: String, aoSalvar: () -> Unit) {
        val nome = pendente ?: return
        pendente = null
        viewModelScope.launch {
            dao.inserir(FotoMoto(motoId = moto.id, arquivo = nome, data = hojeUtcMillis(), km = moto.kilometragem, legenda = legenda.trim()))
            fotos = dao.listarPorMoto(moto.id)
            aoSalvar()
        }
    }

    fun descartarPendente() {
        pendente?.let { ArmazemDeFotos.apagar(getApplication<Application>(), it) }
        pendente = null
    }

    fun atualizarLegenda(foto: FotoMoto, legenda: String) {
        viewModelScope.launch {
            dao.atualizar(foto.copy(legenda = legenda.trim()))
            fotos = dao.listarPorMoto(foto.motoId)
        }
    }

    fun excluir(foto: FotoMoto, aoExcluir: () -> Unit) {
        viewModelScope.launch {
            dao.deletar(foto)
            ArmazemDeFotos.apagar(getApplication<Application>(), foto.arquivo)
            fotos = dao.listarPorMoto(foto.motoId)
            aoExcluir()
        }
    }

    // se o app morrer com uma foto pendente (sem legenda), o arquivo não fica órfão pra sempre
    override fun onCleared() {
        pendente?.let { ArmazemDeFotos.apagar(getApplication<Application>(), it) }
    }
}
