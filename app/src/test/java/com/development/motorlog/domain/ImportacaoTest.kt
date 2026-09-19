package com.development.motorlog.domain

import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportacaoTest {
    private val dia = MILLIS_POR_DIA
    // formatador/parser de mentira, consistentes entre si: "D<n>" ↔ n dias
    private fun fmt(millis: Long) = "D${millis / dia}"
    private fun parse(s: String) = s.removePrefix("D").toLongOrNull()?.let { it * dia }

    @Test
    fun `ida e volta - o que exporta, importa igual`() {
        val csv = montarExportacao(
            motos = listOf(Moto(id = 1, modelo = "Crosser", placa = "ABC1D23", anoFabricacao = 2020, kilometragem = 16000, kmAtualizadoEm = 5 * dia)),
            pecas = listOf(Peca(id = 7, nome = "Óleo do motor", intervaloKm = 3000), Peca(id = 8, nome = "Vela", intervaloKm = 10000)),
            registros = listOf(
                Registro(motoId = 1, pecaId = 7, kmTroca = 12000, servicoId = null, preco = 0),
                Registro(motoId = 1, pecaId = 7, kmTroca = 16000, servicoId = 3, preco = 60),
            ),
            servicos = listOf(Servico(id = 3, motoId = 1, custo = 250, kilometragem = 16000, tipoServico = "Revisão", data = 9 * dia, local = "Zé")),
            formatarData = ::fmt,
        )
        val d = lerExportacao(csv, ::parse)

        assertEquals(listOf(MotoImportada("Crosser", "ABC1D23", 2020, 16000, 5 * dia)), d.motos)
        assertEquals(2, d.trocas.size)
        assertEquals(TrocaImportada("Crosser ABC1D23", "Óleo do motor", 12000, 0, emServico = false), d.trocas[0])
        assertEquals(TrocaImportada("Crosser ABC1D23", "Óleo do motor", 16000, 60, emServico = true), d.trocas[1])
        assertEquals(listOf(ServicoImportado("Crosser ABC1D23", "Revisão", 9 * dia, 16000, 250, "Zé")), d.servicos)
        assertEquals(listOf(PecaImportada("Óleo do motor", 3000), PecaImportada("Vela", 10000)), d.pecas)
        assertTrue(d.avisos.isEmpty())
    }

    @Test
    fun `linha estranha vira aviso nao erro e CRLF com espacos e tolerado`() {
        val csv = "MotorLog\r\n\r\nMOTOS\r\nmodelo;placa;ano;km_atual;km_atualizado_em\r\n Fan ; XYZ ; 2019 ; 30000 ; \r\nlixo aqui\r\n"
        val d = lerExportacao(csv) { null }
        assertEquals(listOf(MotoImportada("Fan", "XYZ", 2019, 30000, null)), d.motos)
        assertEquals(1, d.avisos.size)
    }

    @Test
    fun `texto sem secoes e vazio`() {
        assertTrue(lerExportacao("qualquer coisa") { null }.vazio)
    }
}
