package com.development.motorlog.fotos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.LruCache
import androidx.core.content.FileProvider
import com.development.motorlog.domain.fatorDeAmostragem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.max

// Fotos do álbum no armazenamento PRIVADO do app (filesDir/fotos): não aparecem na galeria, não
// precisam de permissão e saem junto quando o app é desinstalado. Cada foto é salva já reduzida
// (lado maior ≤ 1920 px, JPEG 85 — ~300-600 KB) e já girada conforme o EXIF da câmera, então quem
// lê não precisa se preocupar com orientação.
object ArmazemDeFotos {
    private const val PASTA = "fotos"
    private const val LADO_MAX_SALVO = 1920
    const val AUTORIDADE_SUFIXO = ".arquivos"   // FileProvider (AndroidManifest + xml/arquivos_compartilhados)

    fun arquivo(contexto: Context, nome: String): File = File(File(contexto.filesDir, PASTA), nome)

    // Arquivo temporário onde o app da câmera grava a foto (cache/camera) + a Uri que ele recebe
    fun novaUriDaCamera(contexto: Context): Uri {
        val pasta = File(contexto.cacheDir, "camera").apply { mkdirs() }
        val arquivo = File(pasta, "foto_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(contexto, contexto.packageName + AUTORIDADE_SUFIXO, arquivo)
    }

    fun limparTemporariosDaCamera(contexto: Context) {
        File(contexto.cacheDir, "camera").listFiles()?.forEach { it.delete() }
    }

    // Lê a imagem da Uri (galeria ou câmera), reduz, gira e grava. Devolve o nome do arquivo, ou
    // null se não deu pra ler (arquivo corrompido, formato estranho, Uri sem permissão).
    suspend fun salvarDe(contexto: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val cr = contexto.contentResolver
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
            if (limites.outWidth <= 0 || limites.outHeight <= 0) return@runCatching null

            val opcoes = BitmapFactory.Options().apply {
                inSampleSize = fatorDeAmostragem(limites.outWidth, limites.outHeight, LADO_MAX_SALVO)
            }
            val lida = cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opcoes) } ?: return@runCatching null
            val graus = cr.openInputStream(uri)?.use { grausDoExif(ExifInterface(it)) } ?: 0

            val escala = LADO_MAX_SALVO.toFloat() / max(lida.width, lida.height)
            val matriz = Matrix().apply {
                if (escala < 1f) postScale(escala, escala)
                if (graus != 0) postRotate(graus.toFloat())
            }
            val pronta = if (matriz.isIdentity) lida
                else Bitmap.createBitmap(lida, 0, 0, lida.width, lida.height, matriz, true).also { if (it !== lida) lida.recycle() }

            val nome = "${UUID.randomUUID()}.jpg"
            val destino = arquivo(contexto, nome).apply { parentFile?.mkdirs() }
            destino.outputStream().use { pronta.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            pronta.recycle()
            nome
        }.getOrNull()
    }

    fun apagar(contexto: Context, nome: String) {
        arquivo(contexto, nome).delete()
        cache.snapshot().keys.filter { it.startsWith("$nome@") }.forEach { cache.remove(it) }
    }

    // ── leitura com cache: a mesma miniatura aparece na Garagem, no Painel e no álbum ──

    // até 1/8 da memória do app em bitmaps (medido em KB)
    private val cache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount / 1024
    }

    fun doCache(nome: String, ladoMax: Int): Bitmap? = cache.get("$nome@$ladoMax")

    // Bitmap com o lado maior perto de 'ladoMax' (nunca menor que isso, se a foto for maior).
    // Chamar fora da main thread.
    fun carregar(contexto: Context, nome: String, ladoMax: Int): Bitmap? {
        doCache(nome, ladoMax)?.let { return it }
        val f = arquivo(contexto, nome)
        if (!f.exists()) return null
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(f.path, limites)
        val opcoes = BitmapFactory.Options().apply { inSampleSize = fatorDeAmostragem(limites.outWidth, limites.outHeight, ladoMax) }
        val bmp = BitmapFactory.decodeFile(f.path, opcoes) ?: return null
        cache.put("$nome@$ladoMax", bmp)
        return bmp
    }

    private fun grausDoExif(exif: ExifInterface): Int = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> 270
        else -> 0
    }

    // usado pelo PDF: foto de capa como bitmap pequeno (o PDF não precisa de 1920 px)
    fun carregarParaPdf(contexto: Context, nome: String): Bitmap? = carregar(contexto, nome, 600)
}
