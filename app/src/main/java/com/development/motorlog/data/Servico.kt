package com.development.motorlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(
    entity = Moto::class,
    parentColumns = ["id"],
    childColumns = ["motoId"],
    onDelete = ForeignKey.CASCADE
)], indices = [Index("motoId")])
data class Servico(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val motoId: Long,
    val custo: Int,
    val kilometragem: Int,
    val tipoServico: String,
    val data: Long,
    val local: String,
)
