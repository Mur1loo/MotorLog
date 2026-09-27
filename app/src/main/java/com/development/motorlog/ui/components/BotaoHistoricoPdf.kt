package com.development.motorlog.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.Moto
import com.development.motorlog.relatorio.compartilharPdf
import com.development.motorlog.ui.viewModels.RegistroViewModel

// "Compartilhar histórico (PDF)": gera o PDF da moto e abre o compartilhar do Android
// (WhatsApp, e-mail…). Usado no Painel e no Histórico.
@Composable
fun BotaoHistoricoPdf(
    moto: Moto,
    onMensagem: (String) -> Unit,
    modifier: Modifier = Modifier,
    registroViewModel: RegistroViewModel = viewModel(),
) {
    val contexto = LocalContext.current
    val gerando = registroViewModel.gerandoPdf
    BotaoSecundario(
        if (gerando) "Gerando o PDF…" else "Compartilhar histórico (PDF)",
        onClick = {
            registroViewModel.gerarHistoricoPdf(
                moto,
                aoPronto = { compartilharPdf(contexto, it, moto) },
                aoFalhar = { onMensagem("Não consegui gerar o PDF. Tente de novo.") },
            )
        },
        modifier = modifier,
        icone = R.drawable.ic_ml_share,
        enabled = !gerando,
    )
}
