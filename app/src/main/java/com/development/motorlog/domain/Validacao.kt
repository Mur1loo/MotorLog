package com.development.motorlog.domain

// Validação dos formulários, campo a campo: cada erro diz O QUE corrigir e aparece embaixo do
// próprio campo (antes era uma frase genérica no rodapé, sem dizer qual campo). null = campo ok.
// Placa (moto) e nome da oficina (visita) são opcionais: não travam um registro rápido.

const val ANO_MINIMO = 1900
const val ANO_MAXIMO = 2100

// Km digitado num campo: obrigatório, só números, sem negativo
fun erroDeKm(texto: String): String? = when {
    texto.isBlank() -> "Informe o km"
    texto.trim().toIntOrNull()?.takeIf { it >= 0 } == null -> "Use só números, ex.: 16100"
    else -> null
}

data class ErrosMoto(val modelo: String?, val ano: String?, val km: String?, val revisao: String?, val kmChegada: String? = null) {
    val ok get() = modelo == null && ano == null && km == null && revisao == null && kmChegada == null
}

// km = null quando o campo não existe (edição: o km muda em "Atualizar km").
// kmChegada é opcional (vazio = não lembra).
fun validarMoto(modelo: String, ano: String, km: String?, revisao: String, kmChegada: String = "") = ErrosMoto(
    modelo = if (modelo.isBlank()) "Informe o modelo, ex.: Fan 160" else null,
    ano = when {
        ano.isBlank() -> "Informe o ano"
        ano.trim().toIntOrNull()?.takeIf { it in ANO_MINIMO..ANO_MAXIMO } == null -> "Ano com 4 números, ex.: 2020"
        else -> null
    },
    km = km?.let(::erroDeKm),
    revisao = if (revisao.isNotBlank() && revisao.trim().toIntOrNull()?.takeIf { it >= 0 } == null)
        "Use só números, ou deixe vazio pra não avisar" else null,
    kmChegada = if (kmChegada.isNotBlank() && kmChegada.trim().toIntOrNull()?.takeIf { it >= 0 } == null)
        "Use só números, ou deixe vazio se não lembrar" else null,
)

data class ErrosServico(val tipo: String?, val custo: String?, val km: String?) {
    val ok get() = tipo == null && custo == null && km == null
}

fun validarServico(tipo: String, custo: String, km: String) = ErrosServico(
    tipo = if (tipo.isBlank()) "Escolha acima ou escreva o que foi feito" else null,
    custo = when {
        custo.isBlank() -> "Informe o valor pago"
        lerReais(custo) == null -> "Use só números, ex.: 45,90"
        else -> null
    },
    km = erroDeKm(km),
)

data class ErrosPeca(val nome: String?, val intervalo: String?) {
    val ok get() = nome == null && intervalo == null
}

// intervalo 0 marcaria VENCIDA no instante da troca; negativo inverte a conta
fun validarPeca(nome: String, intervalo: String) = ErrosPeca(
    nome = if (nome.isBlank()) "Informe o nome da peça" else null,
    intervalo = if (intervalo.trim().toIntOrNull()?.takeIf { it > 0 } == null) "Informe os km entre uma troca e outra, ex.: 3000" else null,
)

data class ErrosAbastecimento(val km: String?, val litros: String?, val valor: String?) {
    val ok get() = km == null && litros == null && valor == null
}

// valor é opcional (sem ele não dá custo por km, mas o consumo sai igual)
fun validarAbastecimento(km: String, litros: String, valor: String) = ErrosAbastecimento(
    km = erroDeKm(km),
    litros = when {
        litros.isBlank() -> "Informe quantos litros entraram"
        lerLitros(litros) == null -> "Use só números, ex.: 8,5"
        else -> null
    },
    valor = if (valor.isNotBlank() && lerReais(valor) == null) "Use só números, ex.: 45,90" else null,
)
