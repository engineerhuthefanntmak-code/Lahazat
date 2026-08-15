package com.floating.stopwatch.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun EnergyAuraEffect(
    isRunning: Boolean,
    effectType: String = "Ribbons & Sparks",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuraEffectTransition")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraProgress"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraPulse"
    )

    val PearlWhite = Color(0xFFF5F3EF)
    val Platinum = Color(0xFFE5E5EA)
    val Charcoal = Color(0xFF3A3A3C)

    val targetIntensity = if (isRunning) 1.0f else 0.35f
    val animatedIntensity by animateFloatAsState(
        targetValue = targetIntensity,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "AuraIntensity"
    )

    val particles = remember {
        List(20) {
            ParticleData(
                xPct = Random.nextFloat(),
                yPct = Random.nextFloat(),
                speed = 0.3f + Random.nextFloat() * 1.2f,
                size = 1.5f + Random.nextFloat() * 2.5f,
                angle = Random.nextFloat() * 360f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h / 2f
        val baseRadius = (minOf(w, h) / 2f) * 0.95f

        if (baseRadius <= 0f) return@Canvas

        when (effectType) {
            "Lightning" -> {
                val boltPath = Path().apply {
                    val segments = 8
                    var currX = centerX - baseRadius * 0.8f
                    var currY = centerY
                    moveTo(currX, currY)
                    for (i in 1..segments) {
                        val nextX = centerX - baseRadius * 0.8f + (i * (baseRadius * 1.6f / segments))
                        val offset = (sin((progress * 360f + i * 45).toDouble()) * 18f * animatedIntensity).toFloat()
                        currY = centerY + offset
                        lineTo(nextX, currY)
                    }
                }
                drawPath(
                    path = boltPath,
                    color = PearlWhite.copy(alpha = 0.7f * animatedIntensity),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            "Fire" -> {
                for (i in 0..12) {
                    val pAngle = Math.toRadians((i * 30 + progress * 360f).toDouble())
                    val flameR = baseRadius * (0.8f + 0.3f * sin(pAngle * 2) * animatedIntensity)
                    val fx = centerX + (flameR * cos(pAngle)).toFloat()
                    val fy = centerY + (flameR * sin(pAngle)).toFloat() - (15f * progress * animatedIntensity)
                    drawCircle(
                        color = Platinum.copy(alpha = (0.4f * (1f - progress)) * animatedIntensity),
                        radius = (6.dp.toPx() * pulse),
                        center = Offset(fx, fy)
                    )
                }
            }
            "Wave" -> {
                for (r in 1..3) {
                    val waveR = baseRadius * ((progress + r * 0.3f) % 1.0f) * 1.3f
                    val waveAlpha = (1.0f - (waveR / (baseRadius * 1.3f))).coerceIn(0f, 1f) * animatedIntensity
                    drawCircle(
                        color = PearlWhite.copy(alpha = waveAlpha * 0.5f),
                        radius = waveR,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
            "Rain" -> {
                particles.forEach { p ->
                    val rx = p.xPct * w
                    val ry = ((p.yPct + progress * p.speed) % 1.0f) * h
                    drawLine(
                        color = PearlWhite.copy(alpha = 0.4f * animatedIntensity),
                        start = Offset(rx, ry),
                        end = Offset(rx - 2f, ry + 12f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            "Smoke" -> {
                for (i in 0..4) {
                    val offsetR = baseRadius * 0.4f * sin((progress * 360f + i * 72).toDouble()).toFloat()
                    val sx = centerX + offsetR
                    val sy = centerY - (i * 10f * animatedIntensity)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Platinum.copy(alpha = 0.25f * animatedIntensity), Color.Transparent),
                            center = Offset(sx, sy),
                            radius = baseRadius * 0.8f * pulse
                        ),
                        center = Offset(sx, sy),
                        radius = baseRadius * 0.8f * pulse
                    )
                }
            }
            "Stardust" -> {
                particles.forEach { p ->
                    val pAngleRad = Math.toRadians((p.angle + progress * 360f * p.speed).toDouble())
                    val pr = baseRadius * (0.5f + 0.5f * sin(pAngleRad))
                    val sx = centerX + (pr * cos(pAngleRad)).toFloat()
                    val sy = centerY + (pr * sin(pAngleRad)).toFloat()
                    drawCircle(
                        color = PearlWhite.copy(alpha = 0.8f * animatedIntensity),
                        radius = p.size.dp.toPx(),
                        center = Offset(sx, sy)
                    )
                }
            }
            "Pulse Ring" -> {
                val ringR = baseRadius * pulse
                drawCircle(
                    color = PearlWhite.copy(alpha = 0.4f * animatedIntensity),
                    radius = ringR,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            "Glow Mist" -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PearlWhite.copy(alpha = 0.35f * animatedIntensity),
                            Platinum.copy(alpha = 0.15f * animatedIntensity),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = baseRadius * 1.3f * pulse
                    ),
                    center = Offset(centerX, centerY),
                    radius = baseRadius * 1.3f * pulse
                )
            }
            "Energy Threads" -> {
                for (i in 0..5) {
                    val threadAngle = Math.toRadians((i * 60 + progress * 360f).toDouble())
                    val tx = centerX + (baseRadius * cos(threadAngle)).toFloat()
                    val ty = centerY + (baseRadius * sin(threadAngle)).toFloat()
                    drawLine(
                        color = Platinum.copy(alpha = 0.5f * animatedIntensity),
                        start = Offset(centerX, centerY),
                        end = Offset(tx, ty),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            else -> { // "Ribbons & Sparks" default
                // Outer Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PearlWhite.copy(alpha = 0.18f * animatedIntensity),
                            Platinum.copy(alpha = 0.08f * animatedIntensity),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = baseRadius * 1.25f * pulse
                    ),
                    center = Offset(centerX, centerY),
                    radius = baseRadius * 1.25f * pulse
                )

                // Concentric Ribbons
                val ribbonPath = Path().apply {
                    val segments = 8
                    for (i in 0..segments) {
                        val a = Math.toRadians(((progress * 360f) + (i * 360f / segments)).toDouble())
                        val r = baseRadius * (1.0f + 0.08f * sin(a * 3 + progress * 360f * 0.05) * animatedIntensity)
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

                // Sparks
                particles.forEach { p ->
                    val currentAngle = Math.toRadians((p.angle + progress * 360f * p.speed).toDouble())
                    val currentR = baseRadius + (p.xPct * 20f) * animatedIntensity * pulse
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
    }
}

private class ParticleData(
    val xPct: Float,
    val yPct: Float,
    val speed: Float,
    val size: Float,
    val angle: Float
)
