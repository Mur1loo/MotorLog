package com.development.motorlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Um cuidado com a moto que não é troca de peça: lavei, encerei, lubrifiquei a corrente,
// calibrei os pneus. É o ritual de quem trata bem a moto — registrado em 1 toque no Painel.
// tipo = código de TipoCuidado (domain/Cuidados.kt), guardado como texto pra sobreviver a
// mudanças de ordem no enum.
@Entity(
    foreignKeys = [ForeignKey(
        entity = Moto::class,
        parentColumns = ["id"],
        childColumns = ["motoId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("motoId")],
)
data class Cuidado(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val motoId: Long,
    val tipo: String,
    // meia-noite UTC do dia
    val data: Long,
    // km da moto na hora
    val km: Int,
    val nota: String = "",
)
