package com.floating.stopwatch.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun EnergyAuraEffect(
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EnergyAura")

    // Ambient rotation angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraRotation"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraPulse"
    )

    // Spark particles
    val particles = remember {
        List(12) {
            Particle(
                angle = Random.nextFloat() * 360f,
                speed = 0.5f + Random.nextFloat() * 1.5f,
                radiusOffset = Random.nextFloat() * 20f,
                size = 1.5f + Random.nextFloat() * 2.5f
            )
        }
    }

    val PearlWhite = Color(0xFFF5F3EF)
    val Platinum = Color(0xFFE5E5EA)
    val BrushedSilver = Color(0xFFD1D1D6)

    val targetIntensity = if (isRunning) 1.0f else 0.35f
    val animatedIntensity by animateFloatAsState(
        targetValue = targetIntensity,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "AuraIntensity"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val baseRadius = (minOf(size.width, size.height) / 2f) * 0.95f

        if (baseRadius <= 0f) return@Canvas

        // 1. Outer Soft Pearl Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    PearlWhite.copy(alpha = 0.18f * animatedIntensity),
                    Platinum.copy(alpha = 0.08f * animatedIntensity),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = baseRadius * 1.25f * breathingPulse
            ),
            center = Offset(centerX, centerY),
            radius = baseRadius * 1.25f * breathingPulse
        )

        // 2. Flowing Concentric Energy Ribbons
        val ribbonPath = Path().apply {
            val segments = 8
            for (i in 0..segments) {
                val a = Math.toRadians((rotationAngle + (i * 360f / segments)).toDouble())
                val r = baseRadius * (1.0f + 0.08f * sin(a * 3 + rotationAngle * 0.05) * animatedIntensity)
                val x = (centerX + r * cos(a)).toFloat()
                val y = (centerY + r * sin(a)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

        drawPath(
            path = ribbonPath,
            color = Platinum.copy(alpha = 0.35f * animatedIntensity),
            style = Stroke(width = 2.dp.toPx())
        )

        // 3. Electrical Arcs around circumference
        val arcPath = Path().apply {
            val arcSegments = 6
            for (i in 0..arcSegments) {
                val a = Math.toRadians((-rotationAngle * 1.5f + (i * 360f / arcSegments)).toDouble())
                val arcR = baseRadius * (1.05f + 0.05f * cos(a * 4) * animatedIntensity)
                val x = (centerX + arcR * cos(a)).toFloat()
                val y = (centerY + arcR * sin(a)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

        drawPath(
            path = arcPath,
            color = PearlWhite.copy(alpha = 0.5f * animatedIntensity),
            style = Stroke(
                width = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 25f), 0f)
            )
        )

        // 4. Micro Spark Particles
        particles.forEach { p ->
            val currentAngle = Math.toRadians((p.angle + rotationAngle * p.speed).toDouble())
            val currentR = baseRadius + p.radiusOffset * animatedIntensity * breathingPulse
            val px = (centerX + currentR * cos(currentAngle)).toFloat()
            val py = (centerY + currentR * sin(currentAngle)).toFloat()

            drawCircle(
                color = PearlWhite.copy(alpha = 0.7f * animatedIntensity),
                radius = p.size.dp.toPx(),
                center = Offset(px, py)
            )
        }
    }
}

private class Particle(
    val angle: Float,
    val speed: Float,
    val radiusOffset: Float,
    val size: Float
)
