package com.development.motorlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.development.motorlog.R
import com.development.motorlog.ui.theme.MlBorder

// "Foto" da moto: tile escuro com gradiente da cor da moto, losango no canto e o glifo de moto.
@Composable
fun BikeBadge(accent: Color, modifier: Modifier = Modifier, tamanho: Dp = 56.dp, raio: Dp = 16.dp) {
    Box(
        modifier = modifier
            .size(tamanho)
            .clip(RoundedCornerShape(raio))
            .background(Color(0xFF0B0D11))
            .background(Brush.linearGradient(0f to accent.copy(alpha = 0.2f), 0.55f to Color.Transparent))
            .border(1.dp, MlBorder, RoundedCornerShape(raio)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(tamanho * 0.5f)
                .offset(x = tamanho * 0.42f, y = tamanho * 0.42f)
                .rotate(45f)
                .clip(RoundedCornerShape(6.dp))
                .background(accent.copy(alpha = 0.9f)),
        )
        Icon(
            painterResource(R.drawable.ic_ml_moto), contentDescription = null,
            tint = Color.White, modifier = Modifier.size(tamanho * 0.52f),
        )
    }
}
