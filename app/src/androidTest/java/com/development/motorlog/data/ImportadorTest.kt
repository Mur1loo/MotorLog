package com.development.motorlog.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.development.motorlog.domain.DadosImportados
import com.development.motorlog.domain.MotoImportada
import com.development.motorlog.domain.PecaImportada
import com.development.motorlog.domain.ServicoImportado
import com.development.motorlog.domain.TrocaImportada
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Restaurar backup no banco real (em memória): cria o que falta, liga troca ao serviço do mesmo km
// e NÃO duplica quando o mesmo arquivo é importado duas vezes.
@RunWith(AndroidJUnit4::class)
class ImportadorTest {
    private lateinit var db: AppDatabase

    @Before
    fun abrir() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
    }

    @After
    fun fechar() = db.close()

    private val dados = DadosImportados(
        motos = listOf(MotoImportada("Crosser", "ABC1D23", 2020, 16000, kmAtualizadoEm = 86_400_000L * 5)),
        trocas = listOf(
            TrocaImportada("Crosser ABC1D23", "Óleo do motor", 12000, 0, emServico = false),
            TrocaImportada("Crosser ABC1D23", "Óleo do motor", 16000, 60, emServico = true),
            TrocaImportada("Crosser ABC1D23", "Peça que não existe", 15000, 0, emServico = false),
        ),
        servicos = listOf(ServicoImportado("Crosser ABC1D23", "Revisão", 86_400_000L * 9, 16000, 250, "Zé")),
        pecas = listOf(PecaImportada("Óleo do motor", 3000)),
        avisos = emptyList(),
    )

    @Test
    fun importa_liga_troca_ao_servico_e_nao_duplica() = runBlocking {
        val r1 = db.importar(dados)
        assertEquals(1, r1.motos); assertEquals(3, r1.trocas); assertEquals(1, r1.servicos)
        assertEquals(2, r1.pecas)   // "Óleo do motor" do catálogo + "Peça que não existe" criada pra não perder a troca

        val moto = db.motoDao().listarTodas().single()
        assertEquals(16000, moto.kilometragem)
        assertEquals(86_400_000L * 5, moto.kmAtualizadoEm)
        assertEquals(1, db.historicoKmDao().listarPorMoto(moto.id).size)

        val servico = db.servicoDao().query(moto.id).single()
        val trocas = db.registroDao().listarRegistros(moto.id)
        val noServico = trocas.single { it.kmTroca == 16000 }
        assertEquals(servico.id, noServico.servicoId)
        assertEquals(60, noServico.preco)
        assertNotNull(trocas.single { it.kmTroca == 12000 }.let { it.servicoId ?: 0L })

        // segunda vez: tudo já existe
        val r2 = db.importar(dados)
        assertEquals(0, r2.motos); assertEquals(0, r2.trocas); assertEquals(0, r2.servicos); assertEquals(0, r2.pecas)
        assertEquals(3, db.registroDao().listarRegistros(moto.id).size)
        assertEquals(1, db.servicoDao().query(moto.id).size)
        assertEquals(1, db.motoDao().listarTodas().size)
    }
}
