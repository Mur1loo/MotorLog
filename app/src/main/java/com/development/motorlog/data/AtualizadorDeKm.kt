package com.development.motorlog.data

import com.development.motorlog.domain.pontoMaisRecente

// A ação nº 1 do app, num só lugar: salva o km, marca o dia e guarda 1 ponto/dia no histórico.
// Usado pelo MotoViewModel (telas) e pelo AtualizarKmReceiver (resposta rápida da notificação).
suspend fun AppDatabase.atualizarKm(moto: Moto, novoKm: Int, hoje: Long) {
    motoDao().atualizar(moto.copy(kilometragem = novoKm, kmAtualizadoEm = hoje))
    if (historicoKmDao().atualizarDia(moto.id, hoje, novoKm) == 0) {
        historicoKmDao().inserir(HistoricoKm(motoId = moto.id, km = novoKm, data = hoje))
    }
}

// Correção de um registro de km de outro dia (novoKm) ou apagar (null). Se era o registro mais
// recente, o km da moto acompanha o que ficou por último — senão a moto seguiria mostrando o erro.
suspend fun AppDatabase.corrigirRegistroDeKm(moto: Moto, ponto: HistoricoKm, novoKm: Int?) {
    val dao = historicoKmDao()
    val eraOMaisRecente = pontoMaisRecente(dao.listarPorMoto(moto.id))?.id == ponto.id
    if (novoKm == null) dao.deletar(ponto) else dao.atualizar(ponto.copy(km = novoKm))
    if (eraOMaisRecente) {
        pontoMaisRecente(dao.listarPorMoto(moto.id))?.let { ultimo ->
            motoDao().atualizar(moto.copy(kilometragem = ultimo.km, kmAtualizadoEm = ultimo.data))
        }
    }
}
