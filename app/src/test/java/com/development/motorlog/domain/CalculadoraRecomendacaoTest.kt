package com.development.motorlog.domain

import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculadoraRecomendacaoTest {

    private val oleo = Peca(id = 1, nome = "Óleo do motor", intervaloKm = 3000)
    private val vela = Peca(id = 2, nome = "Vela", intervaloKm = 10000)

    private fun troca(peca: Peca, km: Int) =
        Registro(motoId = 1, pecaId = peca.id, kmTroca = km, servicoId = null)

    private fun unica(kmAtual: Int, vararg registros: Registro) =
        calcularRecomendacoes(kmAtual, listOf(oleo), registros.toList()).single()

    @Test
    fun `peca nunca trocada vira NUNCA_TROCADA com km nulos`() {
        val rec = unica(kmAtual = 16000)
        assertEquals(StatusTroca.NUNCA_TROCADA, rec.statusTroca)
        assertNull(rec.kmProximaTroca)
        assertNull(rec.kmRestante)
        assertEquals(oleo.id, rec.pecaId)
        assertEquals(oleo.nome, rec.pecaNome)
    }

    @Test
    fun `varias trocas da mesma peca - vale a de maior km, nao a ultima inserida`() {
        val rec = unica(16000, troca(oleo, 15000), troca(oleo, 9000), troca(oleo, 12000))
        assertEquals(18000, rec.kmProximaTroca)
        assertEquals(2000, rec.kmRestante)
        assertEquals(StatusTroca.OK, rec.statusTroca)
    }

    @Test
    fun `registros de outra peca sao ignorados`() {
        val rec = unica(16000, troca(vela, 15900))
        assertEquals(StatusTroca.NUNCA_TROCADA, rec.statusTroca)
    }

    @Test
    fun `limite PERTO - um quarto do intervalo entra, um km a mais fica OK`() {
        // intervalo 3000 -> PERTO quando faltam <= 750 km
        assertEquals(StatusTroca.PERTO, unica(15000 + 3000 - 750, troca(oleo, 15000)).statusTroca)
        assertEquals(StatusTroca.OK, unica(15000 + 3000 - 751, troca(oleo, 15000)).statusTroca)
    }

    @Test
    fun `limite VENCIDA - faltando 1 km ainda e PERTO, passou 1 km e VENCIDA`() {
        assertEquals(StatusTroca.PERTO, unica(17999, troca(oleo, 15000)).statusTroca)
        val vencida = unica(18001, troca(oleo, 15000))
        assertEquals(StatusTroca.VENCIDA, vencida.statusTroca)
        assertEquals(-1, vencida.kmRestante)
    }

    @Test
    fun `vence exatamente agora - kmRestante zero e VENCIDA`() {
        val rec = unica(18000, troca(oleo, 15000))
        assertEquals(0, rec.kmRestante)
        assertEquals(StatusTroca.VENCIDA, rec.statusTroca)
    }

    @Test
    fun `ordenacao por urgencia - mais vencida primeiro, nunca trocada por ultimo`() {
        val pneu = Peca(id = 3, nome = "Pneu", intervaloKm = 12000)
        val bateria = Peca(id = 4, nome = "Bateria", intervaloKm = 30000)
        val recs = calcularRecomendacoes(
            kmAtual = 20000,
            pecas = listOf(bateria, oleo, vela, pneu),
            registros = listOf(
                troca(oleo, 15000),   // próxima 18000 -> -2000 (VENCIDA)
                troca(vela, 10500),   // próxima 20500 -> +500  (PERTO)
                troca(pneu, 5000),    // próxima 17000 -> -3000 (VENCIDA, mais atrasada)
            ),
        )
        assertEquals(listOf("Pneu", "Óleo do motor", "Vela", "Bateria"), recs.map { it.pecaNome })
        assertEquals(
            listOf(StatusTroca.VENCIDA, StatusTroca.VENCIDA, StatusTroca.PERTO, StatusTroca.NUNCA_TROCADA),
            recs.map { it.statusTroca },
        )
    }

    @Test
    fun `lista de pecas vazia devolve lista vazia`() {
        assertEquals(emptyList<Recomendacao>(), calcularRecomendacoes(1000, emptyList(), listOf(troca(oleo, 500))))
    }
}
