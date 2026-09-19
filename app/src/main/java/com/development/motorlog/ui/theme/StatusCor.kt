package com.development.motorlog.ui.theme

import androidx.compose.ui.graphics.Color
import com.development.motorlog.domain.StatusTroca

// O domínio dá o significado; a UI escolhe a cor. Único lugar que mapeia status → cor.
fun StatusTroca.cor(): Color = when (this) {
    StatusTroca.OK -> MlOk
    StatusTroca.PERTO -> MlSoon
    StatusTroca.VENCIDA -> MlOver
    StatusTroca.NUNCA_TROCADA -> MlTextFaint
}
