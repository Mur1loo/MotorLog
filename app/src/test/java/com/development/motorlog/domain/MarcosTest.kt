package com.development.motorlog.domain

import com.development.motorlog.data.HistoricoKm
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarcosTest {
    private val dia = MILLIS_POR_DIA
    private fun p(km: Int, d: Long) = HistoricoKm(motoId = 1, km = km, data = d * dia)
    private fun data(ano: Int, mes: Int, d: Int) = LocalDate.of(ano, mes, d).toEpochDay() * dia

    @Test
    fun `marcos de km - 5 mil e depois de 10 em 10 mil`() {
        assertEquals(listOf(5_000, 10_000, 20_000, 30_000), marcosDeKm(34_000))
        assertEquals(emptyList<Int>(), marcosDeKm(4_999))
    }

    @Test
    fun `marco de km conta o dia em que foi cruzado`() {
        val pontos = listOf(p(48_800, 10), p(49_600, 12), p(50_150, 14), p(50_900, 16))
        val m = marcosDaMoto("Pretinha", 0, pontos, 20 * dia).single()
        assertEquals("km-50000", m.chave)
        assertEquals("Passou dos 50.000 km!", m.titulo)
        assertEquals(14 * dia, m.data)
    }

    @Test
    fun `km de antes do app nao vira marco`() {
        // cadastrou já com 52 mil: os 50 mil foram antes do app
        assertTrue(marcosDaMoto("x", 0, listOf(p(52_000, 1), p(52_500, 2)), 3 * dia).isEmpty())
    }

    @Test
    fun `aniversarios da chegada`() {
        val hoje = data(2026, 10, 3)
        val marcos = marcosDaMoto("Pretinha", data(2024, 9, 1), emptyList(), hoje)
        assertEquals(listOf("1 ano com a Pretinha", "2 anos com a Pretinha"), marcos.map { it.titulo })
        assertEquals(data(2026, 9, 1), marcos.last().data)
    }

    @Test
    fun `comemora o mais recente dos ultimos 30 dias que nao foi visto`() {
        val hoje = 100 * dia
        val marcos = listOf(
            Marco("km-10000", TipoMarco.KM, "a", 50 * dia, 10_000),
            Marco("km-20000", TipoMarco.KM, "b", 80 * dia, 20_000),
            Marco("aniversario-1", TipoMarco.ANIVERSARIO, "c", 95 * dia, 1),
        )
        assertEquals("aniversario-1", marcoPraComemorar(marcos, hoje, emptySet())?.chave)
        assertEquals("km-20000", marcoPraComemorar(marcos, hoje, setOf("aniversario-1"))?.chave)
        assertNull(marcoPraComemorar(marcos, hoje, setOf("aniversario-1", "km-20000")))   // o de 50 dias atrás já passou
    }
}
