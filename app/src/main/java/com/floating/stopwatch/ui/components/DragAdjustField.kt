package com.floating.stopwatch.ui.components

import android.os.SystemClock
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.ui.theme.LuxuryColors
import kotlin.math.abs

@Composable
fun DragAdjustField(
    label: String,
    value: Float,
    minValue: Float,
    maxValue: Float,
    pixelsPerUnit: Float,
    accentColor: Color,
    valueFormatter: (Float) -> String,
    onValueChange: (Float) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var liveValue by remember(value) { mutableFloatStateOf(value.coerceIn(minValue, maxValue)) }
    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentFormatter by rememberUpdatedState(valueFormatter)
    val highlightColor by animateColorAsState(
        targetValue = if (isDragging) accentColor.copy(alpha = 0.1f) else Color.Transparent,
        label = "DragAdjustHighlight"
    )
    val valueScale by animateFloatAsState(
        targetValue = if (isDragging) 1.02f else 1f,
        label = "DragAdjustScale"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            accentColor.copy(alpha = if (isDragging) 0.55f else 0.18f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(minValue, maxValue, pixelsPerUnit) {
                var dragValue = currentValue
                var lastEventTime = 0L
                detectDragGestures(
                    onDragStart = {
                        dragValue = currentValue.coerceIn(minValue, maxValue)
                        liveValue = dragValue
                        lastEventTime = SystemClock.uptimeMillis()
                        isDragging = true
                    },
                    onDragEnd = {
                        isDragging = false
                    },
                    onDragCancel = {
                        liveValue = currentValue.coerceIn(minValue, maxValue)
                        isDragging = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val now = SystemClock.uptimeMillis()
                        val elapsedMs = (now - lastEventTime).coerceAtLeast(1L)
                        val verticalDistance = -dragAmount.y
                        val velocity = abs(verticalDistance) / elapsedMs * 1000f
                        val acceleration = (1f + (velocity / 900f)).coerceIn(1f, 2.5f)
                        val delta = verticalDistance / pixelsPerUnit * acceleration
                        dragValue = (dragValue + delta).coerceIn(minValue, maxValue)
                        liveValue = dragValue
                        currentOnValueChange(dragValue)
                        lastEventTime = now
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(highlightColor)
                .padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = LuxuryColors.WarmGray,
                fontSize = 10.sp,
                letterSpacing = 1.8.sp
            )
            Text(
                text = currentFormatter(liveValue),
                color = if (isDragging) accentColor else LuxuryColors.CreamyWhite,
                fontSize = 22.sp,
                modifier = Modifier.scale(valueScale)
            )
            Text(
                text = "DRAG UP/DOWN TO ADJUST",
                color = LuxuryColors.WarmGray.copy(alpha = 0.7f),
                fontSize = 9.sp,
                letterSpacing = 1.2.sp
            )
        }
    }
}
