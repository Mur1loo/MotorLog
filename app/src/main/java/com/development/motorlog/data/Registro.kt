package com.development.motorlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Moto::class,
        parentColumns = ["id"],
        childColumns = ["motoId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Peca::class,
        parentColumns = ["id"],
        childColumns = ["pecaId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Servico::class,
        parentColumns = ["id"],
        childColumns = ["servicoId"],
        onDelete = ForeignKey.SET_NULL
    )],
    // índices nas FKs (o Room avisa a cada build sem eles; entram na v8)
    indices = [Index("motoId"), Index("pecaId"), Index("servicoId")],
)
data class Registro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val motoId: Long,
    val pecaId: Long,
    val kmTroca: Int,
    val servicoId: Long?,
    val preco: Int = 0,
    // meia-noite UTC do dia da troca; 0 = dia desconhecido (trocas anteriores à v11, "não lembro")
    val data: Long = 0,
)