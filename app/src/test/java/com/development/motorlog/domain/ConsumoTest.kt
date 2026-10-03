package com.development.motorlog.domain

import com.development.motorlog.data.Abastecimento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsumoTest {
    private fun ab(km: Int, ml: Int, cheio: Boolean = true, valor: Int = 0, id: Long = km.toLong()) =
        Abastecimento(id = id, motoId = 1, km = km, mililitros = ml, valor = valor, tanqueCheio = cheio, data = 0)

    @Test
    fun `tanque cheio a tanque cheio - o exemplo do README`() {
        // cheio aos 16.000 (ponto de partida), cheio aos 16.300 com 7,5 L → 40 km/l
        val t = calcularTrechos(listOf(ab(16000, 6000), ab(16300, 7500)))
        assertEquals(1, t.size)
        assertEquals(300, t[0].km)
        assertEquals(40.0, t[0].kmPorLitro, 0.001)
    }

    @Test
    fun `parcial soma no proximo tanque cheio`() {
        // 16.000 cheio; 16.150 parcial 3 L; 16.400 cheio 7 L → 400 km com 10 L = 40 km/l
        val t = calcularTrechos(listOf(ab(16000, 5000), ab(16150, 3000, cheio = false), ab(16400, 7000)))
        assertEquals(1, t.size)
        assertEquals(10000, t[0].mililitros)
        assertEquals(40.0, t[0].kmPorLitro, 0.001)
    }

    @Test
    fun `parcial antes do primeiro tanque cheio nao entra`() {
        val t = calcularTrechos(listOf(ab(15900, 2000, cheio = false), ab(16000, 5000), ab(16300, 7500)))
        assertEquals(1, t.size)
        assertEquals(7500, t[0].mililitros)
    }

    @Test
    fun `ordem de registro nao importa, vale o km`() {
        val t = calcularTrechos(listOf(ab(16300, 7500), ab(16000, 6000)))
        assertEquals(40.0, t.single().kmPorLitro, 0.001)
    }

    @Test
    fun `trecho absurdo (abastecimento esquecido) fica fora`() {
        // 16.000 → 16.800 com 7,5 L daria 106 km/l: faltou registrar um abastecimento no meio
        assertTrue(calcularTrechos(listOf(ab(16000, 6000), ab(16800, 7500))).isEmpty())
    }

    @Test
    fun `media e km total sobre litros totais, nao media das medias`() {
        // trecho 1: 100 km / 4 L = 25; trecho 2: 400 km / 8 L = 50 → média 500/12 = 41,67 (e não 37,5)
        val r = resumirConsumo(listOf(ab(1000, 5000), ab(1100, 4000), ab(1500, 8000)))
        assertEquals(2, r.trechos)
        assertEquals(500 / 12.0, r.mediaKmPorLitro!!, 0.001)
        assertEquals(50.0, r.ultimoKmPorLitro!!, 0.001)
    }

    @Test
    fun `preco por litro e custo por km de combustivel`() {
        // 7,5 L por R$ 45,00 → R$ 6,00/L; 300 km com R$ 45 → R$ 0,15/km
        val r = resumirConsumo(listOf(ab(16000, 6000, valor = 3600), ab(16300, 7500, valor = 4500)))
        assertEquals(600, r.precoPorLitro)
        assertEquals(0.15, r.custoPorKm!!, 0.0001)
    }

    @Test
    fun `trecho com abastecimento sem valor nao entra no custo, mas entra no consumo`() {
        val r = resumirConsumo(listOf(ab(16000, 6000), ab(16300, 7500, valor = 0)))
        assertNotNull(r.mediaKmPorLitro)
        assertNull(r.custoPorKm)
        assertNull(r.precoPorLitro)
    }

    @Test
    fun `um abastecimento so ainda nao da consumo`() {
        val r = resumirConsumo(listOf(ab(16000, 6000)))
        assertNull(r.mediaKmPorLitro); assertNull(r.ultimoKmPorLitro); assertEquals(0, r.trechos)
    }

    @Test
    fun `le litros com virgula, ponto e ate 3 casas`() {
        assertEquals(8000, lerLitros("8"))
        assertEquals(8500, lerLitros("8,5"))
        assertEquals(8734, lerLitros("8,734"))
        assertEquals(8734, lerLitros("8.734"))
        assertEquals(8500, lerLitros(" 8,5 L"))
        assertNull(lerLitros(""))
        assertNull(lerLitros("0"))
        assertNull(lerLitros("8,7345"))
        assertNull(lerLitros("abc"))
        assertNull(lerLitros("101"))
    }

    @Test
    fun `litros viram texto e voltam iguais`() {
        assertEquals("8,734", litrosParaTexto(8734))
        assertEquals("8,5", litrosParaTexto(8500))
        assertEquals("8", litrosParaTexto(8000))
        listOf(1, 8500, 8734, 12000).forEach { assertEquals(it, lerLitros(litrosParaTexto(it))) }
    }

    @Test
    fun `validacao do abastecimento`() {
        assertTrue(validarAbastecimento("16300", "7,5", "").ok)
        val e = validarAbastecimento("", "", "45,901")
        assertNotNull(e.km); assertEquals("Informe quantos litros entraram", e.litros); assertNotNull(e.valor)
    }
}
