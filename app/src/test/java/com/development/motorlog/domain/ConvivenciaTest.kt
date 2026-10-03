package com.development.motorlog.domain

import com.development.motorlog.data.Moto
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConvivenciaTest {
    private fun dia(ano: Int, mes: Int, d: Int) = LocalDate.of(ano, mes, d).toEpochDay() * MILLIS_POR_DIA
    private val hoje = dia(2026, 10, 3)

    @Test
    fun `tempo juntos em anos, meses e dias`() {
        assertEquals("1 ano e 3 meses", descreverTempoJuntos(dia(2025, 7, 1), hoje))
        assertEquals("2 anos", descreverTempoJuntos(dia(2024, 10, 3), hoje))
        assertEquals("1 ano e 1 mês", descreverTempoJuntos(dia(2025, 9, 3), hoje))
        assertEquals("5 meses", descreverTempoJuntos(dia(2026, 5, 1), hoje))
        assertEquals("12 dias", descreverTempoJuntos(dia(2026, 9, 21), hoje))
        assertEquals("1 dia", descreverTempoJuntos(dia(2026, 10, 2), hoje))
        assertEquals("hoje", descreverTempoJuntos(hoje, hoje))
    }

    @Test
    fun `chegada nao informada ou no futuro nao descreve`() {
        assertNull(descreverTempoJuntos(0, hoje))
        assertNull(descreverTempoJuntos(dia(2026, 10, 10), hoje))
    }

    @Test
    fun `km juntos`() {
        assertEquals(23400, kmJuntos(10000, 33400))
        assertNull(kmJuntos(-1, 33400))
        assertEquals(0, kmJuntos(34000, 33400))   // km corrigido pra baixo não vira negativo
    }

    @Test
    fun `nome da moto e o apelido, ou o modelo`() {
        val moto = Moto(modelo = "Fan 160", placa = "", anoFabricacao = 2020, kilometragem = 0)
        assertEquals("Fan 160", nomeDaMoto(moto))
        assertEquals("Pretinha", nomeDaMoto(moto.copy(apelido = " Pretinha ")))
    }
}
