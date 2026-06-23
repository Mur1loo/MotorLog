package com.development.motorlog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Peca(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val intervaloKm: Int,
)