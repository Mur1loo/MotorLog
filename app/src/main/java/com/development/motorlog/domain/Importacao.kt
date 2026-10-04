package com.development.motorlog.domain

// Leitura do CSV gerado por montarExportacao (o inverso dele). Puro: a data chega como texto e
// quem sabe ler "dd/MM/yyyy" em UTC é a UI (parseData). Linhas que não entende viram avisos, não erro.
// Preço e custo vêm em reais ("45,90", ou "45" nos backups de antes dos centavos) e viram centavos.
// apelido/chegouEm/kmChegada: colunas da v14 (backups antigos não têm → sem apelido, chegada desconhecida);
// vendidaEm: coluna da v16 (backups antigos não têm → moto na garagem)
data class MotoImportada(
    val modelo: String, val placa: String, val ano: Int, val km: Int, val kmAtualizadoEm: Long?, val intervaloRevisaoKm: Int = 0, val cor: Int = -1,
    val apelido: String = "", val chegouEm: Long? = null, val kmChegada: Int = -1, val vendidaEm: Long? = null,
) {
    val chave get() = "$modelo $placa".trim()
}
data class TrocaImportada(val motoChave: String, val peca: String, val km: Int, val preco: Int, val emServico: Boolean, val data: Long? = null)
data class ServicoImportado(val motoChave: String, val tipo: String, val data: Long?, val km: Int, val custo: Int, val oficina: String)
data class PecaImportada(val nome: String, val intervaloKm: Int)
data class CuidadoImportado(val motoChave: String, val tipo: String, val data: Long?, val km: Int, val nota: String = "")
data class DesejoImportado(
    val motoChave: String, val nome: String, val preco: Int, val nota: String, val criadoEm: Long?, val instaladoEm: Long?, val kmInstalado: Int,
)
data class AbastecimentoImportado(val motoChave: String, val km: Int, val mililitros: Int, val valor: Int, val tanqueCheio: Boolean, val data: Long?)

data class DadosImportados(
    val motos: List<MotoImportada>,
    val trocas: List<TrocaImportada>,
    val servicos: List<ServicoImportado>,
    val pecas: List<PecaImportada>,
    val avisos: List<String>,
    // backups de antes do abastecimento não têm a seção
    val abastecimentos: List<AbastecimentoImportado> = emptyList(),
    // backups de antes do diário de cuidados não têm a seção
    val cuidados: List<CuidadoImportado> = emptyList(),
    // backups de antes da lista de desejos não têm a seção
    val desejos: List<DesejoImportado> = emptyList(),
) {
    val vazio get() = motos.isEmpty() && trocas.isEmpty() && servicos.isEmpty() && pecas.isEmpty() && abastecimentos.isEmpty() && cuidados.isEmpty() && desejos.isEmpty()
}

private enum class Secao { NENHUMA, MOTOS, TROCAS, SERVICOS, ABASTECIMENTOS, CUIDADOS, DESEJOS, PECAS }

fun lerExportacao(texto: String, parseData: (String) -> Long?): DadosImportados {
    val motos = mutableListOf<MotoImportada>()
    val trocas = mutableListOf<TrocaImportada>()
    val servicos = mutableListOf<ServicoImportado>()
    val pecas = mutableListOf<PecaImportada>()
    val abastecimentos = mutableListOf<AbastecimentoImportado>()
    val cuidados = mutableListOf<CuidadoImportado>()
    val desejos = mutableListOf<DesejoImportado>()
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
            "ABASTECIMENTOS" -> Secao.ABASTECIMENTOS
            "CUIDADOS" -> Secao.CUIDADOS
            "DESEJOS" -> Secao.DESEJOS
            "PEÇAS (CATÁLOGO)", "PECAS (CATALOGO)", "PEÇAS", "PECAS" -> Secao.PECAS
            else -> null
        }
        if (nova != null) { secao = nova; pularCabecalho = true; return@forEachIndexed }
        if (pularCabecalho) { pularCabecalho = false; return@forEachIndexed }   // linha de nomes das colunas

        val c = linha.split(';').map { it.trim() }
        fun int(ix: Int) = c.getOrNull(ix)?.toIntOrNull()
        fun reais(ix: Int) = c.getOrNull(ix)?.let(::lerReais)
        val ok = when (secao) {
            Secao.MOTOS -> if (c.size >= 4 && int(2) != null && int(3) != null) {
                motos += MotoImportada(
                    c[0], c[1], int(2)!!, int(3)!!, c.getOrNull(4)?.takeIf { it.isNotBlank() }?.let(parseData), int(5) ?: 0, int(6) ?: -1,
                    apelido = c.getOrNull(7) ?: "", chegouEm = c.getOrNull(8)?.takeIf { it.isNotBlank() }?.let(parseData), kmChegada = int(9) ?: -1,
                    vendidaEm = c.getOrNull(10)?.takeIf { it.isNotBlank() }?.let(parseData),
                ); true
            } else false
            Secao.TROCAS -> if (c.size >= 3 && int(2) != null) {
                trocas += TrocaImportada(c[0], c[1], int(2)!!, reais(3) ?: 0, c.getOrNull(4)?.lowercase() == "sim", c.getOrNull(5)?.takeIf { it.isNotBlank() }?.let(parseData)); true
            } else false
            Secao.SERVICOS -> if (c.size >= 5 && int(3) != null && reais(4) != null) {
                servicos += ServicoImportado(c[0], c[1], parseData(c[2]), int(3)!!, reais(4)!!, c.getOrNull(5) ?: ""); true
            } else false
            Secao.ABASTECIMENTOS -> if (c.size >= 3 && int(1) != null && c.getOrNull(2)?.let(::lerLitros) != null) {
                abastecimentos += AbastecimentoImportado(
                    c[0], int(1)!!, lerLitros(c[2])!!, reais(3) ?: 0,
                    tanqueCheio = c.getOrNull(4)?.lowercase() != "não", data = c.getOrNull(5)?.takeIf { it.isNotBlank() }?.let(parseData),
                ); true
            } else false
            Secao.CUIDADOS -> if (c.size >= 4 && c[1].isNotBlank() && int(3) != null) {
                cuidados += CuidadoImportado(c[0], c[1], parseData(c[2]), int(3)!!, c.getOrNull(4) ?: ""); true
            } else false
            Secao.DESEJOS -> if (c.size >= 2 && c[1].isNotBlank()) {
                desejos += DesejoImportado(
                    c[0], c[1], reais(2) ?: 0, c.getOrNull(3) ?: "",
                    criadoEm = c.getOrNull(4)?.takeIf { it.isNotBlank() }?.let(parseData),
                    instaladoEm = c.getOrNull(5)?.takeIf { it.isNotBlank() }?.let(parseData),
                    kmInstalado = int(6) ?: -1,
                ); true
            } else false
            Secao.PECAS -> if (c.size >= 2 && int(1) != null && int(1)!! > 0) {
                pecas += PecaImportada(c[0], int(1)!!); true
            } else false
            Secao.NENHUMA -> false
        }
        if (!ok) avisos += "linha ${i + 1} ignorada: \"${linha.take(40)}\""
    }
    return DadosImportados(motos, trocas, servicos, pecas, avisos, abastecimentos, cuidados, desejos)
}
