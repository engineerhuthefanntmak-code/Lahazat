package com.floating.stopwatch.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.getValue
import com.floating.stopwatch.ui.theme.LuxuryColors
import com.floating.stopwatch.ui.theme.DiwaniFontFamily
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RollingDigit(
    digit: Char,
    modifier: Modifier = Modifier,
    style: TextStyle
) {
    AnimatedContent(
        targetState = digit,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically { height -> height } + fadeIn(animationSpec = tween(180, easing = EaseOutQuint)))
                    .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(180, easing = EaseOutQuint)))
            } else {
                (slideInVertically { height -> -height } + fadeIn(animationSpec = tween(180, easing = EaseOutQuint)))
                    .togetherWith(slideOutVertically { height -> height } + fadeOut(animationSpec = tween(180, easing = EaseOutQuint)))
            }
        },
        label = "RollingDigit"
    ) { targetDigit ->
        Text(
            text = targetDigit.toString(),
            style = style.copy(fontFeatureSettings = "tnum"),
            modifier = modifier
        )
    }
}

@Composable
fun TimeDisplay(
    elapsedTimeMs: Long,
    showCentiseconds: Boolean,
    modifier: Modifier = Modifier,
    baseStyle: TextStyle,
    scaleFactor: Float = 1.0f,
    accentColor: Color? = null,
    gradientGoldEnabled: Boolean = false,
    isVertical: Boolean = false
) {
    val totalSeconds = elapsedTimeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val centiseconds = (elapsedTimeMs % 1000) / 10

    val hourStr = String.format(Locale.US, "%02d", hours)
    val minuteStr = String.format(Locale.US, "%02d", minutes)
    val secondStr = String.format(Locale.US, "%02d", seconds)
    val centiStr = String.format(Locale.US, "%02d", centiseconds)

    val showHours = hours > 0

    val totalSecondsForFlash = elapsedTimeMs / 1000
    val isFullMinute = elapsedTimeMs > 0 && totalSecondsForFlash % 60 == 0L && (elapsedTimeMs % 1000) < 600

    val effectiveColor = if (gradientGoldEnabled) (accentColor ?: LuxuryColors.AccentGold) else baseStyle.color
    val animatedFlashColor by animateColorAsState(
        targetValue = if (isFullMinute) (accentColor ?: LuxuryColors.AccentGold) else effectiveColor,
        animationSpec = tween(durationMillis = 200),
        label = "MinuteFlash"
    )

    val scaledMainSize = (baseStyle.fontSize.value * scaleFactor).sp

    val gradientBrush = if (gradientGoldEnabled) {
        Brush.linearGradient(
            colors = listOf(
                accentColor ?: LuxuryColors.AccentGold,
                Color.White,
                accentColor ?: LuxuryColors.AccentGold
            )
        )
    } else null

    val mainDigitStyle = if (gradientBrush != null) {
        baseStyle.copy(
            fontSize = scaledMainSize,
            fontFamily = DiwaniFontFamily,
            fontWeight = FontWeight.Bold,
            brush = gradientBrush,
            shadow = androidx.compose.ui.graphics.Shadow(
                color = Color.Black.copy(alpha = 0.35f),
                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                blurRadius = 4f
            )
        )
    } else {
        baseStyle.copy(
            fontSize = scaledMainSize,
            fontFamily = DiwaniFontFamily,
            fontWeight = FontWeight.Bold,
            color = animatedFlashColor,
            shadow = androidx.compose.ui.graphics.Shadow(
                color = Color.Black.copy(alpha = 0.35f),
                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                blurRadius = 4f
            )
        )
    }

    val centiSize = (scaledMainSize.value * 0.58f).sp
    val centiDigitStyle = if (gradientBrush != null) {
        val centiBrush = Brush.linearGradient(
            colors = listOf(
                (accentColor ?: LuxuryColors.AccentGold).copy(alpha = 0.7f),
                Color.White.copy(alpha = 0.7f),
                (accentColor ?: LuxuryColors.AccentGold).copy(alpha = 0.7f)
            )
        )
        mainDigitStyle.copy(
            fontSize = centiSize,
            fontWeight = FontWeight.Normal,
            brush = centiBrush
        )
    } else {
        mainDigitStyle.copy(
            fontSize = centiSize,
            fontWeight = FontWeight.Normal,
            color = animatedFlashColor.copy(alpha = 0.7f)
        )
    }

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
    ) {
        if (isVertical) {
            // Vertical layout presentation option (Section 2 - Item 6)
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (showHours) {
                    Row {
                        RollingDigit(digit = hourStr[0], style = mainDigitStyle)
                        RollingDigit(digit = hourStr[1], style = mainDigitStyle)
                    }
                }
                Row {
                    RollingDigit(digit = minuteStr[0], style = mainDigitStyle)
                    RollingDigit(digit = minuteStr[1], style = mainDigitStyle)
                }
                Row {
                    RollingDigit(digit = secondStr[0], style = mainDigitStyle)
                    RollingDigit(digit = secondStr[1], style = mainDigitStyle)
                }
                if (showCentiseconds) {
                    Row {
                        RollingDigit(digit = centiStr[0], style = centiDigitStyle)
                        RollingDigit(digit = centiStr[1], style = centiDigitStyle)
                    }
                }
            }
        } else {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                if (showHours) {
                    RollingDigit(digit = hourStr[0], style = mainDigitStyle)
                    RollingDigit(digit = hourStr[1], style = mainDigitStyle)
                    Text(text = ":", style = mainDigitStyle)
                }

                RollingDigit(digit = minuteStr[0], style = mainDigitStyle)
                RollingDigit(digit = minuteStr[1], style = mainDigitStyle)
                Text(text = ":", style = mainDigitStyle)

                RollingDigit(digit = secondStr[0], style = mainDigitStyle)
                RollingDigit(digit = secondStr[1], style = mainDigitStyle)

                if (showCentiseconds) {
                    Text(text = ".", style = centiDigitStyle)
                    RollingDigit(digit = centiStr[0], style = centiDigitStyle)
                    RollingDigit(digit = centiStr[1], style = centiDigitStyle)
                }
            }
        }
    }
}
