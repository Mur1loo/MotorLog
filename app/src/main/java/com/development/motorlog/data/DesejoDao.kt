package com.development.motorlog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DesejoDao {
    @Insert
    suspend fun inserir(desejo: Desejo): Long

    @Update
    suspend fun atualizar(desejo: Desejo)

    @Delete
    suspend fun deletar(desejo: Desejo)

    @Query("SELECT * FROM Desejo WHERE motoId = :motoId ORDER BY criadoEm DESC, id DESC")
    suspend fun listarPorMoto(motoId: Long): List<Desejo>

    @Query("SELECT * FROM Desejo")
    suspend fun listarTodos(): List<Desejo>
}
