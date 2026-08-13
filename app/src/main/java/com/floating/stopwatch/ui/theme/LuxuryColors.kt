package com.floating.stopwatch.ui.theme

import androidx.compose.ui.graphics.Color

object LuxuryColors {
    val Background = Color(0x0F, 0x0F, 0x0F) // #0A0A0A or dark warm black
    val WarmBlack = Color(0xFF0A0A0A)
    val CreamyWhite = Color(0xFFF5F3EF)
    val WarmGray = Color(0xFF8A8680)
    val AccentGold = Color(0xFFC9A66B)

    // Additional Accent presets
    val GalaxyBlue = Color(0xFF2C5E8A)
    val Titanium = Color(0xFF5A6065)
    val Emerald = Color(0xFF3F826D)
    val Sapphire = Color(0xFF1F4E79)
    val Violet = Color(0xFF6B4C8C)
    val Rose = Color(0xFFB85C77)
    val Ice = Color(0xFF8BB5C4)
    val Amber = Color(0xFFD97D36)
    val PureWhite = Color(0xFFFFFFFF)

    fun fromName(name: String): Color {
        return when (name) {
            "Galaxy Blue" -> GalaxyBlue
            "Titanium" -> Titanium
            "Emerald" -> Emerald
            "Sapphire" -> Sapphire
            "Violet" -> Violet
            "Rose" -> Rose
            "Ice" -> Ice
            "Amber" -> Amber
            "Pure White" -> PureWhite
            else -> AccentGold
        }
    }
}
