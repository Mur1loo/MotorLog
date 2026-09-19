package com.development.motorlog.domain

import com.development.motorlog.data.HistoricoKm

// Janela de histórico usada pra estimar o ritmo (uso recente vale mais que o de 1 ano atrás)
const val JANELA_RITMO_DIAS = 90
// Abaixo disso a estimativa é ruído (2 pontos no mesmo fim de semana)
const val MINIMO_DIAS_PARA_RITMO = 7

// km/mês a partir dos pontos (dia, km) do histórico. null quando não dá pra estimar com confiança.
fun calcularRitmoKmMes(historico: List<HistoricoKm>, hoje: Long): Int? {
    val pontos = historico
        .filter { diasEntre(it.data, hoje) <= JANELA_RITMO_DIAS }
        .sortedBy { it.data }
    if (pontos.size < 2) return null
    val primeiro = pontos.first()
    val ultimo = pontos.last()
    val dias = diasEntre(primeiro.data, ultimo.data)
    val km = ultimo.km - primeiro.km
    if (dias < MINIMO_DIAS_PARA_RITMO || km <= 0) return null
    return (km.toLong() * 30 / dias).toInt()
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
