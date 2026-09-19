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
            registros = listOf(Registro(motoId = 1, pecaId = 7, kmTroca = 15000, servicoId = null, preco = 60)),
            servicos = listOf(Servico(motoId = 1, custo = 250, kilometragem = 16000, tipoServico = "Revisão", data = 9, local = "Zé")),
            formatarData = { "D$it" },
        )
        assertTrue(csv.contains("Crosser;ABC1D23;2020;16000;D5"))
        assertTrue(csv.contains("Crosser ABC1D23;Óleo, sintético;15000;60;não"))
        assertTrue(csv.contains("Crosser ABC1D23;Revisão;D9;16000;250;Zé"))
        assertTrue(csv.contains("Óleo, sintético;3000"))
    }

    @Test
    fun `moto sem kmAtualizadoEm exporta campo vazio`() {
        val csv = montarExportacao(listOf(Moto(id = 1, modelo = "X", placa = "P", anoFabricacao = 2020, kilometragem = 1)), emptyList(), emptyList(), emptyList()) { "nunca" }
        assertEquals(true, csv.lines().any { it == "X;P;2020;1;" })
    }
}
