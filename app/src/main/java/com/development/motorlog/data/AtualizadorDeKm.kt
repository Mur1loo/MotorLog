package com.development.motorlog.data

// A ação nº 1 do app, num só lugar: salva o km, marca o dia e guarda 1 ponto/dia no histórico.
// Usado pelo MotoViewModel (telas) e pelo AtualizarKmReceiver (resposta rápida da notificação).
suspend fun AppDatabase.atualizarKm(moto: Moto, novoKm: Int, hoje: Long) {
    motoDao().atualizar(moto.copy(kilometragem = novoKm, kmAtualizadoEm = hoje))
    if (historicoKmDao().atualizarDia(moto.id, hoje, novoKm) == 0) {
        historicoKmDao().inserir(HistoricoKm(motoId = moto.id, km = novoKm, data = hoje))
    }
}
