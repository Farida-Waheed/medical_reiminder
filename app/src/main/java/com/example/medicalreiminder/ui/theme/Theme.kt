package com.example.medicalreiminder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = JaleesBlue80,
    secondary = JaleesTeal80,
    tertiary = JaleesEmergency80,
    background = Color(0xFF0F171B),
    surface = Color(0xFF17242B),
    onPrimary = Color(0xFF07334F),
    onSecondary = Color(0xFF073B49),
    onTertiary = Color(0xFF690005),
    onBackground = Color(0xFFE6EEF2),
    onSurface = Color(0xFFE6EEF2)
)

private val LightColorScheme = lightColorScheme(
    primary = JaleesBlue40,
    secondary = JaleesTeal40,
    tertiary = JaleesEmergency40,
    background = JaleesBackground,
    surface = JaleesSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = JaleesOnSurface,
    onSurface = JaleesOnSurface
)

@Composable
fun JaleesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
