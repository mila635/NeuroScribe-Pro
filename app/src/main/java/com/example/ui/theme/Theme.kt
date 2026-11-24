package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EnterpriseTealDark,
    onPrimary = DarkBackground,
    primaryContainer = EnterpriseTealSubtleDark,
    onPrimaryContainer = EnterpriseTealDark,
    secondary = EnterpriseTealDark,
    onSecondary = DarkBackground,
    secondaryContainer = DarkElevated,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = EnterpriseTealDark,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = StatusError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EnterpriseTealLight,
    onPrimary = Color.White,
    primaryContainer = EnterpriseTealSubtleLight,
    onPrimaryContainer = EnterpriseTealLight,
    secondary = EnterpriseTealLight,
    onSecondary = Color.White,
    secondaryContainer = LightElevated,
    onSecondaryContainer = LightTextPrimary,
    tertiary = EnterpriseTealLight,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = StatusError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

