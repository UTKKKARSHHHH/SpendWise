package com.spendwise.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class VeltoThemeColors(
    val canvas: Color,
    val card: Color,
    val cardElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val accentForest: Color,
    val accentLime: Color,
    val accentSage: Color,
    val isDark: Boolean
)

val LocalVeltoColors = compositionLocalOf {
    VeltoThemeColors(
        canvas = VeltoCanvasLight,
        card = VeltoCardLight,
        cardElevated = VeltoCardLight,
        textPrimary = VeltoTextPrimaryLight,
        textSecondary = VeltoTextSecondaryLight,
        textMuted = VeltoTextMutedLight,
        border = VeltoBorderLight,
        accentForest = VeltoForestGreen,
        accentLime = VeltoLime,
        accentSage = VeltoSage,
        isDark = false
    )
}

private val LightColorPalette = VeltoThemeColors(
    canvas = VeltoCanvasLight,
    card = VeltoCardLight,
    cardElevated = VeltoCardLight,
    textPrimary = VeltoTextPrimaryLight,
    textSecondary = VeltoTextSecondaryLight,
    textMuted = VeltoTextMutedLight,
    border = VeltoBorderLight,
    accentForest = VeltoForestGreen,
    accentLime = VeltoLime,
    accentSage = VeltoSage,
    isDark = false
)

private val DarkColorPalette = VeltoThemeColors(
    canvas = VeltoCanvasDark,
    card = VeltoCardDark,
    cardElevated = VeltoCardElevatedDark,
    textPrimary = VeltoTextPrimaryDark,
    textSecondary = VeltoTextSecondaryDark,
    textMuted = VeltoTextMutedDark,
    border = VeltoBorderDark,
    accentForest = VeltoForestGreen,
    accentLime = VeltoLime,
    accentSage = VeltoSageDark,
    isDark = true
)

private val LightMaterialColorScheme = lightColorScheme(
    primary = VeltoForestGreen,
    secondary = VeltoLime,
    background = VeltoCanvasLight,
    surface = VeltoCardLight,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = VeltoTextPrimaryLight,
    onSurface = VeltoTextPrimaryLight,
    error = ExpenseRed
)

private val DarkMaterialColorScheme = darkColorScheme(
    primary = VeltoLime,
    secondary = PrimaryEmerald,
    background = VeltoCanvasDark,
    surface = VeltoCardDark,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = VeltoTextPrimaryDark,
    onSurface = VeltoTextPrimaryDark,
    error = ExpenseRed
)

@Composable
fun SpendwiseTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    val materialScheme = if (darkTheme) DarkMaterialColorScheme else LightMaterialColorScheme

    CompositionLocalProvider(LocalVeltoColors provides colors) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = Typography,
            content = content
        )
    }
}
