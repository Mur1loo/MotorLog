package com.development.motorlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Um item da lista de desejos da moto: "quero colocar na minha moto" (baú, protetor de motor,
// manopla, viseira fumê). Planejar a personalização é parte do sonho; quando é instalado, sai
// da lista e entra na história da moto (linha do tempo).
@Entity(
    foreignKeys = [ForeignKey(
        entity = Moto::class,
        parentColumns = ["id"],
        childColumns = ["motoId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("motoId")],
)
data class Desejo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val motoId: Long,
    val nome: String,
    // centavos; 0 = sem preço. Depois de instalado, o que foi pago de verdade
    val preco: Int = 0,
    // onde comprar, modelo, cor… (opcional)
    val nota: String = "",
    // meia-noite UTC do dia em que entrou na lista
    val criadoEm: Long,
    // meia-noite UTC do dia em que foi instalado; 0 = ainda é desejo
    val instaladoEm: Long = 0,
    // km da moto na instalação; -1 = ainda é desejo
    val kmInstalado: Int = -1,
)
