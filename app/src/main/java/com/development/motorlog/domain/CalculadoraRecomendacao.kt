package com.development.motorlog.domain

import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro

fun calcularRecomendacoes(
    kmAtual: Int,
    pecas: List<Peca>,
    registros: List<Registro>
): List<Recomendacao> {
    return pecas.mapNotNull { peca ->
        val ultimaTroca = registros
            .filter { registro -> registro.pecaId == peca.id }
            .maxByOrNull { registro -> registro.kmTroca }

        if (ultimaTroca == null) {
            null
        } else {
            val proxima = ultimaTroca.kmTroca + peca.intervaloKm
            Recomendacao(
                pecaNome = peca.nome,
                kmProximaTroca = proxima,
                kmRestante = proxima - kmAtual
            )
        }
    }
}