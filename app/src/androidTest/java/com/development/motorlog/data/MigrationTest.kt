package com.development.motorlog.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Sobe um banco na versão antiga (a partir de schemas/N.json), semeia dados, roda as migrations
// reais do AppDatabase e valida o schema final contra o JSON exportado pelo Room.
// schemas/5.json foi RECONSTRUÍDO à mão a partir do 6.json (exportSchema só ligou na v6); o
// identityHash dele é sintético — o helper só usa createSql/setupQueries (DECISOES D3).
// Roda no emulador: ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val nomeBanco = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    private fun semearV5(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO Moto (id, modelo, placa, anoFabricacao, kilometragem) VALUES (1, 'Crosser', 'ABC1D23', 2020, 16000)")
        db.execSQL("INSERT INTO Peca (id, nome, intervaloKm) VALUES (1, 'Óleo do motor', 3000)")
        db.execSQL("INSERT INTO Registro (id, motoId, pecaId, kmTroca) VALUES (7, 1, 1, 15000)")
    }

    @Test
    fun migra5para6_registroGanhaServicoIdNulo() {
        helper.createDatabase(nomeBanco, 5).apply { semearV5(this); close() }

        val db = helper.runMigrationsAndValidate(nomeBanco, 6, true, MIGRATION_5_6)

        db.query("SELECT id, motoId, pecaId, kmTroca, servicoId FROM Registro").use { c ->
            assertEquals(1, c.count)
            c.moveToFirst()
            assertEquals(7L, c.getLong(0))
            assertEquals(15000, c.getInt(3))
            assertTrue("servicoId deve nascer NULL", c.isNull(4))
        }
        db.query("SELECT COUNT(*) FROM Servico").use { c -> c.moveToFirst(); assertEquals(0, c.getInt(0)) }
    }

    @Test
    fun migra5para7_dadosSobrevivem_precoNasceZero() {
        helper.createDatabase(nomeBanco, 5).apply { semearV5(this); close() }

        val db = helper.runMigrationsAndValidate(nomeBanco, 7, true, MIGRATION_5_6, MIGRATION_6_7)

        db.query("SELECT modelo, kilometragem FROM Moto WHERE id = 1").use { c ->
            c.moveToFirst()
            assertEquals("Crosser", c.getString(0))
            assertEquals(16000, c.getInt(1))
        }
        db.query("SELECT kmTroca, servicoId, preco FROM Registro WHERE id = 7").use { c ->
            c.moveToFirst()
            assertEquals(15000, c.getInt(0))
            assertTrue(c.isNull(1))
            assertEquals(0, c.getInt(2))
        }
        // a FK nova funciona: apagar o serviço solta a troca (SET NULL) em vez de apagá-la.
        // (o Room liga foreign_keys ao abrir o banco real; na conexão do helper é preciso ligar à mão)
        db.execSQL("PRAGMA foreign_keys = ON")
        db.execSQL("INSERT INTO Servico (id, motoId, custo, kilometragem, tipoServico, data, local) VALUES (3, 1, 200, 16000, 'Revisão', 0, 'Oficina')")
        db.execSQL("UPDATE Registro SET servicoId = 3 WHERE id = 7")
        db.execSQL("DELETE FROM Servico WHERE id = 3")
        db.query("SELECT servicoId FROM Registro WHERE id = 7").use { c -> c.moveToFirst(); assertTrue(c.isNull(0)) }
    }

    @Test
    fun migra7para8_criaIndices_semTocarDados() {
        helper.createDatabase(nomeBanco, 7).apply {
            // na v7 o CREATE do Room não tem DEFAULT (o DEFAULT 0 vive só na MIGRATION_6_7) → preco explícito
            execSQL("INSERT INTO Moto (id, modelo, placa, anoFabricacao, kilometragem) VALUES (1, 'Crosser', 'ABC1D23', 2020, 16000)")
            execSQL("INSERT INTO Peca (id, nome, intervaloKm) VALUES (1, 'Óleo do motor', 3000)")
            execSQL("INSERT INTO Registro (id, motoId, pecaId, kmTroca, servicoId, preco) VALUES (7, 1, 1, 15000, NULL, 0)")
            close()
        }
        val db = helper.runMigrationsAndValidate(nomeBanco, 8, true, MIGRATION_7_8)
        db.query("SELECT COUNT(*) FROM Registro").use { c -> c.moveToFirst(); assertEquals(1, c.getInt(0)) }
        db.query("SELECT name FROM sqlite_master WHERE type = 'index' AND name LIKE 'index_%' ORDER BY name").use { c ->
            val nomes = generateSequence { if (c.moveToNext()) c.getString(0) else null }.toList()
            assertEquals(
                listOf("index_Registro_motoId", "index_Registro_pecaId", "index_Registro_servicoId", "index_Servico_motoId"),
                nomes,
            )
        }
    }

    @Test
    fun migra8para9_motoGanhaKmAtualizadoEmZero_eTabelaHistoricoKm() {
        helper.createDatabase(nomeBanco, 8).apply {
            execSQL("INSERT INTO Moto (id, modelo, placa, anoFabricacao, kilometragem) VALUES (1, 'Crosser', 'ABC1D23', 2020, 16000)")
            close()
        }
        val db = helper.runMigrationsAndValidate(nomeBanco, 9, true, MIGRATION_8_9)
        db.query("SELECT kilometragem, kmAtualizadoEm FROM Moto WHERE id = 1").use { c ->
            c.moveToFirst(); assertEquals(16000, c.getInt(0)); assertEquals(0L, c.getLong(1))
        }
        db.query("SELECT COUNT(*) FROM HistoricoKm").use { c -> c.moveToFirst(); assertEquals(0, c.getInt(0)) }
        // FK CASCADE: apagar a moto leva o histórico junto
        db.execSQL("PRAGMA foreign_keys = ON")
        db.execSQL("INSERT INTO HistoricoKm (motoId, km, data) VALUES (1, 16000, 0)")
        db.execSQL("DELETE FROM Moto WHERE id = 1")
        db.query("SELECT COUNT(*) FROM HistoricoKm").use { c -> c.moveToFirst(); assertEquals(0, c.getInt(0)) }
    }

    @Test
    fun migra5para9_caminhoCompletoDoCelular() {
        helper.createDatabase(nomeBanco, 5).apply { semearV5(this); close() }
        val db = helper.runMigrationsAndValidate(
            nomeBanco, 9, true, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
        )
        db.query("SELECT kmTroca, preco FROM Registro WHERE id = 7").use { c ->
            c.moveToFirst(); assertEquals(15000, c.getInt(0)); assertEquals(0, c.getInt(1))
        }
        db.query("SELECT kmAtualizadoEm FROM Moto WHERE id = 1").use { c -> c.moveToFirst(); assertEquals(0L, c.getLong(0)) }
    }

    @Test
    fun migra6para7_precoNasceZero() {
        helper.createDatabase(nomeBanco, 6).apply {
            semearV5(this)
            close()
        }
        val db = helper.runMigrationsAndValidate(nomeBanco, 7, true, MIGRATION_6_7)
        db.query("SELECT preco FROM Registro WHERE id = 7").use { c -> c.moveToFirst(); assertEquals(0, c.getInt(0)) }
    }
}
