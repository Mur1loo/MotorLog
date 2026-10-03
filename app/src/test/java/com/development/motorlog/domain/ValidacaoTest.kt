package com.development.motorlog.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacaoTest {
    @Test
    fun `km obrigatorio, so numeros e sem negativo`() {
        assertNull(erroDeKm("16100"))
        assertNull(erroDeKm(" 0 "))
        assertEquals("Informe o km", erroDeKm(""))
        assertNotNull(erroDeKm("16.100"))
        assertNotNull(erroDeKm("-5"))
    }

    @Test
    fun `moto valida campo a campo e placa nao existe na validacao`() {
        assertTrue(validarMoto("Fan 160", "2020", "16100", "").ok)
        val e = validarMoto("", "20", "", "abc")
        assertNotNull(e.modelo); assertNotNull(e.ano); assertNotNull(e.km); assertNotNull(e.revisao)
        assertFalse(e.ok)
    }

    @Test
    fun `edicao de moto nao cobra km`() {
        assertTrue(validarMoto("Fan 160", "2020", km = null, revisao = "5000").ok)
    }

    @Test
    fun `ano fora da faixa e erro`() {
        assertNotNull(validarMoto("Fan", "1899", "1", "").ano)
        assertNotNull(validarMoto("Fan", "2101", "1", "").ano)
        assertNull(validarMoto("Fan", "2100", "1", "").ano)
    }

    @Test
    fun `servico aceita centavos e nao exige oficina`() {
        assertTrue(validarServico("Revisão", "180,50", "16000").ok)
        val e = validarServico(" ", "", "x")
        assertNotNull(e.tipo); assertEquals("Informe o valor pago", e.custo); assertNotNull(e.km)
        assertEquals("Use só números, ex.: 45,90", validarServico("Pneu", "45,901", "1").custo)
    }

    @Test
    fun `peca exige nome e intervalo maior que zero`() {
        assertTrue(validarPeca("Óleo", "3000").ok)
        assertNotNull(validarPeca("", "3000").nome)
        assertNotNull(validarPeca("Óleo", "0").intervalo)
        assertNotNull(validarPeca("Óleo", "").intervalo)
    }
}
