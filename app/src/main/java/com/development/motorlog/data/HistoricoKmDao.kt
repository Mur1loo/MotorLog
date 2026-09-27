package com.development.motorlog.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoricoKmDao {
    @Insert
    suspend fun inserir(ponto: HistoricoKm)

    // 1 ponto por dia: se já existe o dia, só atualiza o km
    @Query("UPDATE HistoricoKm SET km = :km WHERE motoId = :motoId AND data = :data")
    suspend fun atualizarDia(motoId: Long, data: Long, km: Int): Int

    @Query("SELECT * FROM HistoricoKm WHERE motoId = :motoId ORDER BY data")
    suspend fun listarPorMoto(motoId: Long): List<HistoricoKm>

    // dias em que o usuário registrou km em qualquer moto: sinal de retenção pro pedido de apoio
    @Query("SELECT DISTINCT data FROM HistoricoKm ORDER BY data")
    suspend fun listarDiasComKm(): List<Long>
}
