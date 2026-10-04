package com.development.motorlog.domain

import com.development.motorlog.data.Cuidado
import com.development.motorlog.data.Desejo
import com.development.motorlog.data.FotoMoto
import com.development.motorlog.data.HistoricoKm
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Passeio
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RetrospectivaTest {
    private fun dia(a: Int, m: Int, d: Int) = LocalDate.of(a, m, d).toEpochDay() * MILLIS_POR_DIA
    private val moto = Moto(id = 1, modelo = "Fan", placa = "", anoFabricacao = 2020, kilometragem = 30_000, apelido = "Pretinha")
    private fun km(id: Long, data: Long, km: Int) = HistoricoKm(id = id, motoId = 1, km = km, data = data)

    @Test
    fun `km do ano parte do ultimo registro antes de janeiro`() {
        val pontos = listOf(km(1, dia(2025, 11, 20), 10_000), km(2, dia(2026, 3, 1), 12_000), km(3, dia(2026, 12, 20), 28_000), km(4, dia(2027, 1, 5), 28_500))
        assertEquals(18_000, kmRodadosNoAno(pontos, 2026))
        assertEquals(500, kmRodadosNoAno(pontos, 2027))
        assertNull(kmRodadosNoAno(pontos, 2024))
    }

    @Test
    fun `sem registro antes, parte da chegada no ano ou do primeiro do ano`() {
        val pontos = listOf(km(1, dia(2026, 5, 1), 5_000), km(2, dia(2026, 9, 1), 9_000))
        assertEquals(6_000, kmRodadosNoAno(pontos, 2026, chegouEm = dia(2026, 4, 1), kmChegada = 3_000))
        assertEquals(4_000, kmRodadosNoAno(pontos, 2026))
    }

    @Test
    fun `junta o ano inteiro e deixa o resto de fora`() {
        val dados = DadosDaMoto(
            moto = moto,
            historicoKm = listOf(km(1, dia(2025, 12, 31), 10_000), km(2, dia(2026, 12, 1), 25_000)),
            registros = listOf(
                Registro(id = 1, motoId = 1, pecaId = 7, kmTroca = 12_000, servicoId = null, preco = 4_590, data = dia(2026, 2, 1)),
                Registro(id = 2, motoId = 1, pecaId = 8, kmTroca = 20_000, servicoId = 5, preco = 0, data = 0),
                Registro(id = 3, motoId = 1, pecaId = 7, kmTroca = 9_000, servicoId = null, preco = 4_000, data = dia(2025, 6, 1)),
            ),
            servicos = listOf(Servico(id = 5, motoId = 1, custo = 25_000, kilometragem = 20_000, tipoServico = "Revisão", data = dia(2026, 8, 1), local = "")),
            cuidados = listOf(
                Cuidado(motoId = 1, tipo = "lavagem", data = dia(2026, 1, 10), km = 10_100),
                Cuidado(motoId = 1, tipo = "corrente", data = dia(2026, 1, 11), km = 10_200),
            ),
            fotos = listOf(
                FotoMoto(id = 1, motoId = 1, arquivo = "a.jpg", data = dia(2026, 10, 1), km = 22_000),
                FotoMoto(id = 2, motoId = 1, arquivo = "b.jpg", data = dia(2026, 3, 1), km = 13_000, legenda = "Serra"),
            ),
            roles = listOf(Passeio(id = 1, motoId = 1, destino = "Praia", data = dia(2026, 7, 1), kmSaida = 18_000, kmChegada = 18_640)),
            desejos = listOf(Desejo(id = 1, motoId = 1, nome = "Baú", criadoEm = dia(2025, 1, 1), instaladoEm = dia(2026, 4, 1), kmInstalado = 14_000)),
        )
        val r = montarRetrospectiva(2026, dados)
        assertEquals(15_000, r.kmRodados)
        assertEquals(2, r.trocas)
        assertEquals(1, r.visitas)
        assertEquals(29_590, r.gastoManutencao)
        assertEquals(2, r.cuidados)
        assertEquals(1, r.lavagens)
        assertEquals(listOf("b.jpg", "a.jpg"), r.fotos.map { it.arquivo })
        assertEquals(640, r.kmEmRoles)
        assertEquals(listOf("Baú"), r.instalados.map { it.nome })
        assertEquals(listOf("Passou dos 20.000 km!"), r.marcos.map { it.titulo })
        assertEquals(
            listOf(QuadroDaRetrospectiva.KM, QuadroDaRetrospectiva.ROLES, QuadroDaRetrospectiva.MANUTENCAO, QuadroDaRetrospectiva.CUIDADOS, QuadroDaRetrospectiva.FOTOS, QuadroDaRetrospectiva.INSTALADOS),
            quadrosDaRetrospectiva(r),
        )
        assertTrue(montarRetrospectiva(2024, dados).vazia)
    }

    @Test
    fun `aparece em dezembro e em janeiro`() {
        assertEquals(2026, anoDaRetrospectiva(dia(2026, 12, 2)))
        assertEquals(2026, anoDaRetrospectiva(dia(2027, 1, 20)))
        assertNull(anoDaRetrospectiva(dia(2026, 10, 4)))
    }

    @Test
    fun `anos com historia, do mais recente`() {
        val d = DadosDaMoto(moto = moto.copy(chegouEm = dia(2024, 5, 1)), cuidados = listOf(Cuidado(motoId = 1, tipo = "lavagem", data = dia(2026, 1, 1), km = 1)))
        assertEquals(listOf(2026, 2024), anosComHistoria(d, dia(2026, 10, 4)))
    }
}
