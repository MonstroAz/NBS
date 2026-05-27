package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DeepMidnightColorScheme = darkColorScheme(
    primary = PolishPrimaryBlue,
    secondary = PolishCardLighter,
    tertiary = PolishBankerBg,
    background = PolishBackground,
    surface = PolishCard,
    surfaceVariant = PolishBorder,
    onBackground = PolishTextLight,
    onSurface = PolishTextLight,
    onPrimary = PolishPlayerText,
    onSecondary = PolishTextLight,
    onTertiary = PolishBankerText
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Force deep midnight casino theme for premium visual style
    MaterialTheme(
        colorScheme = DeepMidnightColorScheme,
        typography = Typography,
        content = content
    )
}
