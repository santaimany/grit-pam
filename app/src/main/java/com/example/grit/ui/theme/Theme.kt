package com.example.grit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GritColorScheme = lightColorScheme(
    primary = GritGreen,
    onPrimary = BackgroundWhite,
    primaryContainer = GritGreenLight,
    secondary = GritGreenDark,
    background = BackgroundWhite,
    surface = BackgroundWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun GritTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GritColorScheme,
        typography = Typography,
        content = content
    )
}
