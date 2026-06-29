package com.development.motorlog.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RegistroDao {

    @Insert
    suspend fun inserirRegistro(registro: Registro)

    @Query("Select * FROM Registro WHERE motoId = :motoId")
    suspend fun listarRegistros(motoId: Long): List<Registro>

    @Query("SELECT * FROM Registro WHERE servicoId = :servicoId")
    suspend fun listarPorServico(servicoId: Long): List<Registro>
}