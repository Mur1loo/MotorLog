package com.development.motorlog

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Moto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val modelo: String,
    val placa: String,
    val anoFabricacao: Int,
    var kilometragem: Int,
)