package com.development.motorlog.domain

import com.development.motorlog.data.HistoricoKm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegistrosDeKmTest {
    private val dia = 86_400_000L
    private fun ponto(id: Long, d: Int, km: Int) = HistoricoKm(id = id, motoId = 1, km = km, data = d * dia)
    private val pontos = listOf(ponto(1, 10, 15_000), ponto(2, 20, 150_000), ponto(3, 30, 15_600))
    private fun fmt(d: Long) = "D${d / dia}"

    @Test
    fun `mais recente e o do ultimo dia`() {
        assertEquals(3L, pontoMaisRecente(pontos)?.id)
        assertNull(pontoMaisRecente(emptyList()))
    }

    @Test
    fun `km corrigido entre os vizinhos nao tem aviso`() {
        assertNull(avisoDeKmForaDeOrdem(pontos, pontos[1], 15_300, ::fmt))
    }

    @Test
    fun `km abaixo do anterior ou acima do seguinte avisa`() {
        assertEquals("Fica abaixo do registro de D10 (15.000 km)", avisoDeKmForaDeOrdem(pontos, pontos[1], 14_000, ::fmt))
        assertEquals("Fica acima do registro de D30 (15.600 km)", avisoDeKmForaDeOrdem(pontos, pontos[1], 150_000, ::fmt))
    }

    @Test
    fun `km rodados desde o registro anterior`() {
        assertEquals(mapOf(2L to 135_000, 3L to -134_400), kmDesdeOAnterior(pontos))
    }
}
