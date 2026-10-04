package com.development.motorlog.domain

// Ficha da moto: uma imagem pra mostrar a moto com orgulho (status, grupo dos motoboys) ou pra
// anunciar na venda — foto, nome, km e até 4 quadros com o que ela tem de bom. Aqui só a escolha
// dos quadros; o desenho fica em relatorio/FichaDaMoto.kt. Sem placa: a imagem vai pra rede.

enum class QuadroDaFicha { INDICE, KM_JUNTOS, MANUTENCAO, CONSUMO, CUIDADOS }

data class DadosDaFicha(
    val indice: IndiceDeCuidado?,
    val kmJuntos: Int?,          // km rodados desde que ela chegou (null = chegada sem km)
    val kmPorLitro: Double?,     // média de consumo, quando já dá pra calcular
    val trocas: Int,             // trocas de peça registradas (avulsas e de visitas)
    val visitas: Int,            // visitas à oficina
    val cuidados: Int,           // anotações no diário de cuidados
)

const val QUADROS_NA_FICHA = 4

// Só os quadros com dado, na ordem do que mais conta pra quem vê: o índice de cuidado, o tanto
// que rodaram juntos, a manutenção registrada, o consumo e os cuidados. No máximo 4 (2 × 2).
fun quadrosDaFicha(d: DadosDaFicha): List<QuadroDaFicha> = buildList {
    if (d.indice != null) add(QuadroDaFicha.INDICE)
    if ((d.kmJuntos ?: 0) > 0) add(QuadroDaFicha.KM_JUNTOS)
    if (d.trocas + d.visitas > 0) add(QuadroDaFicha.MANUTENCAO)
    if (d.kmPorLitro != null) add(QuadroDaFicha.CONSUMO)
    if (d.cuidados > 0) add(QuadroDaFicha.CUIDADOS)
}.take(QUADROS_NA_FICHA)
