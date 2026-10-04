package com.development.motorlog.relatorio

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.development.motorlog.data.FotoMoto
import com.development.motorlog.fotos.ArmazemDeFotos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// As fotos do álbum pro novo dono, na despedida. O álbum em si nunca sai do app: copia as fotos
// pra cache/album (a pasta que o FileProvider expõe), da mais antiga pra mais recente, e abre o
// compartilhar com todas. false = nenhuma foto pra mandar.
suspend fun compartilharAlbum(contexto: Context, fotos: List<FotoMoto>, texto: String): Boolean {
    val uris = withContext(Dispatchers.IO) {
        val pasta = File(contexto.cacheDir, "album").apply { mkdirs() }
        pasta.listFiles()?.forEach { it.delete() }   // só o último envio fica (é cache)
        fotos.sortedWith(compareBy({ it.data }, { it.id })).mapIndexedNotNull { i, f ->
            val origem = ArmazemDeFotos.arquivo(contexto, f.arquivo)
            if (!origem.exists()) return@mapIndexedNotNull null
            val destino = origem.copyTo(File(pasta, "foto_${i + 1}.jpg"), overwrite = true)
            FileProvider.getUriForFile(contexto, contexto.packageName + ArmazemDeFotos.AUTORIDADE_SUFIXO, destino)
        }
    }
    if (uris.isEmpty()) return false
    val enviar = Intent(Intent.ACTION_SEND_MULTIPLE)
        .setType("image/jpeg")
        .putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList<Uri>(uris))
        .putExtra(Intent.EXTRA_TEXT, texto)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    enviar.clipData = ClipData.newRawUri(null, uris.first()).apply { uris.drop(1).forEach { addItem(ClipData.Item(it)) } }
    contexto.startActivity(Intent.createChooser(enviar, "Mandar as fotos"))
    return true
}
