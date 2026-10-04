package com.development.motorlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Um rolê ou viagem com a moto: pra onde, quando, o km do painel na saída e na volta, quem foi
// junto e uma foto do álbum. Sem GPS (gasta bateria e pede localização): o km do painel basta.
// As viagens são as memórias mais fortes com a moto.
@Entity(
    foreignKeys = [ForeignKey(
        entity = Moto::class,
        parentColumns = ["id"],
        childColumns = ["motoId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("motoId")],
)
data class Passeio(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val motoId: Long,
    val destino: String,
    // meia-noite UTC do dia da saída
    val data: Long,
    val kmSaida: Int,
    // km do painel na volta; -1 = não anotado
    val kmChegada: Int = -1,
    // quem foi junto (garupa, a galera) — opcional
    val companhia: String = "",
    val nota: String = "",
    // foto do álbum que representa o rolê; 0 = sem foto (foto excluída depois também cai aqui)
    val fotoId: Long = 0,
)
