package com.development.motorlog.data

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
    // meia-noite UTC do dia da última atualização de km; 0 = nunca registrado (motos anteriores à v9)
    val kmAtualizadoEm: Long = 0,
    // revisão na oficina a cada N km (0 = não avisar). A última revisão é o Servico mais recente
    // cujo tipo contém "revis"; a próxima = km dele + este intervalo.
    val intervaloRevisaoKm: Int = 0,
)