package com.floating.stopwatch.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedMeshGradient(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MeshAnimation")

    // Animate angles/offsets slowly to create a premium organic look
    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Angle1"
    )

    val angle2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Angle2"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Dark underlying color base
        drawRect(color = Color(0xFF0A0A0A))

        // First soft radial gradient anchor (following a slow ellipse)
        val x1 = width / 2f + (width / 3f) * cos(angle1)
        val y1 = height / 2f + (height / 3f) * sin(angle1)

        // Second soft radial gradient anchor
        val x2 = width / 2f + (width / 4f) * cos(angle2)
        val y2 = height / 2f + (height / 4f) * sin(angle2)

        // Custom drawn blended radial spots
        val radius1 = (width + height) * 0.45f
        val radius2 = (width + height) * 0.35f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.12f),
                    accentColor.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(x1, y1),
                radius = radius1
            ),
            center = Offset(x1, y1),
            radius = radius1
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(x2, y2),
                radius = radius2
            ),
            center = Offset(x2, y2),
            radius = radius2
        )
    }
}
