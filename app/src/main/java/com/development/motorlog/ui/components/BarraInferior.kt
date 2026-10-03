package com.development.motorlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.motorlog.R
import com.development.motorlog.ui.theme.MlBgElev
import com.development.motorlog.ui.theme.MlBorder
import com.development.motorlog.ui.theme.MlTextFaint
import com.development.motorlog.ui.theme.chakra

enum class AbaMoto(val rotulo: String, val icone: Int) {
    PAINEL("Painel", R.drawable.ic_ml_gauge),
    HISTORICO("Histórico", R.drawable.ic_ml_doc),
    TROCAS("Trocas", R.drawable.ic_ml_wrench),
    FOTOS("Fotos", R.drawable.ic_ml_camera),
}

// Barra inferior do protótipo (BottomNav): 2 abas + FAB "+KM" central + 2 abas.
// Só aparece no contexto de uma moto; o FAB é a ação nº 1 do app. A Garagem virou o ícone de
// moto no cabeçalho, pra dar lugar à aba Fotos (o álbum merece destaque, não o fim do Painel).
// mostrarAtualizarKm = false na aba Painel: lá o "Atualizar km" grande já é a ação principal, e dois
// botões laranja iguais na mesma tela disputam a atenção.
@Composable
fun BarraInferior(ativa: AbaMoto, aoNavegar: (AbaMoto) -> Unit, aoAtualizarKm: () -> Unit, mostrarAtualizarKm: Boolean = true) {
    val accent = MaterialTheme.colorScheme.primary
    Column(Modifier.fillMaxWidth().background(MlBgElev)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(MlBorder))
        Row(
            // em paisagem/tablet a barra não se espalha: largura máxima de celular, centralizada
            modifier = Modifier.fillMaxWidth().wrapContentWidth().widthIn(max = 560.dp).fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp).navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ItemAba(AbaMoto.PAINEL, ativa == AbaMoto.PAINEL) { aoNavegar(AbaMoto.PAINEL) }
            ItemAba(AbaMoto.HISTORICO, ativa == AbaMoto.HISTORICO) { aoNavegar(AbaMoto.HISTORICO) }
            if (mostrarAtualizarKm) {
                Column(
                    modifier = Modifier
                        .offset(y = (-18).dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = accent, spotColor = accent)
                        .size(60.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(accent)
                        .clickable { aoAtualizarKm() },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_ml_gauge), contentDescription = "Atualizar km", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(23.dp))
                    Text("+KM", style = chakra(9.sp), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            ItemAba(AbaMoto.TROCAS, ativa == AbaMoto.TROCAS) { aoNavegar(AbaMoto.TROCAS) }
            ItemAba(AbaMoto.FOTOS, ativa == AbaMoto.FOTOS) { aoNavegar(AbaMoto.FOTOS) }
        }
    }
}

@Composable
private fun ItemAba(aba: AbaMoto, ativa: Boolean, onClick: () -> Unit) {
    val cor = if (ativa) MaterialTheme.colorScheme.primary else MlTextFaint
    Column(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(painterResource(aba.icone), contentDescription = aba.rotulo, tint = cor, modifier = Modifier.size(23.dp))
        Spacer(Modifier.height(3.dp))
        Text(aba.rotulo, fontSize = 10.5.sp, fontWeight = if (ativa) FontWeight.Bold else FontWeight.Medium, color = cor)
    }
}
