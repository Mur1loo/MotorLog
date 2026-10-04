package com.development.motorlog.domain

import com.development.motorlog.data.HistoricoKm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RitmoTest {
    private val dia = MILLIS_POR_DIA
    private val hoje = 1000 * dia
    private fun ponto(diasAtras: Int, km: Int) = HistoricoKm(motoId = 1, km = km, data = hoje - diasAtras * dia)

    private fun medias(pontos: List<HistoricoKm>, chegouEm: Long = 0, kmChegada: Int = -1) = calcularMediasDeKm(pontos, chegouEm, kmChegada, hoje)

    @Test
    fun `menos de dois pontos - sem media`() {
        assertNull(medias(emptyList()).ultimoMes)
        assertNull(medias(emptyList()).geral)
        assertNull(medias(listOf(ponto(0, 16000))).ultimoMes)
    }

    @Test
    fun `janela menor que 7 dias - sem media`() {
        assertNull(medias(listOf(ponto(6, 15000), ponto(0, 16000))).ultimoMes)
    }

    @Test
    fun `ultimo mes - 600 km em 10 dias = 1800 km por mes`() {
        val m = medias(listOf(ponto(10, 15400), ponto(5, 15700), ponto(0, 16000)))
        assertEquals(1800, m.ultimoMes)
        assertEquals(10, m.diasUltimoMes)
    }

    @Test
    fun `ultimo mes ignora pontos de mais de 30 dias`() {
        assertEquals(1800, medias(listOf(ponto(60, 13000), ponto(10, 15400), ponto(0, 16000))).ultimoMes)
    }

    @Test
    fun `km que nao avancou - sem media`() {
        assertNull(medias(listOf(ponto(10, 16000), ponto(0, 16000))).ultimoMes)
    }

    @Test
    fun `geral desde a chegada - o caso de 10 mil km em 6 meses`() {
        // chegou há 180 dias com 5.000 km; cadastrado no app há 20 dias com 14.000; hoje 15.000
        val m = medias(listOf(ponto(20, 14000), ponto(0, 15000)), chegouEm = hoje - 180 * dia, kmChegada = 5000)
        assertEquals(1666, m.geral)
        assertTrue(m.geralDesdeAChegada)
        assertEquals(1500, m.ultimoMes)   // 1.000 km em 20 dias: o ritmo de agora é menor
    }

    @Test
    fun `sem chegada a media geral parte do cadastro no app`() {
        val m = medias(listOf(ponto(60, 13000), ponto(10, 15400), ponto(0, 16000)))
        assertEquals(1500, m.geral)   // 3.000 km em 60 dias
        assertFalse(m.geralDesdeAChegada)
    }

    @Test
    fun `previsao usa o ultimo mes com 14 dias ou mais, senao a geral`() {
        val cheio = medias(listOf(ponto(20, 14000), ponto(0, 15000)), chegouEm = hoje - 180 * dia, kmChegada = 5000)
        assertEquals(1500, cheio.paraPrevisao)
        val curto = medias(listOf(ponto(10, 14500), ponto(0, 15000)), chegouEm = hoje - 180 * dia, kmChegada = 5000)
        assertEquals(1666, curto.paraPrevisao)   // 10 dias: ainda pouco pra confiar no último mês
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
