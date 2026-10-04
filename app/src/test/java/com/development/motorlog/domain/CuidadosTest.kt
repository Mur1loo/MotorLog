package com.development.motorlog.domain

import com.development.motorlog.data.Cuidado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CuidadosTest {
    private val dia = MILLIS_POR_DIA
    private val hoje = 100 * dia
    private fun c(tipo: TipoCuidado, data: Long, km: Int, id: Long = 0) = Cuidado(id = id, motoId = 1, tipo = tipo.codigo, data = data, km = km)

    private fun situacao(lista: List<Cuidado>, tipo: TipoCuidado, km: Int = 20000) =
        situacaoDosCuidados(lista, km, hoje).single { it.tipo == tipo }

    @Test
    fun `um item por tipo do painel, sem a cera antiga`() {
        assertEquals(
            listOf(TipoCuidado.LAVAGEM, TipoCuidado.LAVAGEM_DETALHADA, TipoCuidado.CORRENTE, TipoCuidado.CALIBRAGEM),
            situacaoDosCuidados(emptyList(), 20000, hoje).map { it.tipo },
        )
    }

    @Test
    fun `registro antigo de cera continua legivel`() {
        assertEquals("Encerei", TipoCuidado.doCodigo("cera")?.acao)
    }

    @Test
    fun `lavagem detalhada nao cobra`() {
        val s = situacao(listOf(c(TipoCuidado.LAVAGEM_DETALHADA, 10 * dia, 1000)), TipoCuidado.LAVAGEM_DETALHADA)
        assertEquals("há 90 dias", s.quando)
        assertFalse(s.jaEstaNaHora)
    }

    @Test
    fun `sem registro - corrente e calibragem pedem atencao, lavagem nao`() {
        val vazio = emptyList<Cuidado>()
        assertNull(situacao(vazio, TipoCuidado.LAVAGEM).ultimo)
        assertEquals("ainda não registrado", situacao(vazio, TipoCuidado.LAVAGEM).quando)
        assertFalse(situacao(vazio, TipoCuidado.LAVAGEM).jaEstaNaHora)
        assertTrue(situacao(vazio, TipoCuidado.CORRENTE).jaEstaNaHora)
        assertTrue(situacao(vazio, TipoCuidado.CALIBRAGEM).jaEstaNaHora)
    }

    @Test
    fun `lavagem conta dias e usa o registro mais recente`() {
        val lista = listOf(c(TipoCuidado.LAVAGEM, 80 * dia, 19000, id = 1), c(TipoCuidado.LAVAGEM, 88 * dia, 19500, id = 2))
        assertEquals("há 12 dias", situacao(lista, TipoCuidado.LAVAGEM).quando)
        assertEquals("hoje", situacao(listOf(c(TipoCuidado.LAVAGEM, hoje, 20000)), TipoCuidado.LAVAGEM).quando)
        assertEquals("ontem", situacao(listOf(c(TipoCuidado.LAVAGEM, hoje - dia, 20000)), TipoCuidado.LAVAGEM).quando)
    }

    @Test
    fun `corrente conta km e pede atencao a partir de 500`() {
        assertEquals("há 340 km", situacao(listOf(c(TipoCuidado.CORRENTE, 95 * dia, 19660)), TipoCuidado.CORRENTE).quando)
        assertFalse(situacao(listOf(c(TipoCuidado.CORRENTE, 95 * dia, 19660)), TipoCuidado.CORRENTE).jaEstaNaHora)
        assertTrue(situacao(listOf(c(TipoCuidado.CORRENTE, 90 * dia, 19500)), TipoCuidado.CORRENTE).jaEstaNaHora)
        assertEquals("agora há pouco", situacao(listOf(c(TipoCuidado.CORRENTE, hoje, 20000)), TipoCuidado.CORRENTE).quando)
    }

    @Test
    fun `calibragem pede atencao depois de 7 dias`() {
        assertFalse(situacao(listOf(c(TipoCuidado.CALIBRAGEM, 94 * dia, 19800)), TipoCuidado.CALIBRAGEM).jaEstaNaHora)
        assertTrue(situacao(listOf(c(TipoCuidado.CALIBRAGEM, 93 * dia, 19800)), TipoCuidado.CALIBRAGEM).jaEstaNaHora)
    }

    @Test
    fun `mesmo cuidado no mesmo dia e toque repetido`() {
        val lista = listOf(c(TipoCuidado.LAVAGEM, hoje, 20000))
        assertTrue(jaRegistradoHoje(lista, TipoCuidado.LAVAGEM, hoje))
        assertFalse(jaRegistradoHoje(lista, TipoCuidado.CERA, hoje))
        assertFalse(jaRegistradoHoje(lista, TipoCuidado.LAVAGEM, hoje + dia))
    }

    @Test
    fun `codigo volta pro tipo`() {
        assertEquals(TipoCuidado.CORRENTE, TipoCuidado.doCodigo("corrente"))
        assertNull(TipoCuidado.doCodigo("xyz"))
    }
}
