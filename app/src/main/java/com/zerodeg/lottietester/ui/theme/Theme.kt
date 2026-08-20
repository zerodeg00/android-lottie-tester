package com.zerodeg.lottietester.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF9C2F5B),
    secondary = Color(0xFF765663),
    tertiary = Color(0xFF745B00),
    background = Color(0xFFFFF8F7),
    surface = Color(0xFFFFF8F7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB0CA),
    secondary = Color(0xFFE5BDC9),
    tertiary = Color(0xFFE8C343),
)

@Composable
fun LottieTesterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
