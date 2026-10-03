package com.development.motorlog.domain

import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportacaoTest {
    @Test
    fun `resolve nomes, formata data pelo formatador dado e escapa o separador`() {
        val csv = montarExportacao(
            motos = listOf(Moto(id = 1, modelo = "Crosser", placa = "ABC1D23", anoFabricacao = 2020, kilometragem = 16000, kmAtualizadoEm = 5)),
            pecas = listOf(Peca(id = 7, nome = "Óleo; sintético", intervaloKm = 3000)),
            registros = listOf(Registro(motoId = 1, pecaId = 7, kmTroca = 15000, servicoId = null, preco = 4590)),
            servicos = listOf(Servico(motoId = 1, custo = 25000, kilometragem = 16000, tipoServico = "Revisão", data = 9, local = "Zé")),
            formatarData = { "D$it" },
        )
        assertTrue(csv.contains("Crosser;ABC1D23;2020;16000;D5;0"))
        assertTrue(csv.contains("Crosser ABC1D23;Óleo, sintético;15000;45,90;não;"))
        assertTrue(csv.contains("Crosser ABC1D23;Revisão;D9;16000;250;Zé"))
        assertTrue(csv.contains("Óleo, sintético;3000"))
    }

    @Test
    fun `moto sem kmAtualizadoEm exporta campo vazio`() {
        val csv = montarExportacao(listOf(Moto(id = 1, modelo = "X", placa = "P", anoFabricacao = 2020, kilometragem = 1)), emptyList(), emptyList(), emptyList()) { "nunca" }
        assertEquals(true, csv.lines().any { it == "X;P;2020;1;;0;-1" })
    }

    @Test
    fun `troca com dia conhecido exporta a data, sem dia fica vazio`() {
        val csv = montarExportacao(
            motos = listOf(Moto(id = 1, modelo = "X", placa = "P", anoFabricacao = 2020, kilometragem = 1, cor = 5)),
            pecas = listOf(Peca(id = 7, nome = "Vela", intervaloKm = 10000)),
            registros = listOf(
                Registro(motoId = 1, pecaId = 7, kmTroca = 100, servicoId = null, preco = 3000, data = 4),
                Registro(motoId = 1, pecaId = 7, kmTroca = 50, servicoId = null, preco = 0),
            ),
            servicos = emptyList(),
        ) { "D$it" }
        assertTrue(csv.lines().contains("X P;Vela;100;30;não;D4"))
        assertTrue(csv.lines().contains("X P;Vela;50;0;não;"))
        assertTrue(csv.lines().contains("X;P;2020;1;;0;5"))
    }
}
