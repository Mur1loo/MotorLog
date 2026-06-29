package com.development.motorlog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ServicoDao {

    @Insert
    suspend fun inserir(servico: Servico): Long

    @Update
    suspend fun atualizar(servico: Servico)

    @Delete
    suspend fun remover(servico: Servico)

    @Query(value = "SELECT * FROM Servico WHERE motoId = :motoId")
    suspend fun query(motoId: Long): List<Servico>
}