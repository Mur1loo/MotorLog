package com.development.motorlog

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MotoDao {
    @Insert
    suspend fun inserir(moto: Moto)

    @Update
    suspend fun atualizar(moto: Moto)

    @Query(value = "SELECT * FROM Moto")
    suspend fun listarTodas(): List<Moto>
}