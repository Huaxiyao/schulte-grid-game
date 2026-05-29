package com.schulte.grid.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = THEMES[0].lightAccent,
    onPrimary = THEMES[0].lightSurface,
    primaryContainer = THEMES[0].lightAccentLight,
    onPrimaryContainer = THEMES[0].lightAccent,
    secondary = THEMES[0].lightCorrect,
    onSecondary = THEMES[0].lightSurface,
    background = THEMES[0].lightBackground,
    onBackground = THEMES[0].lightText,
    surface = THEMES[0].lightSurface,
    onSurface = THEMES[0].lightText,
    surfaceVariant = THEMES[0].lightGridBg,
    onSurfaceVariant = THEMES[0].lightSub,
    outline = THEMES[0].lightBorder,
    error = THEMES[0].lightWrong,
    onError = THEMES[0].lightSurface,
)

private val DarkColorScheme = darkColorScheme(
    primary = THEMES[0].darkAccent,
    onPrimary = THEMES[0].darkBackground,
    primaryContainer = THEMES[0].darkAccentLight,
    onPrimaryContainer = THEMES[0].darkAccent,
    secondary = THEMES[0].darkCorrect,
    onSecondary = THEMES[0].darkBackground,
    background = THEMES[0].darkBackground,
    onBackground = THEMES[0].darkText,
    surface = THEMES[0].darkSurface,
    onSurface = THEMES[0].darkText,
    surfaceVariant = THEMES[0].darkGridBg,
    onSurfaceVariant = THEMES[0].darkSub,
    outline = THEMES[0].darkBorder,
    error = THEMES[0].darkWrong,
    onError = THEMES[0].darkBackground,
)

/** 根据主题索引生成动态的 ColorScheme */
private fun getLightColorScheme(themeIndex: Int) = lightColorScheme(
    primary = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightAccent,
    onPrimary = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightSurface,
    primaryContainer = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightAccentLight,
    onPrimaryContainer = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightAccent,
    secondary = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightCorrect,
    onSecondary = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightSurface,
    background = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightBackground,
    onBackground = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightText,
    surface = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightSurface,
    onSurface = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightText,
    surfaceVariant = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightGridBg,
    onSurfaceVariant = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightSub,
    outline = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightBorder,
    error = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightWrong,
    onError = THEMES.getOrElse(themeIndex) { THEMES[0] }.lightSurface,
)

private fun getDarkColorScheme(themeIndex: Int) = darkColorScheme(
    primary = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkAccent,
    onPrimary = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkBackground,
    primaryContainer = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkAccentLight,
    onPrimaryContainer = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkAccent,
    secondary = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkCorrect,
    onSecondary = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkBackground,
    background = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkBackground,
    onBackground = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkText,
    surface = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkSurface,
    onSurface = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkText,
    surfaceVariant = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkGridBg,
    onSurfaceVariant = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkSub,
    outline = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkBorder,
    error = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkWrong,
    onError = THEMES.getOrElse(themeIndex) { THEMES[0] }.darkBackground,
)

@Composable
fun SchulteGridTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    themeIndex: Int = 0,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkMode) getDarkColorScheme(themeIndex) else getLightColorScheme(themeIndex)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
