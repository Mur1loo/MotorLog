package com.development.motorlog.domain

data class Recomendacao(
    val pecaNome: String,
    val kmProximaTroca: Int?,
    val kmRestante: Int?,
    val statusTroca: StatusTroca,
)
