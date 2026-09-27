package com.development.motorlog.domain

import com.development.motorlog.data.FotoMoto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FotosTest {
    private fun foto(id: Long, data: Long) = FotoMoto(id = id, motoId = 1, arquivo = "$id.jpg", data = data, km = 0)

    @Test
    fun `capa e a escolhida pelo dono`() {
        assertEquals(1L, escolherCapa(listOf(foto(1, 10), foto(2, 20)), fotoCapaId = 1)?.id)
    }

    @Test
    fun `sem escolha ou escolhida excluida, capa e a mais recente`() {
        val fotos = listOf(foto(1, 10), foto(3, 20), foto(2, 20))
        assertEquals(3L, escolherCapa(fotos, fotoCapaId = 0)?.id)    // mesmo dia: a adicionada por último
        assertEquals(3L, escolherCapa(fotos, fotoCapaId = 99)?.id)
        assertNull(escolherCapa(emptyList(), fotoCapaId = 1))
    }

    @Test
    fun `fator de amostragem e potencia de 2 que nao deixa a imagem menor que o pedido`() {
        assertEquals(1, fatorDeAmostragem(800, 600, 1000))
        assertEquals(1, fatorDeAmostragem(4000, 3000, 2048))
        assertEquals(4, fatorDeAmostragem(4000, 3000, 1000))
        assertEquals(8, fatorDeAmostragem(3000, 4000, 400))       // retrato: vale o lado maior
        assertEquals(1, fatorDeAmostragem(0, 0, 400))             // arquivo ilegível não quebra
    }
}
