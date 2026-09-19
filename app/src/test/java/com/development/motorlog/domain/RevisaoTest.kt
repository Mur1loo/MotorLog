package com.development.motorlog.domain

import com.development.motorlog.data.Servico
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RevisaoTest {
    private fun servico(tipo: String, km: Int) = Servico(motoId = 1, custo = 0, kilometragem = km, tipoServico = tipo, data = 0, local = "")

    @Test
    fun `sem intervalo configurado nao ha item de revisao`() {
        assertNull(recomendacaoDeRevisao(16000, 0, listOf(servico("Revisão", 12000))))
    }

    @Test
    fun `sem revisao registrada vira NUNCA_TROCADA`() {
        val r = recomendacaoDeRevisao(16000, 5000, listOf(servico("Troca de óleo", 12000)))!!
        assertEquals(StatusTroca.NUNCA_TROCADA, r.statusTroca)
        assertEquals(REVISAO_ID, r.pecaId)
    }

    @Test
    fun `usa a revisao mais recente, reconhecendo REVISAO sem acento e minuscula`() {
        val r = recomendacaoDeRevisao(16000, 5000, listOf(servico("revisao geral", 10000), servico("REVISÃO", 12000), servico("Pneu", 15000)))!!
        assertEquals(12000, r.kmUltimaTroca)
        assertEquals(17000, r.kmProximaTroca)
        assertEquals(1000, r.kmRestante)
        assertEquals(StatusTroca.PERTO, r.statusTroca)   // 1000 <= 5000/4
    }

    @Test
    fun `comRevisao insere na ordem de urgencia`() {
        val oleo = Recomendacao(1, "Óleo", 12000, 15000, -1000, StatusTroca.VENCIDA)
        val vela = Recomendacao(2, "Vela", 10000, 20000, 4000, StatusTroca.OK)
        val rev = recomendacaoDeRevisao(16000, 5000, listOf(servico("Revisão", 12000)))
        assertEquals(listOf("Óleo", REVISAO_NOME, "Vela"), comRevisao(listOf(oleo, vela), rev).map { it.pecaNome })
        assertEquals(listOf(oleo, vela), comRevisao(listOf(oleo, vela), null))
    }
}
