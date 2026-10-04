package com.development.motorlog.domain

import com.development.motorlog.data.Abastecimento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsumoTest {
    private fun ab(km: Int, ml: Int, cheio: Boolean = false, valor: Int = 0, id: Long = km.toLong()) =
        Abastecimento(id = id, motoId = 1, km = km, mililitros = ml, valor = valor, tanqueCheio = cheio, data = 0)

    @Test
    fun `so com parciais - km do 1o ao ultimo sobre os litros depois do 1o`() {
        // 16.000 (5 L) → 16.150 (4 L) → 16.320 (4 L): 320 km com 8 L = 40 km/l, aproximada
        val e = estimarConsumo(listOf(ab(16000, 5000), ab(16150, 4000), ab(16320, 4000)))!!
        assertEquals(320, e.km)
        assertEquals(8000, e.mililitros)
        assertEquals(40.0, e.kmPorLitro, 0.001)
        assertEquals(Confianca.APROXIMADA, e.confianca)
    }

    @Test
    fun `com litros suficientes a confianca fica boa`() {
        // 16 abastecimentos de 4 L a cada 160 km: 60 L depois do 1º
        val lista = (0..15).map { ab(10000 + it * 160, 4000) }
        val e = estimarConsumo(lista)!!
        assertEquals(Confianca.BOA, e.confianca)
        assertEquals(40.0, e.kmPorLitro, 0.001)
    }

    @Test
    fun `dois tanques cheios dao a conta exata, mesmo com parciais no meio`() {
        // cheio 16.000 → parcial 3 L em 16.150 → cheio 16.400 com 7 L: 400 km / 10 L = 40, exata
        val e = estimarConsumo(listOf(ab(15900, 2000), ab(16000, 5000, cheio = true), ab(16150, 3000), ab(16400, 7000, cheio = true)))!!
        assertEquals(Confianca.EXATA, e.confianca)
        assertEquals(400, e.km)
        assertEquals(40.0, e.kmPorLitro, 0.001)
    }

    @Test
    fun `poucos abastecimentos ou pouco km ainda nao dao media`() {
        assertNull(estimarConsumo(listOf(ab(16000, 5000), ab(16300, 7500))))                     // só 2
        assertNull(estimarConsumo(listOf(ab(16000, 5000), ab(16050, 1000), ab(16100, 1000))))   // 100 km
        assertEquals(1, resumirConsumo(listOf(ab(16000, 5000), ab(16300, 7500))).faltamAbastecimentos)
    }

    @Test
    fun `ordem de registro nao importa, vale o km`() {
        val e = estimarConsumo(listOf(ab(16320, 4000), ab(16000, 5000), ab(16150, 4000)))!!
        assertEquals(40.0, e.kmPorLitro, 0.001)
    }

    @Test
    fun `valor absurdo (abastecimento esquecido ou km errado) nao aparece`() {
        // 16.000 → 17.000 com 6 L = 166 km/l
        assertNull(estimarConsumo(listOf(ab(16000, 5000), ab(16500, 3000), ab(17000, 3000))))
    }

    @Test
    fun `media recente usa os ultimos abastecimentos`() {
        // 4 abastecimentos rendendo 40 km/l, depois 6 rendendo 30 km/l (120 km por 4 L)
        val antes = (0..3).map { ab(10000 + it * 160, 4000) }
        val depois = (1..6).map { ab(10480 + it * 120, 4000) }
        val r = resumirConsumo(antes + depois)
        assertNotNull(r.recente)
        assertEquals(30.0, r.recente!!.kmPorLitro, 0.001)
        assertTrue(r.media!!.kmPorLitro > r.recente!!.kmPorLitro)
    }

    @Test
    fun `sem mais de 6 abastecimentos nao ha media recente separada`() {
        assertNull(resumirConsumo((0..5).map { ab(10000 + it * 160, 4000) }).recente)
    }

    @Test
    fun `preco do litro e custo por km pelo preco medio`() {
        // 4 L por R$ 24,00 (R$ 6/L) em todos; 40 km/l → R$ 0,15 por km
        val r = resumirConsumo((0..3).map { ab(10000 + it * 160, 4000, valor = 2400) })
        assertEquals(600, r.precoPorLitro)
        assertEquals(0.15, r.custoPorKm!!, 0.0001)
    }

    @Test
    fun `alguns sem valor ainda dao custo pelo preco medio dos que tem`() {
        val r = resumirConsumo(listOf(ab(10000, 4000), ab(10160, 4000, valor = 2400), ab(10320, 4000)))
        assertEquals(0.15, r.custoPorKm!!, 0.0001)
    }

    @Test
    fun `sem valor nenhum nao ha custo nem preco`() {
        val r = resumirConsumo((0..3).map { ab(10000 + it * 160, 4000) })
        assertNull(r.custoPorKm); assertNull(r.precoPorLitro)
    }

    @Test
    fun `litros pelo valor pago`() {
        // R$ 30,00 com o litro a R$ 6,29 → 4,769 L
        assertEquals(4769, litrosPeloValor(3000, 629))
        assertEquals(5000, litrosPeloValor(3000, 600))
        assertNull(litrosPeloValor(0, 600))
        assertNull(litrosPeloValor(3000, 0))
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
    fun `validacao do abastecimento por litros e por valor`() {
        assertTrue(validarAbastecimento("16300", "7,5", "").ok)
        val e = validarAbastecimento("", "", "45,901")
        assertNotNull(e.km); assertEquals("Informe quantos litros entraram", e.litros); assertNotNull(e.valor)
        assertTrue(validarAbastecimentoPorValor("16300", "30", "6,29").ok)
        val v = validarAbastecimentoPorValor("16300", "", "")
        assertEquals("Informe o valor pago", v.valor); assertNotNull(v.preco)
        assertNotNull(validarAbastecimentoPorValor("16300", "1000", "6").valor)   // 166 L
    }
}
