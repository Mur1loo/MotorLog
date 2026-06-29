package com.development.motorlog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PecaDao {
    @Insert
    suspend fun inserir(peca: Peca)

    @Update
    suspend fun atualizar(peca: Peca)

    @Delete
    suspend fun deletar(peca: Peca)

    @Query("SELECT * FROM PECA")
    suspend fun listarPecas(): List<Peca>
}