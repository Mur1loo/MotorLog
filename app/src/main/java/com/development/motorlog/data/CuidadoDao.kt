package com.development.motorlog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CuidadoDao {
    @Insert
    suspend fun inserir(cuidado: Cuidado): Long

    @Delete
    suspend fun deletar(cuidado: Cuidado)

    // mais recente primeiro
    @Query("SELECT * FROM Cuidado WHERE motoId = :motoId ORDER BY data DESC, id DESC")
    suspend fun listarPorMoto(motoId: Long): List<Cuidado>

    @Query("SELECT * FROM Cuidado")
    suspend fun listarTodos(): List<Cuidado>
}
