package com.example.costostock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CostoStockColorScheme = lightColorScheme(
    primary = CostoOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = CostoOrangeContainer,
    onPrimaryContainer = CostoTextDark,

    secondary = CostoOrangeSecondary,
    onSecondary = Color.White,
    secondaryContainer = CostoOrangeContainer,
    onSecondaryContainer = CostoOrangePrimary,

    background = CostoBackground,
    onBackground = CostoTextDark,

    surface = CostoSurface,
    onSurface = CostoTextDark,
    surfaceVariant = CostoSurfaceVariant,
    onSurfaceVariant = CostoGrayText,

    outline = Color(0xFFD6C7BF)
)

@Composable
fun CostoStockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CostoStockColorScheme,
        content = content
    )
}