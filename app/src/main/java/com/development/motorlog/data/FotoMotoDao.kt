package com.development.motorlog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FotoMotoDao {
    @Insert
    suspend fun inserir(foto: FotoMoto): Long

    @Update
    suspend fun atualizar(foto: FotoMoto)

    @Delete
    suspend fun deletar(foto: FotoMoto)

    // mais recente primeiro (é a ordem do álbum)
    @Query("SELECT * FROM FotoMoto WHERE motoId = :motoId ORDER BY data DESC, id DESC")
    suspend fun listarPorMoto(motoId: Long): List<FotoMoto>
}
