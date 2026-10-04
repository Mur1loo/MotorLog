package com.development.motorlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Um abastecimento: km do painel na hora, quantos litros entraram, quanto custou e se completou
// o tanque. O consumo (km/l) sai da soma dos abastecimentos, parciais inclusive; dois tanques
// cheios deixam a conta exata (domain/Consumo.kt).
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
    // true = completou o tanque (opcional; o normal é parcial, "coloquei R$ 20")
    val tanqueCheio: Boolean = false,
    // meia-noite UTC do dia
    val data: Long,
)
