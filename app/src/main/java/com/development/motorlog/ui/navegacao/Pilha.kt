package com.development.motorlog.ui.navegacao

// Pilha de telas: o voltar do celular (e a seta do topo) leva pra tela de onde o usuário veio, em
// vez de adivinhar por uma tabela fixa. Lista de nomes de tela (String), a última é a que aparece;
// é Serializable, então sobrevive ao giro e à morte do processo via rememberSaveable.
// Kotlin puro (sem Android) pra ser testada na JVM.

const val TELA_INICIAL = "Garagem"
const val TELA_PAINEL = "Painel"

// Estado inicial: aberto pela notificação/atalho numa moto → o voltar do Painel leva pra Garagem
fun pilhaInicial(abrirNoPainel: Boolean): List<String> =
    if (abrirNoPainel) listOf(TELA_INICIAL, TELA_PAINEL) else listOf(TELA_INICIAL)

// Abre uma tela por cima da atual (tocar duas vezes no mesmo botão não empilha duas vezes)
fun List<String>.abrir(tela: String): List<String> = if (lastOrNull() == tela) this else this + tela

// Volta uma tela; a Garagem é o fundo (dali o voltar sai do app, quem decide é o sistema)
fun List<String>.voltar(): List<String> = if (size > 1) dropLast(1) else this

val List<String>.podeVoltar: Boolean get() = size > 1

// Abas da moto (Painel, Histórico, Trocas, Fotos): trocar de aba não empilha — o voltar de
// qualquer aba vai pro Painel, e do Painel pra Garagem, como nos apps de abas do Android
fun pilhaDaAba(aba: String): List<String> =
    if (aba == TELA_PAINEL) listOf(TELA_INICIAL, TELA_PAINEL) else listOf(TELA_INICIAL, TELA_PAINEL, aba)
