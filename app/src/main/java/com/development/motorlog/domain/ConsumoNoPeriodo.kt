package com.development.motorlog.domain

import com.development.motorlog.data.Abastecimento
import java.time.LocalDate

// Consumo num período ("este mês", "mês passado", datas escolhidas), além da média geral.
// A conta é a mesma de estimarConsumo (domain/Consumo.kt), com um cuidado: o 1º abastecimento do
// período repõe o que foi queimado ANTES dele, então o último abastecimento antes do período entra
// como ponto de partida (só o km dele; os litros dele não contam). Sem isso, todo período
// começaria "perdendo" um abastecimento e o primeiro mês nunca teria média.

enum class Periodo(val rotulo: String) {
    ULTIMOS_30_DIAS("30 dias"),
    ESTE_MES("Este mês"),
    MES_PASSADO("Mês passado"),
    ULTIMOS_3_MESES("3 meses"),
    ESTE_ANO("Este ano"),
    PERSONALIZADO("Escolher datas"),
}

// dias inteiros, meia-noite UTC, os dois inclusive
data class Intervalo(val inicio: Long, val fim: Long)

private fun dia(d: LocalDate) = d.toEpochDay() * MILLIS_POR_DIA

// o intervalo de um período rápido, até hoje. PERSONALIZADO fica com as datas que a pessoa escolher
// (aqui devolve os últimos 30 dias, que é por onde a escolha começa).
fun intervaloDoPeriodo(periodo: Periodo, hoje: Long): Intervalo {
    val d = LocalDate.ofEpochDay(hoje / MILLIS_POR_DIA)
    return when (periodo) {
        Periodo.ULTIMOS_30_DIAS, Periodo.PERSONALIZADO -> Intervalo(dia(d.minusDays(29)), hoje)
        Periodo.ESTE_MES -> Intervalo(dia(d.withDayOfMonth(1)), hoje)
        Periodo.MES_PASSADO -> d.minusMonths(1).let { m -> Intervalo(dia(m.withDayOfMonth(1)), dia(m.withDayOfMonth(m.lengthOfMonth()))) }
        Periodo.ULTIMOS_3_MESES -> Intervalo(dia(d.minusMonths(3).plusDays(1)), hoje)
        Periodo.ESTE_ANO -> Intervalo(dia(d.withDayOfYear(1)), hoje)
    }
}

data class ConsumoNoPeriodo(
    val abastecimentos: Int,          // feitos dentro do período
    val mililitros: Int,              // litros colocados no período
    val gasto: Int,                   // centavos (dos que têm valor)
    val kmRodados: Int?,              // do ponto de partida ao último abastecimento do período
    val consumo: EstimativaConsumo?,  // null = abastecimentos ou km de menos no período
    val precoMedio: Int?,             // centavos por litro, dos que têm valor
    val custoPorKm: Double?,          // R$ de combustível por km no período
)

fun consumoNoPeriodo(abastecimentos: List<Abastecimento>, intervalo: Intervalo): ConsumoNoPeriodo {
    val ordem = compareBy<Abastecimento>({ it.km }, { it.data }, { it.id })
    val ord = abastecimentos.sortedWith(ordem)
    val dentro = ord.filter { it.data in intervalo.inicio..intervalo.fim }
    val partida = ord.lastOrNull { it.data < intervalo.inicio }
    val base = listOfNotNull(partida) + dentro
    val consumo = if (dentro.isEmpty()) null else estimarConsumo(base)
    val comValor = dentro.filter { it.valor > 0 && it.mililitros > 0 }
    val precoMedio = if (comValor.isEmpty()) null
        else (comValor.sumOf { it.valor.toLong() } * 1000 / comValor.sumOf { it.mililitros.toLong() }).toInt()
    return ConsumoNoPeriodo(
        abastecimentos = dentro.size,
        mililitros = dentro.sumOf { it.mililitros },
        gasto = dentro.sumOf { it.valor },
        kmRodados = if (base.size >= 2) (base.last().km - base.first().km).takeIf { it > 0 } else null,
        consumo = consumo,
        precoMedio = precoMedio,
        custoPorKm = if (consumo != null && precoMedio != null) precoMedio / 100.0 / consumo.kmPorLitro else null,
    )
}
