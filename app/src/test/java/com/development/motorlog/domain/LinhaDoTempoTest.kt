package com.development.motorlog.domain

import com.development.motorlog.data.Cuidado
import com.development.motorlog.data.FotoMoto
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinhaDoTempoTest {
    private val dia = MILLIS_POR_DIA
    private val moto = Moto(id = 1, modelo = "Fan 160", placa = "", anoFabricacao = 2020, kilometragem = 21000, apelido = "Pretinha", chegouEm = 10 * dia, kmChegada = 15000)
    private val pecas = listOf(Peca(id = 7, nome = "Óleo do motor", intervaloKm = 3000), Peca(id = 8, nome = "Vela", intervaloKm = 10000))

    @Test
    fun `junta tudo, do mais recente para a chegada`() {
        val eventos = montarLinhaDoTempo(
            moto = moto,
            pecas = pecas,
            registros = listOf(
                Registro(motoId = 1, pecaId = 7, kmTroca = 18000, servicoId = null, data = 40 * dia),
                Registro(motoId = 1, pecaId = 8, kmTroca = 19000, servicoId = 3, data = 0),
            ),
            servicos = listOf(Servico(id = 3, motoId = 1, custo = 25000, kilometragem = 19000, tipoServico = "Revisão", data = 50 * dia, local = "Zé")),
            fotos = listOf(FotoMoto(id = 1, motoId = 1, arquivo = "a.jpg", data = 11 * dia, km = 15100, legenda = "O dia em que ela chegou")),
            cuidados = listOf(Cuidado(motoId = 1, tipo = "lavagem", data = 30 * dia, km = 17000)),
            marcos = listOf(Marco("km-20000", TipoMarco.KM, "Passou dos 20.000 km!", 60 * dia, 20000)),
        )
        assertEquals(
            listOf(TipoEvento.MARCO_KM, TipoEvento.VISITA, TipoEvento.TROCA, TipoEvento.CUIDADO, TipoEvento.FOTO, TipoEvento.CHEGADA),
            eventos.map { it.tipo },
        )
        assertEquals("A Pretinha chegou", eventos.last().titulo)
        assertEquals(15000, eventos.last().km)
        assertEquals("Zé · 1 peça trocada", eventos[1].detalhe)
        assertEquals("Troquei: Óleo do motor", eventos[2].titulo)
        assertEquals("Lavagem simples", eventos[3].titulo)
        assertEquals("a.jpg", eventos[4].foto)
    }

    @Test
    fun `sem chegada e sem dia conhecido fica de fora`() {
        val eventos = montarLinhaDoTempo(
            moto = moto.copy(chegouEm = 0),
            pecas = pecas,
            registros = listOf(Registro(motoId = 1, pecaId = 7, kmTroca = 18000, servicoId = null, data = 0)),
            servicos = emptyList(), fotos = emptyList(), cuidados = emptyList(), marcos = emptyList(),
        )
        assertTrue(eventos.isEmpty())
    }

    @Test
    fun `no mesmo dia a chegada fica por ultimo`() {
        val eventos = montarLinhaDoTempo(
            moto = moto,
            pecas = pecas, registros = emptyList(), servicos = emptyList(),
            fotos = listOf(FotoMoto(id = 1, motoId = 1, arquivo = "a.jpg", data = 10 * dia, km = 15000, legenda = "")),
            cuidados = emptyList(), marcos = emptyList(),
        )
        assertEquals(listOf(TipoEvento.FOTO, TipoEvento.CHEGADA), eventos.map { it.tipo })
        assertEquals("Foto da Pretinha", eventos.first().titulo)
    }
}
