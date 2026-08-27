package com.floating.stopwatch.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.R

val DiwaniFontFamily = FontFamily(
    Font(R.font.ulamjad, FontWeight.Light),
    Font(R.font.ulamjad, FontWeight.Normal),
    Font(R.font.ulamjad, FontWeight.Medium),
    Font(R.font.ulamjad, FontWeight.Bold)
)

object NumericTypographyTokens {
    val BaseStopwatchSize: Float = 54f
    val BaseCountdownSize: Float = 54f
    val BaseCounterSize: Float = 72f
    val BaseIntervalSize: Float = 48f
    val BaseLegacySize: Float = 48f
    val BaseFloatingWidgetSize: Float = 24f

    fun getScaledFontSize(baseSp: Float, userScale: Float): TextUnit {
        val clampedScale = userScale.coerceIn(0.60f, 2.00f)
        return (baseSp * clampedScale).sp
    }

    val TabularDigitsTextStyle = TextStyle(
        fontFamily = DiwaniFontFamily,
        fontWeight = FontWeight.Light,
        fontFeatureSettings = "tnum"
    )
}
