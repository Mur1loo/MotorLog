package com.development.motorlog.domain

import com.development.motorlog.data.Servico
import java.text.Normalizer

// A revisão na oficina entra na mesma lista das peças, como um item especial (pecaId negativo):
// mesmo status, mesma cor, mesma ordenação. Tocar nela abre "Fui à oficina", não "Troquei".
const val REVISAO_ID = -1L
const val REVISAO_NOME = "Revisão na oficina"

private fun ehRevisao(tipo: String): Boolean =
    Normalizer.normalize(tipo, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase().contains("revis")

// null quando a moto não tem intervalo de revisão configurado (0 = não avisar)
fun recomendacaoDeRevisao(kmAtual: Int, intervaloRevisaoKm: Int, servicos: List<Servico>): Recomendacao? {
    if (intervaloRevisaoKm <= 0) return null
    val ultima = servicos.filter { ehRevisao(it.tipoServico) }.maxByOrNull { it.kilometragem }
        ?: return Recomendacao(REVISAO_ID, REVISAO_NOME, null, null, null, StatusTroca.NUNCA_TROCADA)
    val proxima = ultima.kilometragem + intervaloRevisaoKm
    val status = when {
        proxima <= kmAtual -> StatusTroca.VENCIDA
        proxima - kmAtual <= intervaloRevisaoKm / 4 -> StatusTroca.PERTO
        else -> StatusTroca.OK
    }
    return Recomendacao(REVISAO_ID, REVISAO_NOME, ultima.kilometragem, proxima, proxima - kmAtual, status)
}

// Lista de peças + revisão, na mesma ordem de urgência
fun comRevisao(recomendacoes: List<Recomendacao>, revisao: Recomendacao?): List<Recomendacao> =
    if (revisao == null) recomendacoes
    else (recomendacoes + revisao).sortedBy { it.kmRestante ?: Int.MAX_VALUE }

val Recomendacao.ehRevisao: Boolean get() = pecaId == REVISAO_ID
