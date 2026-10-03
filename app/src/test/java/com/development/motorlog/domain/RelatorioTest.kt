package com.development.motorlog.domain

import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RelatorioTest {
    private val moto = Moto(id = 1, modelo = "Crosser", placa = "ABC1D23", anoFabricacao = 2020, kilometragem = 16000)
    private val pecas = listOf(Peca(id = 1, nome = "Óleo do motor", intervaloKm = 3000), Peca(id = 2, nome = "Vela de ignição", intervaloKm = 10000))

    @Test
    fun `junta oficina e trocas por conta, mais recente primeiro, com as pecas de cada visita`() {
        val servico = Servico(id = 5, motoId = 1, custo = 25000, kilometragem = 15000, tipoServico = "Revisão", data = 90, local = "Oficina do Zé")
        val registros = listOf(
            Registro(motoId = 1, pecaId = 2, kmTroca = 15000, servicoId = 5, preco = 4500),
            Registro(motoId = 1, pecaId = 1, kmTroca = 15000, servicoId = 5, preco = 6000),
            Registro(motoId = 1, pecaId = 1, kmTroca = 15800, servicoId = null, preco = 5500, data = 100),
            Registro(motoId = 1, pecaId = 2, kmTroca = 9000, servicoId = null, preco = 0),
        )
        val r = montarRelatorio(moto, pecas, registros, listOf(servico), emptyList(), kmRodados = 1000)

        assertEquals(listOf(15800, 15000, 9000), r.itens.map { it.km })
        val emCasa = r.itens[0]
        assertEquals(OrigemItem.POR_CONTA, emCasa.origem)
        assertEquals("Óleo do motor", emCasa.titulo)
        assertEquals(100L, emCasa.data)
        assertNull(emCasa.local)
        val visita = r.itens[1]
        assertEquals("Oficina do Zé", visita.local)
        assertEquals(listOf(PecaDoItem("Óleo do motor", 6000), PecaDoItem("Vela de ignição", 4500)), visita.pecas)
        assertNull("sem dia conhecido", r.itens[2].data)

        assertEquals(25000, r.gastoOficina); assertEquals(1, r.visitas)
        assertEquals(5500, r.gastoPorConta); assertEquals(2, r.trocasPorConta)
        assertEquals(30500, r.gastoTotal)
        assertEquals(0.305, r.custoPorKm!!, 0.0001)
    }

    @Test
    fun `situacao mostra so pecas com registro`() {
        val recs = listOf(
            Recomendacao(1, "Óleo do motor", 15000, 18000, 2000, StatusTroca.OK),
            Recomendacao(2, "Vela de ignição", null, null, null, StatusTroca.NUNCA_TROCADA),
        )
        val r = montarRelatorio(moto, pecas, emptyList(), emptyList(), recs, kmRodados = 0)
        assertEquals(listOf(1L), r.situacao.map { it.pecaId })
        assertNull(r.custoPorKm)
    }

    @Test
    fun `nome do arquivo sem acento nem simbolo`() {
        assertEquals("MotorLog_Crosser_ABC1D23.pdf", nomeDoArquivoPdf(moto))
        assertEquals("MotorLog_Fazer_250_Lander_XYZ_1234.pdf", nomeDoArquivoPdf(moto.copy(modelo = "Fazer 250 / Lânder", placa = "XYZ-1234")))
        assertEquals("MotorLog_moto.pdf", nomeDoArquivoPdf(moto.copy(modelo = "", placa = " ")))
    }
}
