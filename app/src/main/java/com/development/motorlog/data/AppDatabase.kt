package com.development.motorlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Moto::class, Registro::class, Peca::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motoDao(): MotoDao
    abstract fun registroDao(): RegistroDao
    abstract fun pecaDao(): PecaDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = INSTANCE ?: Room.databaseBuilder(
                                context = context.applicationContext,
                                klass = AppDatabase::class.java,
                                name = "motorlog.db"
                ).fallbackToDestructiveMigration(true).
                    build()

                INSTANCE = instance
                instance
            }
        }
    }
}