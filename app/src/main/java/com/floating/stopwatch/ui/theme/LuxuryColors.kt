package com.floating.stopwatch.ui.theme

import androidx.compose.ui.graphics.Color

object LuxuryColors {
    val Background = Color(0x0F, 0x0F, 0x0F)
    val WarmBlack = Color(0xFF0A0A0A)
    val SolidPopupBlack = Color(0xFF0A0A0A)
    val CreamyWhite = Color(0xFFF5F3EF)
    val WarmGray = Color(0xFF8A8680)
    val AccentGold = Color(0xFFC9A66B)

    // Premium Accent Tokens
    val ChampagneGold = Color(0xFFE6C687)
    val SoftGold = Color(0xFFD4AF37)
    val Platinum = Color(0xFFE5E4E2)
    val Silver = Color(0xFFC0C0C0)
    val Pearl = Color(0xFFEAE6DF)
    val Ivory = Color(0xFFFFFFF0)
    val Sand = Color(0xFFC2B280)
    val Bronze = Color(0xFFCD7F32)
    val Copper = Color(0xFFB87333)
    val DeepOlive = Color(0xFF6B8E23)
    val Slate = Color(0xFF708090)
    val RoyalNavy = Color(0xFF2B3E50)
    val Burgundy = Color(0xFF800020)
    val Espresso = Color(0xFF5C4033)
    val Graphite = Color(0xFF484848)

    fun fromName(name: String): Color {
        return when (name) {
            "Champagne Gold", "Gold" -> ChampagneGold
            "Soft Gold" -> SoftGold
            "Platinum" -> Platinum
            "Silver" -> Silver
            "Pearl" -> Pearl
            "Ivory" -> Ivory
            "Sand" -> Sand
            "Bronze" -> Bronze
            "Copper" -> Copper
            "Deep Olive" -> DeepOlive
            "Slate" -> Slate
            "Royal Navy" -> RoyalNavy
            "Burgundy" -> Burgundy
            "Espresso" -> Espresso
            "Graphite" -> Graphite
            else -> ChampagneGold
        }
    }
}
