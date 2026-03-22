package com.dnodevelopment.padelcompanion.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val PadelGreen = Color(0xFF2E7D32)
val PadelGreenLight = Color(0xFF4CAF50)
val PadelGreenDark = Color(0xFF1B5E20)
val PadelAmber = Color(0xFFFFCA28)
val PadelBlue = Color(0xFF42A5F5)
val ScoreBackground = Color(0xFF1A1A2E)
val CardBackground = Color(0xFF16213E)
val TextPrimary = Color(0xFFF5F5F5)
val TextSecondary = Color(0xFFB0BEC5)

private val PadelColorPalette = Colors(
    primary = PadelGreenLight,
    primaryVariant = PadelGreenDark,
    secondary = PadelAmber,
    secondaryVariant = PadelBlue,
    background = ScoreBackground,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun PadelCompanionTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = PadelColorPalette,
        content = content
    )
}
