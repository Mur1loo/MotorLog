package com.development.motorlog.domain

import com.development.motorlog.data.FotoMoto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AntesEDepoisTest {
    private val dia = 86_400_000L
    private val base = 20_000L * dia
    private fun foto(id: Long, data: Long, km: Int = 1000, legenda: String = "") =
        FotoMoto(id = id, motoId = 1, arquivo = "f$id.jpg", data = data, km = km, legenda = legenda)

    @Test
    fun `antes e a mais antiga, em qualquer ordem de toque`() {
        val velha = foto(1, base)
        val nova = foto(2, base + 12 * dia)
        assertSame(velha, montarAntesEDepois(nova, velha).antes)
        assertSame(nova, montarAntesEDepois(velha, nova).depois)
    }

    @Test
    fun `no mesmo dia desempata pelo km e depois pelo id`() {
        val a = foto(5, base, km = 1200)
        val b = foto(3, base, km = 1000)
        assertSame(b, montarAntesEDepois(a, b).antes)
        val c = foto(7, base, km = 1000)
        assertSame(b, montarAntesEDepois(c, b).antes)
    }

    @Test
    fun `intervalo em dias, meses e no mesmo dia`() {
        assertEquals("no mesmo dia", descreverIntervalo(montarAntesEDepois(foto(1, base), foto(2, base))))
        assertEquals("12 dias depois", descreverIntervalo(montarAntesEDepois(foto(1, base), foto(2, base + 12 * dia))))
        assertEquals("1 dia depois", descreverIntervalo(montarAntesEDepois(foto(1, base), foto(2, base + dia))))
    }

    @Test
    fun `titulo vem da legenda do depois`() {
        val par = montarAntesEDepois(foto(1, base, legenda = "Suja"), foto(2, base + dia, legenda = " Depois da lavagem "))
        assertEquals("Depois da lavagem", tituloDoAntesEDepois(par))
        assertEquals("Antes e depois", tituloDoAntesEDepois(montarAntesEDepois(foto(1, base), foto(2, base + dia))))
    }
}
