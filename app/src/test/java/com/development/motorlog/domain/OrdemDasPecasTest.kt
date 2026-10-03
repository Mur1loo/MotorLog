package com.development.motorlog.domain

import com.development.motorlog.data.Peca
import org.junit.Assert.assertEquals
import org.junit.Test

class OrdemDasPecasTest {
    private val catalogo = listOf(
        Peca(id = 1, nome = "Regulagem de válvulas", intervaloKm = 15000),
        Peca(id = 2, nome = "Corrente de comando", intervaloKm = 50000),
        Peca(id = 3, nome = "Pneu traseiro", intervaloKm = 12000),
        Peca(id = 4, nome = "Óleo do motor", intervaloKm = 3000),
        Peca(id = 5, nome = "Peça que eu criei", intervaloKm = 8000),
    )

    @Test
    fun `sem historico as do dia a dia vem primeiro e o resto mantem a ordem do catalogo`() {
        assertEquals(listOf(4L, 3L, 1L, 2L, 5L), ordenarPecasPorUso(catalogo, emptyMap()).map { it.id })
    }

    @Test
    fun `as que a moto mais troca passam na frente`() {
        val usos = mapOf(5L to 3, 3L to 1)
        assertEquals(listOf(5L, 3L, 4L, 1L, 2L), ordenarPecasPorUso(catalogo, usos).map { it.id })
    }

    @Test
    fun `nome do dia a dia nao depende de maiusculas`() {
        val pecas = listOf(Peca(id = 9, nome = "x", intervaloKm = 1), Peca(id = 8, nome = "ÓLEO DO MOTOR", intervaloKm = 1))
        assertEquals(listOf(8L, 9L), ordenarPecasPorUso(pecas, emptyMap()).map { it.id })
    }
}
