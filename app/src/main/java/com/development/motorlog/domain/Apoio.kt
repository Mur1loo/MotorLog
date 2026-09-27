package com.development.motorlog.domain

// Pedido de apoio (Pix) só pra quem o app já provou valor: pedir cedo ou toda hora é o jeito mais
// rápido de ganhar avaliação de 1 estrela. "Provou valor" = sinais de retenção, tirados só do que já
// está no celular (os dias em que o km foi registrado — o cadastro da moto conta como o 1º):
const val DIAS_MINIMOS_DE_USO = 14      // conhece o app há pelo menos 2 semanas
const val DIAS_COM_KM_MINIMOS = 6       // voltou pra atualizar o km em vários dias diferentes
const val DIAS_SEM_KM_MAXIMO = 7        // e continua usando (quem largou o app não é cobrado)
const val DIAS_ENTRE_PEDIDOS = 30       // no máximo 1 pedido por mês

// diasComKm: dias distintos (meia-noite UTC) com km registrado, de qualquer moto.
// ultimoPedido: dia do último pedido, 0 = nunca pediu.
fun deveMostrarPedidoDeApoio(diasComKm: List<Long>, ultimoPedido: Long, hoje: Long): Boolean {
    val dias = diasComKm.distinct()
    if (dias.size < DIAS_COM_KM_MINIMOS) return false
    if (diasEntre(dias.min(), hoje) < DIAS_MINIMOS_DE_USO) return false
    if (diasEntre(dias.max(), hoje) > DIAS_SEM_KM_MAXIMO) return false
    return ultimoPedido <= 0 || diasEntre(ultimoPedido, hoje) >= DIAS_ENTRE_PEDIDOS
}
