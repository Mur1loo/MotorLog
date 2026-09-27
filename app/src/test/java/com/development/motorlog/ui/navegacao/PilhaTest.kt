package com.development.motorlog.ui.navegacao

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PilhaTest {
    @Test
    fun `abertura normal comeca na Garagem e aberta pela notificacao volta pra Garagem`() {
        assertEquals(listOf("Garagem"), pilhaInicial(abrirNoPainel = false))
        assertEquals(listOf("Garagem"), pilhaInicial(abrirNoPainel = true).voltar())
    }

    @Test
    fun `voltar leva pra tela de onde veio`() {
        // o caso que a tabela fixa errava: "Fui à oficina" aberto pela aba Trocas voltava pro Painel
        val pilha = pilhaDaAba("Trocas").abrir("RegistrarServico")
        assertEquals("Trocas", pilha.voltar().last())
    }

    @Test
    fun `detalhe e edicao voltam pelo mesmo caminho`() {
        val pilha = pilhaDaAba("Historico").abrir("RevisaoDetail").abrir("EditarServico")
        assertEquals(listOf("Garagem", "Painel", "Historico", "RevisaoDetail"), pilha.voltar())
        assertEquals(listOf("Garagem", "Painel", "Historico"), pilha.voltar().voltar())
    }

    @Test
    fun `garagem e o fundo da pilha`() {
        val fundo = listOf("Garagem")
        assertFalse(fundo.podeVoltar)
        assertEquals(fundo, fundo.voltar())
        assertTrue(fundo.abrir("Cadastro").podeVoltar)
    }

    @Test
    fun `mesma tela duas vezes nao empilha`() {
        assertEquals(listOf("Garagem", "Cadastro"), listOf("Garagem").abrir("Cadastro").abrir("Cadastro"))
    }

    @Test
    fun `trocar de aba nao empilha e o voltar de qualquer aba vai pro Painel`() {
        assertEquals(listOf("Garagem", "Painel"), pilhaDaAba("Painel"))
        assertEquals(listOf("Garagem", "Painel", "Fotos"), pilhaDaAba("Fotos"))
        assertEquals(listOf("Garagem", "Painel"), pilhaDaAba("Fotos").voltar())
    }
}
