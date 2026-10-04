package com.development.motorlog.domain

import com.development.motorlog.data.Abastecimento
import com.development.motorlog.data.Cuidado
import com.development.motorlog.data.Desejo
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
            motos = listOf(Moto(id = 1, modelo = "Crosser", placa = "ABC1D23", anoFabricacao = 2020, kilometragem = 16000, kmAtualizadoEm = 5 * dia, intervaloRevisaoKm = 5000)),
            pecas = listOf(Peca(id = 7, nome = "Óleo do motor", intervaloKm = 3000), Peca(id = 8, nome = "Vela", intervaloKm = 10000)),
            registros = listOf(
                Registro(motoId = 1, pecaId = 7, kmTroca = 12000, servicoId = null, preco = 0),
                Registro(motoId = 1, pecaId = 7, kmTroca = 16000, servicoId = 3, preco = 6090),
            ),
            servicos = listOf(Servico(id = 3, motoId = 1, custo = 25000, kilometragem = 16000, tipoServico = "Revisão", data = 9 * dia, local = "Zé")),
            formatarData = ::fmt,
        )
        val d = lerExportacao(csv, ::parse)

        assertEquals(listOf(MotoImportada("Crosser", "ABC1D23", 2020, 16000, 5 * dia, 5000)), d.motos)
        assertEquals(2, d.trocas.size)
        assertEquals(TrocaImportada("Crosser ABC1D23", "Óleo do motor", 12000, 0, emServico = false), d.trocas[0])
        assertEquals(TrocaImportada("Crosser ABC1D23", "Óleo do motor", 16000, 6090, emServico = true), d.trocas[1])
        assertEquals(listOf(ServicoImportado("Crosser ABC1D23", "Revisão", 9 * dia, 16000, 25000, "Zé")), d.servicos)
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

    @Test
    fun `data da troca e cor da moto fazem ida e volta, backup antigo sem as colunas continua valendo`() {
        val csv = montarExportacao(
            motos = listOf(Moto(id = 1, modelo = "Fan", placa = "XYZ", anoFabricacao = 2019, kilometragem = 30000, cor = 6)),
            pecas = listOf(Peca(id = 7, nome = "Vela", intervaloKm = 10000)),
            registros = listOf(Registro(motoId = 1, pecaId = 7, kmTroca = 29000, servicoId = null, preco = 3500, data = 12 * dia)),
            servicos = emptyList(),
            formatarData = ::fmt,
        )
        val d = lerExportacao(csv, ::parse)
        assertEquals(6, d.motos.single().cor)
        assertEquals(TrocaImportada("Fan XYZ", "Vela", 29000, 3500, emServico = false, data = 12 * dia), d.trocas.single())

        // formato da v10: MOTOS com 6 colunas, TROCAS com 5
        val antigo = "MOTOS\nmodelo;placa;ano;km_atual;km_atualizado_em;revisao_a_cada_km\nFan;XYZ;2019;30000;;0\n" +
            "TROCAS\nmoto;peca;km_troca;preco;em_servico\nFan XYZ;Vela;29000;35;não\n"
        val v = lerExportacao(antigo, ::parse)
        assertEquals(-1, v.motos.single().cor)
        assertEquals(null, v.trocas.single().data)
        assertEquals("backup de antes dos centavos: 35 = R$ 35,00", 3500, v.trocas.single().preco)
        assertTrue(v.avisos.isEmpty())
    }

    @Test
    fun `abastecimentos fazem ida e volta, backup sem a secao continua valendo`() {
        val csv = montarExportacao(
            motos = listOf(Moto(id = 1, modelo = "Fan", placa = "XYZ", anoFabricacao = 2019, kilometragem = 30000)),
            pecas = emptyList(),
            registros = emptyList(),
            servicos = emptyList(),
            abastecimentos = listOf(
                Abastecimento(motoId = 1, km = 29700, mililitros = 8734, valor = 5240, tanqueCheio = true, data = 3 * dia),
                Abastecimento(motoId = 1, km = 30000, mililitros = 3000, valor = 0, tanqueCheio = false, data = 4 * dia),
            ),
            formatarData = ::fmt,
        )
        assertTrue(csv.contains("Fan XYZ;29700;8,734;52,40;sim;D3"))
        val d = lerExportacao(csv, ::parse)
        assertEquals(
            listOf(
                AbastecimentoImportado("Fan XYZ", 29700, 8734, 5240, tanqueCheio = true, data = 3 * dia),
                AbastecimentoImportado("Fan XYZ", 30000, 3000, 0, tanqueCheio = false, data = 4 * dia),
            ),
            d.abastecimentos,
        )
        assertTrue(d.avisos.isEmpty())
        assertTrue(lerExportacao("MOTOS\nmodelo;placa;ano;km_atual\nFan;XYZ;2019;30000\n", ::parse).abastecimentos.isEmpty())
    }

    @Test
    fun `apelido e chegada fazem ida e volta`() {
        val csv = montarExportacao(
            motos = listOf(Moto(id = 1, modelo = "Fan", placa = "XYZ", anoFabricacao = 2019, kilometragem = 30000, apelido = "Pretinha", chegouEm = 2 * dia, kmChegada = 1200)),
            pecas = emptyList(), registros = emptyList(), servicos = emptyList(), formatarData = ::fmt,
        )
        val m = lerExportacao(csv, ::parse).motos.single()
        assertEquals("Pretinha", m.apelido); assertEquals(2 * dia, m.chegouEm); assertEquals(1200, m.kmChegada)
        assertEquals(null, m.vendidaEm)
    }

    @Test
    fun `desejos fazem ida e volta`() {
        val csv = montarExportacao(
            motos = listOf(Moto(id = 1, modelo = "Fan", placa = "XYZ", anoFabricacao = 2019, kilometragem = 30000)),
            pecas = emptyList(), registros = emptyList(), servicos = emptyList(),
            desejos = listOf(
                Desejo(motoId = 1, nome = "Baú; 41 L", preco = 35_090, nota = "preto", criadoEm = 3 * dia, instaladoEm = 8 * dia, kmInstalado = 29_000),
                Desejo(motoId = 1, nome = "Viseira", criadoEm = 4 * dia),
            ),
            formatarData = ::fmt,
        )
        assertEquals(
            listOf(
                DesejoImportado("Fan XYZ", "Baú, 41 L", 35_090, "preto", 3 * dia, 8 * dia, 29_000),
                DesejoImportado("Fan XYZ", "Viseira", 0, "", 4 * dia, null, -1),
            ),
            lerExportacao(csv, ::parse).desejos,
        )
    }

    @Test
    fun `moto vendida faz ida e volta`() {
        val csv = montarExportacao(
            motos = listOf(Moto(id = 1, modelo = "Fan", placa = "XYZ", anoFabricacao = 2019, kilometragem = 30000, vendidaEm = 9 * dia)),
            pecas = emptyList(), registros = emptyList(), servicos = emptyList(), formatarData = ::fmt,
        )
        assertEquals(9 * dia, lerExportacao(csv, ::parse).motos.single().vendidaEm)
    }

    @Test
    fun `cuidados fazem ida e volta`() {
        val csv = montarExportacao(
            motos = listOf(Moto(id = 1, modelo = "Fan", placa = "XYZ", anoFabricacao = 2019, kilometragem = 30000)),
            pecas = emptyList(), registros = emptyList(), servicos = emptyList(),
            cuidados = listOf(Cuidado(motoId = 1, tipo = "lavagem", data = 7 * dia, km = 29900, nota = "com cera; brilhando")),
            formatarData = ::fmt,
        )
        assertEquals(listOf(CuidadoImportado("Fan XYZ", "lavagem", 7 * dia, 29900, "com cera, brilhando")), lerExportacao(csv, ::parse).cuidados)
    }
}
