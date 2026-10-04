package com.development.motorlog.domain

import com.development.motorlog.data.Desejo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DesejosTest {
    private val dia = 86_400_000L
    private fun desejo(id: Long, criado: Int, preco: Int = 0, instalado: Int = 0) =
        Desejo(id = id, motoId = 1, nome = "Item $id", preco = preco, criadoEm = criado * dia, instaladoEm = instalado * dia, kmInstalado = if (instalado > 0) 1000 else -1)

    @Test
    fun `separa pendentes e instalados com os totais`() {
        val r = resumirDesejos(
            listOf(desejo(1, 1, 35_000), desejo(2, 5, 8_990), desejo(3, 2, 12_000, instalado = 10), desejo(4, 3, instalado = 20)),
        )
        assertEquals(listOf(2L, 1L), r.pendentes.map { it.id })
        assertEquals(listOf(4L, 3L), r.instalados.map { it.id })
        assertEquals(43_990, r.faltaInvestir)
        assertEquals(12_000, r.investido)
    }

    @Test
    fun `validacao pede nome e preco legivel`() {
        assertTrue(validarDesejo("Baú", "").ok)
        assertTrue(validarDesejo("Baú", "350,00").ok)
        assertFalse(validarDesejo(" ", "").ok)
        assertEquals("Valor inválido, ex.: 89,90", validarDesejo("Baú", "abc").preco)
    }
}
