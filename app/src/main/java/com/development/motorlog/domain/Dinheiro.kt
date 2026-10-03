package com.development.motorlog.domain

// Dinheiro no app é guardado em CENTAVOS (Int): R$ 45,90 = 4590. Desde o schema v12 — antes era
// real inteiro e "45,90" não cabia (o campo ficava vermelho, ou o preço virava R$ 0).

// Maior valor aceito num campo: R$ 10 milhões (folga enorme e longe do limite do Int em centavos)
private const val REAIS_MAXIMO = 10_000_000

// Lê o que a pessoa digita: "45", "45,9", "45,90", "1.234,50", "R$ 45,90" e também "45.90"
// (teclado com ponto). Ponto seguido de 3 dígitos é milhar ("1.234" = mil duzentos e trinta e
// quatro reais), como no talão da oficina. Devolve centavos, ou null se não der pra entender.
// Campo vazio também é null: quem chama decide se vazio vale 0 (valor opcional) ou é erro.
fun lerReais(texto: String): Int? {
    val limpo = texto.replace("R$", "").replace(" ", "").replace(' '.toString(), "")
    if (limpo.isEmpty()) return null
    val ixDecimal = when {
        ',' in limpo -> limpo.lastIndexOf(',')
        '.' in limpo && limpo.length - limpo.lastIndexOf('.') - 1 in 1..2 -> limpo.lastIndexOf('.')
        else -> -1
    }
    val inteira = (if (ixDecimal >= 0) limpo.substring(0, ixDecimal) else limpo).replace(".", "")
    val decimal = if (ixDecimal >= 0) limpo.substring(ixDecimal + 1) else ""
    if (inteira.any { !it.isDigit() } || decimal.any { !it.isDigit() } || decimal.length > 2) return null
    if (inteira.isEmpty() && decimal.isEmpty()) return null
    if (inteira.length > 8) return null
    val reais = inteira.ifEmpty { "0" }.toInt()
    if (reais > REAIS_MAXIMO) return null
    val centavos = decimal.padEnd(2, '0').toInt()
    return reais * 100 + centavos
}

// Centavos → texto pra editar num campo e pro CSV do backup: 4590 → "45,90", 4500 → "45".
// Sem ponto de milhar (lerReais aceita os dois jeitos).
fun reaisParaTexto(centavos: Int): String {
    val reais = centavos / 100
    val resto = centavos % 100
    return if (resto == 0) "$reais" else "$reais,${resto.toString().padStart(2, '0')}"
}
