package com.development.motorlog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PasseioDao {
    @Insert
    suspend fun inserir(passeio: Passeio): Long

    @Update
    suspend fun atualizar(passeio: Passeio)

    @Delete
    suspend fun deletar(passeio: Passeio)

    // mais recente primeiro
    @Query("SELECT * FROM Passeio WHERE motoId = :motoId ORDER BY data DESC, id DESC")
    suspend fun listarPorMoto(motoId: Long): List<Passeio>

    @Query("SELECT * FROM Passeio")
    suspend fun listarTodos(): List<Passeio>
}
