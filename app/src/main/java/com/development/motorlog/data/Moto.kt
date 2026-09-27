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
    // cor escolhida pelo dono (índice em MlAccentsMoto); -1 = automática pelo id (motos anteriores à v11)
    val cor: Int = -1,
    // foto de capa escolhida no álbum; 0 = a foto mais recente
    val fotoCapaId: Long = 0,
)