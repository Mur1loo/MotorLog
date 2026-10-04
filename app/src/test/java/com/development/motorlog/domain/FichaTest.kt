package com.development.motorlog.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FichaTest {
    private val vazia = DadosDaFicha(indice = null, kmJuntos = null, kmPorLitro = null, trocas = 0, visitas = 0, cuidados = 0)
    private val indice = IndiceDeCuidado(82, "Bem cuidada", emptyList(), null)

    @Test
    fun `moto sem nada registrado nao tem quadros`() {
        assertEquals(emptyList<QuadroDaFicha>(), quadrosDaFicha(vazia))
    }

    @Test
    fun `km juntos zerado nao vira quadro`() {
        assertEquals(emptyList<QuadroDaFicha>(), quadrosDaFicha(vazia.copy(kmJuntos = 0)))
    }

    @Test
    fun `so entram os quadros com dado, na ordem`() {
        val d = vazia.copy(kmPorLitro = 38.5, visitas = 2, cuidados = 5)
        assertEquals(listOf(QuadroDaFicha.MANUTENCAO, QuadroDaFicha.CONSUMO, QuadroDaFicha.CUIDADOS), quadrosDaFicha(d))
    }

    @Test
    fun `com tudo, fica nos 4 primeiros e o cuidado sai`() {
        val d = DadosDaFicha(indice, kmJuntos = 10_000, kmPorLitro = 38.5, trocas = 12, visitas = 3, cuidados = 20)
        assertEquals(
            listOf(QuadroDaFicha.INDICE, QuadroDaFicha.KM_JUNTOS, QuadroDaFicha.MANUTENCAO, QuadroDaFicha.CONSUMO),
            quadrosDaFicha(d),
        )
    }
}
