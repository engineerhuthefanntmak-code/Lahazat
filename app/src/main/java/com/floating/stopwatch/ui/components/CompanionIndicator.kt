package com.floating.stopwatch.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.ui.AppMode
import java.util.Locale

@Composable
fun CompanionIndicator(
    mode: AppMode,
    progressRatio: Float,
    isEnabled: Boolean,
    accentColor: Color,
    grayColor: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isEnabled) return

    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "CompanionProgress"
    )

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onToggle() })
            }
            .padding(vertical = 2.dp)
    ) {
        val labelText = if (mode == AppMode.Countdown) "SEAM" else "GRAVITY"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = labelText,
                style = TextStyle(
                    color = grayColor.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
            )
            Text(
                text = "${String.format(Locale.US, "%d", (animatedProgress * 100).toInt())}%",
                style = TextStyle(
                    color = accentColor,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Canvas(
            modifier = Modifier
                .width(110.dp)
                .height(8.dp)
        ) {
            val widthPx = size.width
            val heightPx = size.height
            val midY = heightPx / 2f

            // Base track line
            drawLine(
                color = grayColor.copy(alpha = 0.2f),
                start = Offset(0f, midY),
                end = Offset(widthPx, midY),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )

            // Progress line
            val activeWidth = widthPx * animatedProgress
            if (activeWidth > 0f) {
                drawLine(
                    color = accentColor,
                    start = Offset(0f, midY),
                    end = Offset(activeWidth, midY),
                    strokeWidth = 1.8f,
                    cap = StrokeCap.Round
                )
            }

            // Milestone ticks (25%, 50%, 75%, 100%)
            val milestones = floatArrayOf(0.25f, 0.50f, 0.75f, 1.0f)
            for (m in milestones) {
                val tickX = widthPx * m
                val isReached = animatedProgress >= m
                drawLine(
                    color = if (isReached) accentColor else grayColor.copy(alpha = 0.3f),
                    start = Offset(tickX, midY - 2.5f),
                    end = Offset(tickX, midY + 2.5f),
                    strokeWidth = 1.0f
                )
            }
        }
    }
}
