package com.development.motorlog.domain

import com.development.motorlog.data.FotoMoto

// A capa da moto: a foto escolhida pelo dono, ou a mais recente quando ele não escolheu
// (ou quando a escolhida foi excluída). null = moto sem fotos → a UI mostra o badge desenhado.
fun escolherCapa(fotos: List<FotoMoto>, fotoCapaId: Long): FotoMoto? =
    fotos.find { it.id == fotoCapaId && fotoCapaId > 0 }
        ?: fotos.maxWithOrNull(compareBy<FotoMoto>({ it.data }, { it.id }))

// inSampleSize do BitmapFactory: a maior potência de 2 que ainda deixa o lado MAIOR da imagem
// com pelo menos 'ladoMax' px. Evita abrir uma foto de 12 MP inteira na memória pra mostrar 300 px.
fun fatorDeAmostragem(largura: Int, altura: Int, ladoMax: Int): Int {
    if (largura <= 0 || altura <= 0 || ladoMax <= 0) return 1
    val maior = maxOf(largura, altura)
    var fator = 1
    while (maior / (fator * 2) >= ladoMax) fator *= 2
    return fator
}
