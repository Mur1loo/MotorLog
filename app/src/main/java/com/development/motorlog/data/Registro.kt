package com.development.motorlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
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
    )]
)
data class Registro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val motoId: Long,
    val pecaId: Long,
    val kmTroca: Int,
)