package com.floating.stopwatch.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.R

val SulsFontFamily = FontFamily(
    Font(R.font.suls, FontWeight.Normal)
)

val AlRayyashTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SulsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        lineHeight = 52.sp,
        color = AlRayyashColors.TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = SulsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        color = AlRayyashColors.TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = SulsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = AlRayyashColors.TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = SulsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        color = AlRayyashColors.TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = SulsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = AlRayyashColors.TextSecondary
    ),
    bodyLarge = TextStyle(
        fontFamily = SulsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = AlRayyashColors.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = SulsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = AlRayyashColors.TextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = SulsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        color = AlRayyashColors.GoldAccent
    ),
    labelSmall = TextStyle(
        fontFamily = SulsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = AlRayyashColors.TextMuted
    )
)
