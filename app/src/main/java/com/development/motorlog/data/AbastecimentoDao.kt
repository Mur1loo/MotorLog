package com.development.motorlog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AbastecimentoDao {
    @Insert
    suspend fun inserir(abastecimento: Abastecimento): Long

    @Update
    suspend fun atualizar(abastecimento: Abastecimento)

    @Delete
    suspend fun deletar(abastecimento: Abastecimento)

    // mais recente primeiro (a lista da tela); o cálculo de consumo reordena por km
    @Query("SELECT * FROM Abastecimento WHERE motoId = :motoId ORDER BY km DESC, data DESC")
    suspend fun listarPorMoto(motoId: Long): List<Abastecimento>

    @Query("SELECT * FROM Abastecimento")
    suspend fun listarTodos(): List<Abastecimento>
}
