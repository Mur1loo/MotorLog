package com.development.motorlog.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.development.motorlog.domain.SituacaoCuidado
import com.development.motorlog.domain.StatusTroca
import com.development.motorlog.ui.theme.MlFormas
import com.development.motorlog.ui.theme.cor
import com.development.motorlog.ui.util.iconeDoCuidado

// Card "Cuidados com a Pretinha" do Painel: os quatro rituais em botões de 1 toque, cada um com
// "há quanto tempo". Corrente e calibragem passadas do ponto ficam em amarelo (sem notificação).
@Composable
fun CardDeCuidados(
    titulo: String,
    situacoes: List<SituacaoCuidado>,
    accent: Color,
    onRegistrar: (SituacaoCuidado) -> Unit,
    onAbrirDiario: () -> Unit,
) {
    MlCard {
        SectionLabel(titulo, direita = { AcaoDeSecao("Diário", onAbrirDiario) })
        situacoes.chunked(2).forEachIndexed { i, linha ->
            if (i > 0) Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                linha.forEach { s -> BotaoDeCuidado(s, accent, Modifier.weight(1f)) { onRegistrar(s) } }
                if (linha.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BotaoDeCuidado(s: SituacaoCuidado, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    val corQuando = if (s.jaEstaNaHora) StatusTroca.PERTO.cor() else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(MlFormas.campo)
            .border(1.dp, MaterialTheme.colorScheme.outline, MlFormas.campo)
            .clickable(role = Role.Button, onClickLabel = "registrar hoje") { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(iconeDoCuidado(s.tipo)), contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.fillMaxWidth()) {
            Text(s.tipo.acao, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(s.quando, style = MaterialTheme.typography.bodySmall, color = corQuando, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
