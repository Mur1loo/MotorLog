package com.development.motorlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Um ponto (dia, km) por atualização de km. Alimenta "atualizado há X dias" e o ritmo km/mês.
// Convenção: 'data' = meia-noite UTC do dia; no máximo 1 linha por moto por dia.
@Entity(
    foreignKeys = [ForeignKey(
        entity = Moto::class,
        parentColumns = ["id"],
        childColumns = ["motoId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("motoId")],
)
data class HistoricoKm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val motoId: Long,
    val km: Int,
    val data: Long,
)
