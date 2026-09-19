package com.development.motorlog.domain

data class Recomendacao(
    val pecaId: Long,
    val pecaNome: String,
    val kmUltimaTroca: Int?,
    val kmProximaTroca: Int?,
    val kmRestante: Int?,
    val statusTroca: StatusTroca,
)
