package com.development.motorlog.domain

import com.development.motorlog.data.Moto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DespedidaTest {
    private val dia = 86_400_000L
    private val hoje = 20_000L * dia
    private fun moto(id: Long, vendidaEm: Long = 0) = Moto(id = id, modelo = "CG $id", placa = "", anoFabricacao = 2020, kilometragem = 1000, vendidaEm = vendidaEm)

    @Test
    fun `moto sem data de venda esta na garagem`() {
        assertFalse(estaVendida(moto(1)))
        assertTrue(estaVendida(moto(1, vendidaEm = hoje)))
    }

    @Test
    fun `a historia termina na venda`() {
        assertEquals(hoje, fimDaHistoria(moto(1), hoje))
        assertEquals(hoje - 30 * dia, fimDaHistoria(moto(1, vendidaEm = hoje - 30 * dia), hoje))
        // venda com data no futuro (relógio errado) não passa de hoje
        assertEquals(hoje, fimDaHistoria(moto(1, vendidaEm = hoje + dia), hoje))
    }

    @Test
    fun `garagem separa ativas das lembrancas, venda mais recente primeiro`() {
        val g = separarGaragem(listOf(moto(1), moto(2, vendidaEm = hoje - 90 * dia), moto(3), moto(4, vendidaEm = hoje - dia)))
        assertEquals(listOf(1L, 3L), g.ativas.map { it.id })
        assertEquals(listOf(4L, 2L), g.lembrancas.map { it.id })
    }
}
