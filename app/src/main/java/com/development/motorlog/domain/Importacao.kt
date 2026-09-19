package com.development.motorlog.domain

// Leitura do CSV gerado por montarExportacao (o inverso dele). Puro: a data chega como texto e
// quem sabe ler "dd/MM/yyyy" em UTC é a UI (parseData). Linhas que não entende viram avisos, não erro.
data class MotoImportada(val modelo: String, val placa: String, val ano: Int, val km: Int, val kmAtualizadoEm: Long?) {
    val chave get() = "$modelo $placa".trim()
}
data class TrocaImportada(val motoChave: String, val peca: String, val km: Int, val preco: Int, val emServico: Boolean)
data class ServicoImportado(val motoChave: String, val tipo: String, val data: Long?, val km: Int, val custo: Int, val oficina: String)
data class PecaImportada(val nome: String, val intervaloKm: Int)

data class DadosImportados(
    val motos: List<MotoImportada>,
    val trocas: List<TrocaImportada>,
    val servicos: List<ServicoImportado>,
    val pecas: List<PecaImportada>,
    val avisos: List<String>,
) {
    val vazio get() = motos.isEmpty() && trocas.isEmpty() && servicos.isEmpty() && pecas.isEmpty()
}

private enum class Secao { NENHUMA, MOTOS, TROCAS, SERVICOS, PECAS }

fun lerExportacao(texto: String, parseData: (String) -> Long?): DadosImportados {
    val motos = mutableListOf<MotoImportada>()
    val trocas = mutableListOf<TrocaImportada>()
    val servicos = mutableListOf<ServicoImportado>()
    val pecas = mutableListOf<PecaImportada>()
    val avisos = mutableListOf<String>()
    var secao = Secao.NENHUMA
    var pularCabecalho = false

    texto.lines().forEachIndexed { i, bruta ->
        val linha = bruta.trim()
        if (linha.isEmpty() || linha == "MotorLog") return@forEachIndexed
        val nova = when (linha.uppercase()) {
            "MOTOS" -> Secao.MOTOS
            "TROCAS" -> Secao.TROCAS
            "SERVIÇOS", "SERVICOS" -> Secao.SERVICOS
            "PEÇAS (CATÁLOGO)", "PECAS (CATALOGO)", "PEÇAS", "PECAS" -> Secao.PECAS
            else -> null
        }
        if (nova != null) { secao = nova; pularCabecalho = true; return@forEachIndexed }
        if (pularCabecalho) { pularCabecalho = false; return@forEachIndexed }   // linha de nomes das colunas

        val c = linha.split(';').map { it.trim() }
        fun int(ix: Int) = c.getOrNull(ix)?.toIntOrNull()
        val ok = when (secao) {
            Secao.MOTOS -> if (c.size >= 4 && int(2) != null && int(3) != null) {
                motos += MotoImportada(c[0], c[1], int(2)!!, int(3)!!, c.getOrNull(4)?.takeIf { it.isNotBlank() }?.let(parseData)); true
            } else false
            Secao.TROCAS -> if (c.size >= 3 && int(2) != null) {
                trocas += TrocaImportada(c[0], c[1], int(2)!!, int(3) ?: 0, c.getOrNull(4)?.lowercase() == "sim"); true
            } else false
            Secao.SERVICOS -> if (c.size >= 5 && int(3) != null && int(4) != null) {
                servicos += ServicoImportado(c[0], c[1], parseData(c[2]), int(3)!!, int(4)!!, c.getOrNull(5) ?: ""); true
            } else false
            Secao.PECAS -> if (c.size >= 2 && int(1) != null && int(1)!! > 0) {
                pecas += PecaImportada(c[0], int(1)!!); true
            } else false
            Secao.NENHUMA -> false
        }
        if (!ok) avisos += "linha ${i + 1} ignorada: \"${linha.take(40)}\""
    }
    return DadosImportados(motos, trocas, servicos, pecas, avisos)
}
