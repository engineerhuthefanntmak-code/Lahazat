package com.floating.stopwatch.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = AlRayyashColors.GoldAccent,
    onPrimary = AlRayyashColors.BackgroundDeep,
    primaryContainer = AlRayyashColors.GoldSurface,
    onPrimaryContainer = AlRayyashColors.GoldAccentLight,
    background = AlRayyashColors.BackgroundDeep,
    onBackground = AlRayyashColors.TextPrimary,
    surface = AlRayyashColors.SurfaceDeep,
    onSurface = AlRayyashColors.TextPrimary,
    surfaceVariant = AlRayyashColors.SurfaceElevated,
    onSurfaceVariant = AlRayyashColors.TextSecondary,
    outline = AlRayyashColors.SurfaceBorder
)

@Composable
fun AlRayyashTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = AlRayyashTypography,
            content = content
        )
    }
}
