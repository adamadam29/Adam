package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color.Black,
    secondary = ChromaPink,
    onSecondary = Color.White,
    tertiary = FluidPurple,
    background = DeepObsidian,
    onBackground = NeutralWhite,
    surface = LightSlate,
    onSurface = NeutralWhite,
    surfaceVariant = DarkGreyContainer,
    onSurfaceVariant = MutedGrey,
    outline = LightGreyBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Theme for Cinematic workspace by default
    dynamicColor: Boolean = false, // Disable dynamic colors to keep brand consistency
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
