package com.development.motorlog.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlertasTest {
    private fun rec(nome: String, status: StatusTroca) =
        Recomendacao(pecaId = 1, pecaNome = nome, kmUltimaTroca = null, kmProximaTroca = null, kmRestante = null, statusTroca = status)

    private val dia = MILLIS_POR_DIA

    @Test
    fun `diasEntre conta dias inteiros`() {
        assertEquals(0, diasEntre(10 * dia, 10 * dia))
        assertEquals(3, diasEntre(10 * dia, 13 * dia))
        assertEquals(-1, diasEntre(10 * dia, 9 * dia))
    }

    @Test
    fun `resumirAlertas conta vencidas e perto`() {
        val r = resumirAlertas(listOf(rec("a", StatusTroca.VENCIDA), rec("b", StatusTroca.PERTO), rec("c", StatusTroca.OK), rec("d", StatusTroca.NUNCA_TROCADA)))
        assertEquals(ResumoAlertas(vencidas = 1, perto = 1), r)
        assertEquals(true, r.temAlerta)
        assertEquals(false, resumirAlertas(listOf(rec("c", StatusTroca.OK))).temAlerta)
    }

    @Test
    fun `sem motivo nao ha lembrete`() {
        assertNull(montarLembrete("Crosser", kmAtualizadoEm = 10 * dia, hoje = 11 * dia, recomendacoes = listOf(rec("Óleo", StatusTroca.OK))))
    }

    @Test
    fun `moto anterior a v9 (kmAtualizadoEm zero) nao e cobrada por km`() {
        assertNull(montarLembrete("Crosser", kmAtualizadoEm = 0, hoje = 100 * dia, recomendacoes = emptyList()))
    }

    @Test
    fun `km parado ha 3 dias lembra, ha 2 nao`() {
        assertNull(montarLembrete("Crosser", 10 * dia, 12 * dia, emptyList()))
        assertEquals("Crosser: faz 3 dias que o km não é atualizado", montarLembrete("Crosser", 10 * dia, 13 * dia, emptyList()))
    }

    @Test
    fun `vencida e perto entram no texto`() {
        val texto = montarLembrete("Crosser", 10 * dia, 10 * dia, listOf(rec("Óleo do motor", StatusTroca.VENCIDA), rec("Vela", StatusTroca.PERTO)))
        assertEquals("Crosser: Óleo do motor vencido · 1 troca perto de vencer", texto)
    }

    @Test
    fun `varias vencidas resume com contagem`() {
        val texto = montarLembrete("Crosser", 10 * dia, 10 * dia, listOf(rec("Óleo", StatusTroca.VENCIDA), rec("Vela", StatusTroca.VENCIDA), rec("Pneu", StatusTroca.VENCIDA)))
        assertEquals("Crosser: 3 trocas vencidas (Óleo, Vela…)", texto)
    }
}
