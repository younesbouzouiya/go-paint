package com.gopaint.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

private val GoPaintColorScheme = darkColorScheme(
    primary = BrandBlue,
    secondary = BrandPurple,
    tertiary = BrandPink,
    background = BgApp,
    surface = CardBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderColor,
)

val BrandGradient = Brush.horizontalGradient(listOf(BrandBlue, BrandPurple, BrandPink))
val BrandGradientDiagonal = Brush.linearGradient(listOf(BrandBlue, BrandPurple, BrandPink))

@Composable
fun GoPaintTheme(content: @Composable () -> Unit) {
    // GO Paint! always renders in its own dark brand theme, regardless of system setting.
    MaterialTheme(
        colorScheme = GoPaintColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
