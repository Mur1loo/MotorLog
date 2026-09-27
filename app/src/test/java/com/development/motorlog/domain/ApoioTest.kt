package com.development.motorlog.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApoioTest {
    private val dia = MILLIS_POR_DIA
    private val hoje = 100 * dia

    // usuário fiel: começou há 20 dias e atualizou o km em 8 dias, o último ontem
    private val fiel = listOf(80, 82, 85, 88, 90, 93, 96, 99).map { it * dia }

    @Test
    fun `usuario fiel que nunca foi pedido ve o pedido`() {
        assertTrue(deveMostrarPedidoDeApoio(fiel, ultimoPedido = 0, hoje = hoje))
    }

    @Test
    fun `quem acabou de instalar nao e pedido`() {
        // 6 dias com km, mas tudo nas últimas 2 semanas
        val novato = listOf(90, 92, 94, 96, 98, 99).map { it * dia }
        assertFalse(deveMostrarPedidoDeApoio(novato, 0, hoje))
    }

    @Test
    fun `quem abre pouco nao e pedido`() {
        val poucoUso = listOf(70, 85, 95, 99).map { it * dia }
        assertFalse(deveMostrarPedidoDeApoio(poucoUso, 0, hoje))
    }

    @Test
    fun `quem largou o app nao e pedido`() {
        val largou = listOf(60, 62, 64, 66, 68, 70).map { it * dia }
        assertFalse(deveMostrarPedidoDeApoio(largou, 0, hoje))
    }

    @Test
    fun `no maximo um pedido a cada 30 dias`() {
        assertFalse(deveMostrarPedidoDeApoio(fiel, ultimoPedido = hoje, hoje = hoje))
        assertFalse(deveMostrarPedidoDeApoio(fiel, ultimoPedido = hoje - 29 * dia, hoje = hoje))
        assertTrue(deveMostrarPedidoDeApoio(fiel, ultimoPedido = hoje - 30 * dia, hoje = hoje))
    }

    @Test
    fun `limites exatos contam`() {
        // 6 dias, o 1º há exatamente 14 dias e o último há exatamente 7
        val noLimite = listOf(86, 87, 88, 90, 92, 93).map { it * dia }
        assertTrue(deveMostrarPedidoDeApoio(noLimite, 0, hoje))
    }

    @Test
    fun `sem historico nao pede`() {
        assertFalse(deveMostrarPedidoDeApoio(emptyList(), 0, hoje))
    }
}
