package com.development.motorlog.domain

import com.development.motorlog.data.Cuidado
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IndiceDeCuidadoTest {
    private val dia = MILLIS_POR_DIA
    private val hoje = 400 * dia
    private val kmAtual = 20000
    // as 7 essenciais no catálogo (ids 1..7)
    private val pecas = PECAS_ESSENCIAIS.mapIndexed { i, nome -> Peca(id = i + 1L, nome = nome, intervaloKm = 3000) }

    private fun rec(id: Long, status: StatusTroca, ultima: Int = 18000, proxima: Int = 21000) =
        Recomendacao(id, pecas.find { it.id == id }?.nome ?: "Revisão", ultima, proxima, proxima - kmAtual, status)

    private fun troca(pecaId: Long, diasAtras: Int) = Registro(motoId = 1, pecaId = pecaId, kmTroca = 18000, servicoId = null, data = hoje - diasAtras * dia)
    private fun cuidado(tipo: TipoCuidado, diasAtras: Int, km: Int = kmAtual) = Cuidado(motoId = 1, tipo = tipo.codigo, data = hoje - diasAtras * dia, km = km)

    private fun indice(
        recs: List<Recomendacao> = emptyList(),
        registros: List<Registro> = emptyList(),
        servicos: List<Servico> = emptyList(),
        cuidados: List<Cuidado> = emptyList(),
        diasSemKm: Int = 0,
    ) = calcularIndiceDeCuidado(recs, pecas, registros, servicos, cuidados, kmAtual, hoje - diasSemKm * dia, hoje)

    @Test
    fun `sem historico nenhum nao ha nota`() {
        assertNull(indice())
    }

    @Test
    fun `moto exemplar chega perto de 100`() {
        val i = indice(
            recs = (1L..7L).map { rec(it, StatusTroca.OK) },
            registros = (1L..7L).map { troca(it, 20) },
            cuidados = listOf(cuidado(TipoCuidado.CORRENTE, 2), cuidado(TipoCuidado.CALIBRAGEM, 2), cuidado(TipoCuidado.LAVAGEM_DETALHADA, 5)),
        )!!
        assertEquals(100, i.nota)
        assertEquals("Excelente", i.faixa)
        assertNull(i.dica)
    }

    @Test
    fun `troca vencida derruba a nota e vira a dica`() {
        val recs = listOf(rec(1, StatusTroca.VENCIDA, ultima = 15000, proxima = 18000)) + (2L..7L).map { rec(it, StatusTroca.OK) }
        val i = indice(
            recs = recs,
            registros = (1L..7L).map { troca(it, 20) },
            cuidados = listOf(cuidado(TipoCuidado.CORRENTE, 2), cuidado(TipoCuidado.CALIBRAGEM, 2), cuidado(TipoCuidado.LAVAGEM, 5)),
        )!!
        // óleo 2.000 km atrasado num intervalo de 3.000 (> meio intervalo) = 0 → trocas em dia 85
        assertEquals(85, i.partes.single { it.nome == "Trocas em dia" }.nota)
        assertTrue(i.nota < 100)
        assertEquals("trocar óleo do motor (vencida)", i.dica)
    }

    @Test
    fun `manutencao registrada mede recencia e cobertura das essenciais`() {
        // só o óleo registrado, há 200 dias: recência 40, cobertura 1/7 = 14 → 27
        val i = indice(recs = listOf(rec(1, StatusTroca.OK)), registros = listOf(troca(1, 200)))!!
        val parte = i.partes.single { it.nome == "Manutenção registrada" }
        assertEquals(27, parte.nota)
        assertEquals("registrar a última troca de filtro de óleo", parte.dica)
    }

    @Test
    fun `revisao so entra quando configurada`() {
        val sem = indice(recs = listOf(rec(1, StatusTroca.OK)), registros = listOf(troca(1, 10)))!!
        assertTrue(sem.partes.none { it.nome == "Revisão" })
        val com = indice(recs = listOf(rec(1, StatusTroca.OK), rec(REVISAO_ID, StatusTroca.NUNCA_TROCADA)), registros = listOf(troca(1, 10)))!!
        assertEquals(0, com.partes.single { it.nome == "Revisão" }.nota)
    }

    @Test
    fun `km parado e cuidados atrasados pesam`() {
        val i = indice(
            recs = (1L..7L).map { rec(it, StatusTroca.OK) },
            registros = (1L..7L).map { troca(it, 20) },
            cuidados = listOf(cuidado(TipoCuidado.CORRENTE, 30, km = 19000), cuidado(TipoCuidado.CALIBRAGEM, 20)),
            diasSemKm = 20,
        )!!
        assertEquals(10, i.partes.single { it.nome == "Km em dia" }.nota)
        // corrente 1.000 km atrás (40), calibragem 20 dias (40), sem lavagem (0) → 26
        assertEquals(26, i.partes.single { it.nome == "Cuidados de rotina" }.nota)
        assertEquals("lubrificar a corrente", i.dica)
    }

    @Test
    fun `faixas`() {
        assertEquals("Excelente", faixaDoIndice(85))
        assertEquals("Bem cuidada", faixaDoIndice(70))
        assertEquals("Pede atenção", faixaDoIndice(50))
        assertEquals("Precisa de cuidado", faixaDoIndice(49))
    }
}
