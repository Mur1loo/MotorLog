package com.development.motorlog.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DinheiroTest {
    @Test
    fun `le reais inteiros e com centavos`() {
        assertEquals(4500, lerReais("45"))
        assertEquals(4590, lerReais("45,90"))
        assertEquals(4590, lerReais("45,9"))
        assertEquals(50, lerReais(",50"))
        assertEquals(0, lerReais("0"))
    }

    @Test
    fun `aceita R$, espacos e ponto de milhar`() {
        assertEquals(4590, lerReais("R$ 45,90"))
        assertEquals(123450, lerReais("1.234,50"))
        assertEquals(123400, lerReais("1.234"))
        assertEquals(100000000, lerReais("1.000.000"))
    }

    @Test
    fun `ponto com 1 ou 2 digitos depois e decimal`() {
        assertEquals(4590, lerReais("45.90"))
        assertEquals(4550, lerReais("45.5"))
    }

    @Test
    fun `texto invalido ou vazio vira null`() {
        assertNull(lerReais(""))
        assertNull(lerReais("  "))
        assertNull(lerReais("abc"))
        assertNull(lerReais("45,901"))
        assertNull(lerReais("-10"))
        assertNull(lerReais(","))
        assertNull(lerReais("99999999999"))
    }

    @Test
    fun `centavos viram texto de campo`() {
        assertEquals("45,90", reaisParaTexto(4590))
        assertEquals("45", reaisParaTexto(4500))
        assertEquals("0,05", reaisParaTexto(5))
        assertEquals("1234,50", reaisParaTexto(123450))
    }

    @Test
    fun `ida e volta nao perde nada`() {
        listOf(0, 5, 99, 4500, 4590, 123450).forEach { assertEquals(it, lerReais(reaisParaTexto(it))) }
    }
}
