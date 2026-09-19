package com.development.motorlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. cria a tabela nova Servico
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `Servico` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `motoId` INTEGER NOT NULL, `custo` INTEGER NOT NULL, `kilometragem` INTEGER NOT NULL, `tipoServico` TEXT NOT NULL, `data` INTEGER NOT NULL, `local` TEXT NOT NULL, FOREIGN KEY(`motoId`) REFERENCES `Moto`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        // 2. cria o Registro_new já com as 3 FKs
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `Registro_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `motoId` INTEGER NOT NULL, `pecaId` INTEGER NOT NULL, `kmTroca` INTEGER NOT NULL, `servicoId` INTEGER, FOREIGN KEY(`motoId`) REFERENCES `Moto`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`pecaId`) REFERENCES `Peca`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`servicoId`) REFERENCES `Servico`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )"
        )
        // 3. copia as 4 colunas antigas; servicoId fica NULL nas linhas existentes
        db.execSQL(
            "INSERT INTO `Registro_new` (`id`, `motoId`, `pecaId`, `kmTroca`) SELECT `id`, `motoId`, `pecaId`, `kmTroca` FROM `Registro`"
        )
        // 4. derruba a tabela antiga
        db.execSQL("DROP TABLE `Registro`")
        // 5. a nova assume o nome Registro
        db.execSQL("ALTER TABLE `Registro_new` RENAME TO `Registro`")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // coluna simples (sem FK) → ALTER ADD COLUMN basta. NOT NULL exige DEFAULT
        // pras linhas que já existem (elas ganham preco = 0).
        db.execSQL("ALTER TABLE `Registro` ADD COLUMN `preco` INTEGER NOT NULL DEFAULT 0")
    }
}
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // só índices (não tocam dados). SQL copiado do 8.json exportado pelo Room.
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Registro_motoId` ON `Registro` (`motoId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Registro_pecaId` ON `Registro` (`pecaId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Registro_servicoId` ON `Registro` (`servicoId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Servico_motoId` ON `Servico` (`motoId`)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // SQL copiado do 9.json exportado pelo Room.
        // coluna simples → ADD COLUMN; DEFAULT 0 = "nunca registrado" pras motos que já existem
        db.execSQL("ALTER TABLE `Moto` ADD COLUMN `kmAtualizadoEm` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE TABLE IF NOT EXISTS `HistoricoKm` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `motoId` INTEGER NOT NULL, `km` INTEGER NOT NULL, `data` INTEGER NOT NULL, FOREIGN KEY(`motoId`) REFERENCES `Moto`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_HistoricoKm_motoId` ON `HistoricoKm` (`motoId`)")
    }
}

@Database(
    entities = [Moto::class,
        Registro::class,
        Peca::class,
        Servico::class,
        HistoricoKm::class],
    version = 9,
    exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motoDao(): MotoDao
    abstract fun registroDao(): RegistroDao
    abstract fun pecaDao(): PecaDao

    abstract fun servicoDao(): ServicoDao
    abstract fun historicoKmDao(): HistoricoKmDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = INSTANCE ?: Room.databaseBuilder(
                                context = context.applicationContext,
                                klass = AppDatabase::class.java,
                                name = "motorlog.db"
                ).addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9).build()

                INSTANCE = instance
                instance
            }
        }
    }
}