package com.development.motorlog.domain

import com.development.motorlog.data.FotoMoto
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupDeFotosTest {
    private fun foto(id: Long, data: Long) = FotoMoto(id = id, motoId = 1, arquivo = "f$id.jpg", data = data, km = 0)
    private val fotos = listOf(foto(1, 10), foto(2, 30), foto(3, 20), foto(4, 5))

    @Test
    fun `tudo cabe, tudo entra`() {
        assertEquals(setOf("f1.jpg", "f2.jpg", "f3.jpg", "f4.jpg"), fotosNoBackup(fotos, emptySet(), emptyMap()))
    }

    @Test
    fun `sem espaco, a capa entra primeiro e depois as mais recentes`() {
        val tamanhos = fotos.associate { it.arquivo to 100L }
        // cabem 2: a capa (a mais antiga, f4) e a mais recente (f2)
        assertEquals(setOf("f4.jpg", "f2.jpg"), fotosNoBackup(fotos, capas = setOf(4L), tamanhos = tamanhos, orcamento = 250))
    }

    @Test
    fun `foto grande que nao cabe nao impede as menores`() {
        val tamanhos = mapOf("f2.jpg" to 500L, "f3.jpg" to 100L, "f1.jpg" to 100L, "f4.jpg" to 100L)
        assertEquals(setOf("f3.jpg", "f1.jpg"), fotosNoBackup(fotos, emptySet(), tamanhos, orcamento = 250))
    }

    @Test
    fun `copia que ainda nao existe conta pelo tamanho estimado`() {
        val orcamento = TAMANHO_ESTIMADO_COPIA * 2
        assertEquals(setOf("f2.jpg", "f3.jpg"), fotosNoBackup(fotos, emptySet(), emptyMap(), orcamento))
    }
}
