package com.floating.stopwatch.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.getValue
import com.floating.stopwatch.ui.theme.LuxuryColors

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
    scaleFactor: Float = 1.0f
) {
    val totalSeconds = elapsedTimeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val centiseconds = (elapsedTimeMs % 1000) / 10

    val hourStr = String.format("%02d", hours)
    val minuteStr = String.format("%02d", minutes)
    val secondStr = String.format("%02d", seconds)
    val centiStr = String.format("%02d", centiseconds)

    val showHours = hours > 0

    val totalSecondsForFlash = elapsedTimeMs / 1000
    val isFullMinute = elapsedTimeMs > 0 && totalSecondsForFlash % 60 == 0L && (elapsedTimeMs % 1000) < 600

    val animatedFlashColor by animateColorAsState(
        targetValue = if (isFullMinute) LuxuryColors.AccentGold else baseStyle.color,
        animationSpec = tween(durationMillis = 200),
        label = "MinuteFlash"
    )

    val scaledMainSize = (baseStyle.fontSize.value * scaleFactor).sp
    val mainDigitStyle = baseStyle.copy(
        fontSize = scaledMainSize,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = animatedFlashColor
    )

    val centiSize = (scaledMainSize.value * 0.58f).sp
    val centiDigitStyle = mainDigitStyle.copy(
        fontSize = centiSize,
        fontWeight = FontWeight.Normal,
        color = animatedFlashColor.copy(alpha = 0.7f)
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.End // Forces RTL Layout display structure for numerical flows
    ) {
        if (showCentiseconds) {
            RollingDigit(digit = centiStr[1], style = centiDigitStyle)
            RollingDigit(digit = centiStr[0], style = centiDigitStyle)
            Text(text = ".", style = centiDigitStyle)
        }

        RollingDigit(digit = secondStr[1], style = mainDigitStyle)
        RollingDigit(digit = secondStr[0], style = mainDigitStyle)
        Text(text = ":", style = mainDigitStyle)

        RollingDigit(digit = minuteStr[1], style = mainDigitStyle)
        RollingDigit(digit = minuteStr[0], style = mainDigitStyle)

        if (showHours) {
            Text(text = ":", style = mainDigitStyle)
            RollingDigit(digit = hourStr[1], style = mainDigitStyle)
            RollingDigit(digit = hourStr[0], style = mainDigitStyle)
        }
    }
}
