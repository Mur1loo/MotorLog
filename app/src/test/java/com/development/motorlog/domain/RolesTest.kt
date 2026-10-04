package com.development.motorlog.domain

import com.development.motorlog.data.Passeio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RolesTest {
    private fun role(id: Long, saida: Int, chegada: Int = -1, companhia: String = "") =
        Passeio(id = id, motoId = 1, destino = "Destino $id", data = 0, kmSaida = saida, kmChegada = chegada, companhia = companhia)

    @Test
    fun `km do role so com a volta anotada e acima da saida`() {
        assertEquals(320, kmDoRole(role(1, 16_000, 16_320)))
        assertNull(kmDoRole(role(2, 16_000)))
        assertNull(kmDoRole(role(3, 16_000, 15_000)))
    }

    @Test
    fun `resumo soma os km e acha o maior`() {
        val r = resumirRoles(listOf(role(1, 16_000, 16_320), role(2, 17_000), role(3, 18_000, 18_900)))
        assertEquals(3, r.quantidade)
        assertEquals(1_220, r.kmTotal)
        assertEquals(3L, r.maior?.id)
    }

    @Test
    fun `validacao`() {
        assertTrue(validarRole("Praia", "16000", "").ok)
        assertEquals("Pra onde vocês foram?", validarRole(" ", "16000", "").destino)
        assertEquals("A volta não pode ter menos km que a saída", validarRole("Praia", "16000", "15000").kmChegada)
        assertEquals("Use só números, ex.: 16100", validarRole("Praia", "16000", "abc").kmChegada)
    }

    @Test
    fun `detalhe junta km e companhia`() {
        assertEquals("320 km · com a galera", detalheDoRole(role(1, 16_000, 16_320, " a galera "), { "$it km" }))
        assertNull(detalheDoRole(role(2, 16_000), { "$it km" }))
    }
}
