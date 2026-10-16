package com.development.motorlog.domain

import com.development.motorlog.data.Abastecimento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ConsumoNoPeriodoTest {
    private fun dia(a: Int, m: Int, d: Int) = LocalDate.of(a, m, d).toEpochDay() * MILLIS_POR_DIA
    private fun ab(km: Int, ml: Int, data: Long, valor: Int = 0, cheio: Boolean = false) =
        Abastecimento(id = km.toLong(), motoId = 1, km = km, mililitros = ml, valor = valor, tanqueCheio = cheio, data = data)

    // setembro: 3 abastecimentos; outubro: 2 (+ o último de setembro como ponto de partida)
    private val lista = listOf(
        ab(10_000, 5_000, dia(2026, 9, 2), valor = 3_000),
        ab(10_200, 5_000, dia(2026, 9, 10), valor = 3_000),
        ab(10_400, 5_000, dia(2026, 9, 20), valor = 3_000),
        ab(10_650, 5_000, dia(2026, 10, 3), valor = 3_200),
        ab(10_900, 5_000, dia(2026, 10, 12), valor = 3_200),
    )

    @Test
    fun `periodo usa o ultimo abastecimento antes dele como ponto de partida`() {
        val r = consumoNoPeriodo(lista, Intervalo(dia(2026, 10, 1), dia(2026, 10, 31)))
        assertEquals(2, r.abastecimentos)
        assertEquals(10_000, r.mililitros)
        assertEquals(6_400, r.gasto)
        assertEquals(500, r.kmRodados)
        // 500 km (10.400 → 10.900) ÷ 10 L colocados em outubro = 50 km/l
        assertEquals(50.0, r.consumo!!.kmPorLitro, 0.001)
        assertEquals(640, r.precoMedio)
        assertEquals(6.40 / 50.0, r.custoPorKm!!, 0.0001)
    }

    @Test
    fun `primeiro periodo, sem ponto de partida, conta do primeiro ao ultimo`() {
        val r = consumoNoPeriodo(lista, Intervalo(dia(2026, 9, 1), dia(2026, 9, 30)))
        assertEquals(3, r.abastecimentos)
        assertEquals(400, r.kmRodados)
        // 400 km ÷ 10 L (os litros do 1º não contam: não se sabe o que ele repôs)
        assertEquals(40.0, r.consumo!!.kmPorLitro, 0.001)
    }

    @Test
    fun `periodo sem abastecimento nao tem consumo`() {
        val r = consumoNoPeriodo(lista, Intervalo(dia(2026, 8, 1), dia(2026, 8, 31)))
        assertEquals(0, r.abastecimentos)
        assertNull(r.consumo)
        assertNull(r.precoMedio)
    }

    @Test
    fun `abastecimento so no comeco do periodo ainda nao da media`() {
        val r = consumoNoPeriodo(lista.take(2), Intervalo(dia(2026, 9, 1), dia(2026, 9, 30)))
        assertNull(r.consumo)
        assertEquals(200, r.kmRodados)
    }

    @Test
    fun `intervalos dos periodos rapidos`() {
        val hoje = dia(2026, 10, 16)
        assertEquals(Intervalo(dia(2026, 9, 17), hoje), intervaloDoPeriodo(Periodo.ULTIMOS_30_DIAS, hoje))
        assertEquals(Intervalo(dia(2026, 10, 1), hoje), intervaloDoPeriodo(Periodo.ESTE_MES, hoje))
        assertEquals(Intervalo(dia(2026, 9, 1), dia(2026, 9, 30)), intervaloDoPeriodo(Periodo.MES_PASSADO, hoje))
        assertEquals(Intervalo(dia(2026, 7, 17), hoje), intervaloDoPeriodo(Periodo.ULTIMOS_3_MESES, hoje))
        assertEquals(Intervalo(dia(2026, 1, 1), hoje), intervaloDoPeriodo(Periodo.ESTE_ANO, hoje))
        // janeiro: "mês passado" é dezembro do ano anterior
        assertEquals(Intervalo(dia(2025, 12, 1), dia(2025, 12, 31)), intervaloDoPeriodo(Periodo.MES_PASSADO, dia(2026, 1, 10)))
    }
}
