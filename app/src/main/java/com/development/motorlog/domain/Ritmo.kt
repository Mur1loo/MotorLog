package com.development.motorlog.domain

import com.development.motorlog.data.HistoricoKm

// Quanto a moto roda por mês. Duas médias, porque elas respondem perguntas diferentes:
//  - último mês: o ritmo de agora (km registrados no app nos últimos 30 dias);
//  - geral: desde que ela chegou (dia e km da chegada, do cadastro) — ou desde o cadastro no app,
//    quando a chegada não foi informada.
// Ex.: chegou há 6 meses e rodou 10.000 km → geral ~1.667 km/mês; se nas últimas semanas rodou
// menos, o último mês mostra isso. Antes o app só tinha a média recente (90 dias, só com km
// registrado no app) e não dizia de onde vinha o número.

const val JANELA_ULTIMO_MES_DIAS = 30
// abaixo disso a estimativa é ruído (2 pontos no mesmo fim de semana)
const val MINIMO_DIAS_PARA_RITMO = 7
// a média do último mês só vale pra prever trocas quando cobre pelo menos 2 semanas
const val DIAS_PRA_PREVER_COM_ULTIMO_MES = 14

data class MediasDeKm(
    val ultimoMes: Int?,
    // dias cobertos pela média do último mês (entre o 1º e o último registro da janela)
    val diasUltimoMes: Int,
    val geral: Int?,
    // true = a média geral parte da chegada; false = do cadastro no app
    val geralDesdeAChegada: Boolean,
) {
    // a usada pra "vence em ~6 dias": o ritmo de agora quando há dados, senão o geral
    val paraPrevisao: Int?
        get() = if (ultimoMes != null && diasUltimoMes >= DIAS_PRA_PREVER_COM_ULTIMO_MES) ultimoMes else geral ?: ultimoMes
}

private fun kmPorMes(km: Int, dias: Int): Int? =
    if (dias < MINIMO_DIAS_PARA_RITMO || km <= 0) null else (km.toLong() * 30 / dias).toInt()

// kmChegada -1 / chegouEm 0 = chegada não informada
fun calcularMediasDeKm(historico: List<HistoricoKm>, chegouEm: Long, kmChegada: Int, hoje: Long): MediasDeKm {
    val pontos = historico.sortedWith(compareBy({ it.data }, { it.km }))
    val ultimo = pontos.lastOrNull()

    val janela = pontos.filter { diasEntre(it.data, hoje) <= JANELA_ULTIMO_MES_DIAS }
    val diasJanela = if (janela.size >= 2) diasEntre(janela.first().data, janela.last().data) else 0
    val ultimoMes = if (janela.size >= 2) kmPorMes(janela.last().km - janela.first().km, diasJanela) else null

    // geral: da chegada (se informada e anterior ao último registro) até o último km registrado
    val temChegada = chegouEm > 0 && kmChegada >= 0 && ultimo != null && chegouEm < ultimo.data
    val geral = when {
        ultimo == null -> null
        temChegada -> kmPorMes(ultimo.km - kmChegada, diasEntre(chegouEm, ultimo.data))
        else -> kmPorMes(ultimo.km - pontos.first().km, diasEntre(pontos.first().data, ultimo.data))
    }
    return MediasDeKm(ultimoMes, diasJanela, geral, geralDesdeAChegada = temChegada && geral != null)
}

// "faltam 400 km" → "~6 dias". null se não há ritmo ou a peça já venceu.
fun estimarDiasAteTroca(kmRestante: Int?, ritmoKmMes: Int?): Int? {
    if (kmRestante == null || kmRestante <= 0 || ritmoKmMes == null || ritmoKmMes <= 0) return null
    return (kmRestante.toLong() * 30 / ritmoKmMes).toInt()
}

fun descreverDias(dias: Int): String = when {
    dias == 0 -> "hoje"
    dias == 1 -> "~1 dia"
    dias < 60 -> "~$dias dias"
    else -> "~${dias / 30} meses"
}
