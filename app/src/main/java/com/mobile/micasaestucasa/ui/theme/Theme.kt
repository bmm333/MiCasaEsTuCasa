package com.mobile.micasaestucasa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = Primario,
    onPrimary = Color.White,
    secondary = Secondary,
    onSecondary = Color.White,
    tertiary = Accenti,
    onTertiary = Color.White,
    background = ScreenBackground,
    onBackground = HeadingText,
    surface = CardSurface,
    onSurface = HeadingText,
    surfaceVariant = SkeletonLoader,
    onSurfaceVariant = SecondaryText,
    error = ErrorColor,
    onError = Color.White,
    outline = BorderDivider
)

@Composable
fun MiCasaEsTuCasaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}