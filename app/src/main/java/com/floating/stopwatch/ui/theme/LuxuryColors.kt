package com.floating.stopwatch.ui.theme

import androidx.compose.ui.graphics.Color

object LuxuryColors {
    val Background = Color(0x0F, 0x0F, 0x0F)
    val WarmBlack = Color(0xFF0A0A0A)
    val SolidPopupBlack = Color(0xFF0A0A0A)
    val CreamyWhite = Color(0xFFF5F3EF)
    val WarmGray = Color(0xFF8A8680)
    val AccentGold = Color(0xFFC9A66B)

    // 30 Design Tokens for Premium Color Accents
    val Champagne = Color(0xFFE6C687)
    val AntiqueGold = Color(0xFFC5A059)
    val BrushedGold = Color(0xFFD4AF37)
    val RoseGold = Color(0xFFB76E79)
    val PaleGold = Color(0xFFE6CA65)
    val Platinum = Color(0xFFE5E4E2)
    val Titanium = Color(0xFF878A8F)
    val Pearl = Color(0xFFEAE6DF)
    val Ivory = Color(0xFFFFFFF0)
    val Porcelain = Color(0xFFF2EEEC)
    val Sand = Color(0xFFC2B280)
    val Taupe = Color(0xFFB38B6D)
    val Bronze = Color(0xFFCD7F32)
    val Copper = Color(0xFFB87333)
    val DarkCopper = Color(0xFF8B4513)
    val Mocha = Color(0xFF967969)
    val Espresso = Color(0xFF5C4033)
    val DeepOlive = Color(0xFF6B8E23)
    val Sage = Color(0xFF9CAF88)
    val Emerald = Color(0xFF2E8B57)
    val Forest = Color(0xFF228B22)
    val Slate = Color(0xFF708090)
    val SteelBlue = Color(0xFF4682B4)
    val MidnightBlue = Color(0xFF191970)
    val RoyalNavy = Color(0xFF2B3E50)
    val DeepBurgundy = Color(0xFF6B1724)
    val Wine = Color(0xFF722F37)
    val Plum = Color(0xFF8E4585)
    val Graphite = Color(0xFF484848)
    val Charcoal = Color(0xFF36454F)

    fun fromName(name: String): Color {
        return when (name) {
            "Champagne", "Champagne Gold", "Gold" -> Champagne
            "Antique Gold" -> AntiqueGold
            "Brushed Gold", "Soft Gold" -> BrushedGold
            "Rose Gold" -> RoseGold
            "Pale Gold" -> PaleGold
            "Platinum" -> Platinum
            "Titanium" -> Titanium
            "Pearl" -> Pearl
            "Ivory" -> Ivory
            "Porcelain" -> Porcelain
            "Sand" -> Sand
            "Taupe" -> Taupe
            "Bronze" -> Bronze
            "Copper" -> Copper
            "Dark Copper" -> DarkCopper
            "Mocha" -> Mocha
            "Espresso" -> Espresso
            "Deep Olive" -> DeepOlive
            "Sage" -> Sage
            "Emerald" -> Emerald
            "Forest" -> Forest
            "Slate" -> Slate
            "Steel Blue" -> SteelBlue
            "Midnight Blue" -> MidnightBlue
            "Royal Navy" -> RoyalNavy
            "Deep Burgundy", "Burgundy" -> DeepBurgundy
            "Wine" -> Wine
            "Plum" -> Plum
            "Graphite" -> Graphite
            "Charcoal" -> Charcoal
            else -> Champagne
        }
    }
}
