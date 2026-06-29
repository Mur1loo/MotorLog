package com.development.motorlog.domain

import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro

fun calcularRecomendacoes(
    kmAtual: Int,
    pecas: List<Peca>,
    registros: List<Registro>
): List<Recomendacao> {
    return pecas
            .map { peca ->
        val ultimaTroca = registros
            .filter { registro -> registro.pecaId == peca.id }
            .maxByOrNull { registro -> registro.kmTroca }

        if (ultimaTroca == null) {
            Recomendacao(
                pecaId = peca.id,
                pecaNome = peca.nome,
                kmProximaTroca = null,
                kmRestante = null,
                statusTroca = StatusTroca.NUNCA_TROCADA,
            )
        } else {
            val proxima = ultimaTroca.kmTroca + peca.intervaloKm
            val status = when {
                proxima <= kmAtual -> StatusTroca.VENCIDA
                proxima - kmAtual <= peca.intervaloKm / 4 -> StatusTroca.PERTO
                else -> StatusTroca.OK
            }
            Recomendacao(
                pecaId = peca.id,
                pecaNome = peca.nome,
                kmProximaTroca = proxima,
                kmRestante = proxima - kmAtual,
                statusTroca = status
            )
        }
    }.sortedBy { rec -> rec.kmRestante?: Int.MAX_VALUE }

}