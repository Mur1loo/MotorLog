package com.development.motorlog.domain

import com.development.motorlog.data.HistoricoKm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RitmoTest {
    private val dia = MILLIS_POR_DIA
    private val hoje = 1000 * dia
    private fun ponto(diasAtras: Int, km: Int) = HistoricoKm(motoId = 1, km = km, data = hoje - diasAtras * dia)

    @Test
    fun `menos de dois pontos - sem ritmo`() {
        assertNull(calcularRitmoKmMes(emptyList(), hoje))
        assertNull(calcularRitmoKmMes(listOf(ponto(0, 16000)), hoje))
    }

    @Test
    fun `janela menor que 7 dias - sem ritmo`() {
        assertNull(calcularRitmoKmMes(listOf(ponto(6, 15000), ponto(0, 16000)), hoje))
    }

    @Test
    fun `600 km em 10 dias = 1800 km por mes`() {
        assertEquals(1800, calcularRitmoKmMes(listOf(ponto(10, 15400), ponto(5, 15700), ponto(0, 16000)), hoje))
    }

    @Test
    fun `pontos fora da janela de 90 dias sao ignorados`() {
        // sem o filtro, 100 dias e 3000 km dariam 900/mês; com o filtro só os 2 últimos contam
        assertEquals(1800, calcularRitmoKmMes(listOf(ponto(100, 13000), ponto(10, 15400), ponto(0, 16000)), hoje))
    }

    @Test
    fun `km que nao avancou - sem ritmo`() {
        assertNull(calcularRitmoKmMes(listOf(ponto(10, 16000), ponto(0, 16000)), hoje))
    }

    @Test
    fun `estimativa de dias`() {
        assertEquals(6, estimarDiasAteTroca(kmRestante = 400, ritmoKmMes = 1800))
        assertNull(estimarDiasAteTroca(kmRestante = -100, ritmoKmMes = 1800))
        assertNull(estimarDiasAteTroca(kmRestante = 400, ritmoKmMes = null))
        assertNull(estimarDiasAteTroca(kmRestante = null, ritmoKmMes = 1800))
    }

    @Test
    fun `descreverDias`() {
        assertEquals("hoje", descreverDias(0))
        assertEquals("~1 dia", descreverDias(1))
        assertEquals("~45 dias", descreverDias(45))
        assertEquals("~3 meses", descreverDias(95))
    }
}
