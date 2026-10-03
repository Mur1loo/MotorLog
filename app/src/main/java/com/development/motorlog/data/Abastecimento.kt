package com.development.motorlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Um abastecimento: km do painel na hora, quantos litros entraram, quanto custou e se encheu o
// tanque. O consumo (km/l) sai de "tanque cheio a tanque cheio" (domain/Consumo.kt).
// Litros em MILILITROS (8,734 L = 8734) e valor em CENTAVOS: inteiros, sem erro de arredondamento.
@Entity(
    foreignKeys = [ForeignKey(
        entity = Moto::class,
        parentColumns = ["id"],
        childColumns = ["motoId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("motoId")],
)
data class Abastecimento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val motoId: Long,
    val km: Int,
    val mililitros: Int,
    // centavos; 0 = não informado
    val valor: Int = 0,
    // false = parcial ("coloquei R$ 20"): os litros somam no próximo tanque cheio
    val tanqueCheio: Boolean = true,
    // meia-noite UTC do dia
    val data: Long,
)
