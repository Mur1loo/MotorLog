package com.development.motorlog.ui.viewModels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.HistoricoKm
import com.development.motorlog.data.Moto
import com.development.motorlog.data.ResumoImportacao
import com.development.motorlog.data.atualizarKm
import com.development.motorlog.data.corrigirRegistroDeKm
import com.development.motorlog.data.importar
import com.development.motorlog.domain.DadosImportados
import com.development.motorlog.domain.MediasDeKm
import com.development.motorlog.domain.ResumoAlertas
import com.development.motorlog.domain.calcularMediasDeKm
import com.development.motorlog.domain.calcularRecomendacoes
import com.development.motorlog.domain.comRevisao
import com.development.motorlog.domain.escolherCapa
import com.development.motorlog.domain.kmRodadosNoApp
import com.development.motorlog.domain.lerExportacao
import com.development.motorlog.domain.montarExportacao
import com.development.motorlog.domain.recomendacaoDeRevisao
import com.development.motorlog.domain.resumirAlertas
import com.development.motorlog.fotos.ArmazemDeFotos
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.hojeUtcMillis
import com.development.motorlog.ui.util.lerData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MotoViewModel(application : Application) : AndroidViewModel(application = application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.motoDao()
    private val historicoDao = AppDatabase.getDatabase(application).historicoKmDao()
    private val pecaDao = AppDatabase.getDatabase(application).pecaDao()
    private val registroDao = AppDatabase.getDatabase(application).registroDao()
    private val servicoDao = AppDatabase.getDatabase(application).servicoDao()
    private val fotoDao = db.fotoMotoDao()

    var motos by mutableStateOf<List<Moto>>(emptyList())
        private set

    // false até a 1ª leitura do banco terminar: a Garagem não mostra "cadastre sua moto" antes disso
    var carregou by mutableStateOf(false)
        private set

    // motoId -> quantas trocas vencidas/perto (alertas da Garagem). Recalculado junto com a lista.
    var alertas by mutableStateOf<Map<Long, ResumoAlertas>>(emptyMap())
        private set

    // motoId -> pontos (dia, km) registrados: base dos marcos ("Passou dos 50.000 km!")
    var historicos by mutableStateOf<Map<Long, List<HistoricoKm>>>(emptyMap())
        private set
    // motoId -> média do último mês e média geral (domain/Ritmo.kt), mostradas no Painel
    var medias by mutableStateOf<Map<Long, MediasDeKm>>(emptyMap())
        private set
    // motoId -> km/mês usado nas previsões ("vence em ~6 dias"); null = ainda sem dados suficientes
    var ritmos by mutableStateOf<Map<Long, Int?>>(emptyMap())
        private set

    // motoId -> km rodados desde o 1º registro no app (base do custo por km)
    var kmRodados by mutableStateOf<Map<Long, Int>>(emptyMap())
        private set

    // motoId -> arquivo da foto de capa (só motos com foto). Garagem mostra a foto no lugar do badge.
    var capas by mutableStateOf<Map<Long, String>>(emptyMap())
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
                val recs = calcularRecomendacoes(moto.kilometragem, pecas, registroDao.listarRegistros(moto.id))
                moto.id to resumirAlertas(
                    comRevisao(recs, recomendacaoDeRevisao(moto.kilometragem, moto.intervaloRevisaoKm, servicoDao.query(moto.id)))
                )
            }
            val hoje = hojeUtcMillis()
            val historicos = lista.associate { moto -> moto.id to historicoDao.listarPorMoto(moto.id) }
            this@MotoViewModel.historicos = historicos
            medias = lista.associate { moto -> moto.id to calcularMediasDeKm(historicos.getValue(moto.id), moto.chegouEm, moto.kmChegada, hoje) }
            ritmos = medias.mapValues { it.value.paraPrevisao }
            kmRodados = lista.associate { moto -> moto.id to kmRodadosNoApp(historicos.getValue(moto.id), moto.kilometragem) }
            capas = lista.mapNotNull { moto -> escolherCapa(fotoDao.listarPorMoto(moto.id), moto.fotoCapaId)?.let { moto.id to it.arquivo } }.toMap()
            motos = lista
            carregou = true
        }
    }

    // Restaurar backup: 1) lê o CSV e guarda pra confirmação; 2) aplica sem duplicar
    var importacaoPendente by mutableStateOf<DadosImportados?>(null)
        private set

    fun prepararImportacao(texto: String) {
        importacaoPendente = lerExportacao(texto, ::lerData)
    }

    fun cancelarImportacao() { importacaoPendente = null }

    fun confirmarImportacao(aoTerminar: (ResumoImportacao) -> Unit) {
        val dados = importacaoPendente ?: return
        viewModelScope.launch {
            val resumo = db.importar(dados)
            importacaoPendente = null
            carregarMotos()
            aoTerminar(resumo)
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
                    abastecimentos = db.abastecimentoDao().listarTodos(),
                    cuidados = db.cuidadoDao().listarTodos(),
                    formatarData = ::formatarData,
                )
            )
        }
    }

    // aoSalvar recebe o id da moto nova depois que ela já está na lista (quem abre o Painel dela
    // logo em seguida não pode achar a lista sem ela — MainActivity volta pra Garagem nesse caso)
    fun inserirMoto(moto: Moto, aoSalvar: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val hoje = hojeUtcMillis()
            val id = dao.inserir(moto.copy(kmAtualizadoEm = hoje))
            historicoDao.inserir(HistoricoKm(motoId = id, km = moto.kilometragem, data = hoje))
            carregarMotos()
            carga?.join()
            aoSalvar(id)
        }
    }

    // A ação nº 1: salva o km, marca o dia e guarda 1 ponto/dia no histórico (ritmo e lembrete).
    fun atualizarKm(moto: Moto, novoKm: Int) {
        viewModelScope.launch {
            db.atualizarKm(moto, novoKm, hojeUtcMillis())
            carregarMotos()
        }
    }

    // Registros de km: corrigir (novoKm) ou apagar (null) um km de outro dia; médias e marcos se refazem
    fun corrigirRegistroDeKm(moto: Moto, ponto: HistoricoKm, novoKm: Int?) {
        viewModelScope.launch {
            db.corrigirRegistroDeKm(moto, ponto, novoKm)
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
            // o CASCADE apaga as linhas do álbum; os arquivos das fotos o app apaga aqui
            val fotos = fotoDao.listarPorMoto(moto.id)
            dao.deletar(moto)
            fotos.forEach { ArmazemDeFotos.apagar(getApplication<Application>(), it.arquivo) }
            carregarMotos()
        }
    }
}