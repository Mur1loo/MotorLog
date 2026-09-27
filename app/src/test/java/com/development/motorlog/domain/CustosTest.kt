package com.development.motorlog.domain

import com.development.motorlog.data.HistoricoKm
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CustosTest {
    private val dia = MILLIS_POR_DIA
    private fun servico(custo: Int, data: Long) = Servico(motoId = 1, custo = custo, kilometragem = 0, tipoServico = "x", data = data, local = "")

    @Test
    fun `gasto total soma oficina e trocas avulsas, ignorando trocas de servico`() {
        val total = gastoTotal(
            listOf(servico(250, 0)),
            listOf(Registro(motoId = 1, pecaId = 1, kmTroca = 1, servicoId = null, preco = 40), Registro(motoId = 1, pecaId = 1, kmTroca = 2, servicoId = 9, preco = 60)),
        )
        assertEquals(290, total)
    }

    @Test
    fun `km rodados desde o primeiro ponto do historico`() {
        val h = listOf(HistoricoKm(motoId = 1, km = 16000, data = 10 * dia), HistoricoKm(motoId = 1, km = 15000, data = 3 * dia))
        assertEquals(1500, kmRodadosNoApp(h, 16500))
        assertEquals(0, kmRodadosNoApp(emptyList(), 16500))
        assertEquals(0, kmRodadosNoApp(h, 14000))   // km corrigido pra baixo não vira negativo
    }

    @Test
    fun `custo por km`() {
        assertEquals(0.42, custoPorKm(gastoTotal = 630, kmRodados = 1500)!!, 0.0001)
        assertNull(custoPorKm(630, 0))
        assertNull(custoPorKm(0, 1500))
    }

    @Test
    fun `gasto no mes conta so servicos do mes corrente`() {
        // 2026-09-19 em dias desde a época
        val hoje = java.time.LocalDate.of(2026, 9, 19).toEpochDay() * dia
        val inicioSet = java.time.LocalDate.of(2026, 9, 1).toEpochDay() * dia
        assertEquals(inicioSet, inicioDoMes(hoje))
        val total = gastoNoMes(listOf(servico(250, hoje), servico(100, inicioSet), servico(999, inicioSet - dia)), emptyList(), hoje)
        assertEquals(350, total)
    }

    @Test
    fun `gasto no mes soma trocas por conta propria com dia conhecido`() {
        val hoje = java.time.LocalDate.of(2026, 9, 19).toEpochDay() * dia
        val inicioSet = java.time.LocalDate.of(2026, 9, 1).toEpochDay() * dia
        fun troca(preco: Int, data: Long, servicoId: Long? = null) =
            Registro(motoId = 1, pecaId = 1, kmTroca = 1, servicoId = servicoId, preco = preco, data = data)
        val total = gastoNoMes(
            listOf(servico(250, hoje)),
            listOf(
                troca(40, hoje),                    // entra
                troca(30, inicioSet),               // entra (1º dia do mês)
                troca(999, inicioSet - dia),        // mês passado
                troca(777, 0),                      // dia desconhecido
                troca(555, hoje, servicoId = 9),    // já está no custo do serviço
            ),
            hoje,
        )
        assertEquals(320, total)
    }
}
