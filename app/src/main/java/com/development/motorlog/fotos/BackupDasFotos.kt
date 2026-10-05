package com.development.motorlog.fotos

import android.content.Context
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.domain.escolherCapa

// Ao abrir o app: devolve ao álbum as fotos restauradas do backup do Google (celular novo) e
// mantém as cópias reduzidas que entram no backup (capas primeiro). Nunca derruba o app: se der
// errado, tenta de novo na próxima abertura.
suspend fun AppDatabase.sincronizarFotosDoBackup(contexto: Context) {
    runCatching {
        val fotos = fotoMotoDao().listarTodas()
        val porMoto = fotos.groupBy { it.motoId }
        val capas = motoDao().listarTodas().mapNotNull { moto -> escolherCapa(porMoto[moto.id].orEmpty(), moto.fotoCapaId)?.id }.toSet()
        ArmazemDeFotos.sincronizarBackup(contexto.applicationContext, fotos, capas)
    }
}
