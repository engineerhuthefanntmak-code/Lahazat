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
    if (effectType == "None/Off") return

    val infiniteTransition = rememberInfiniteTransition(label = "AuraEffectTransition")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraProgress"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraPulse"
    )

    val PearlWhite = Color(0xFFF5F3EF)
    val Platinum = Color(0xFFE5E5EA)
    val BrushedSilver = Color(0xFFC7C7CC)
    val VintageGold = Color(0xFFC9A66B)

    val targetIntensity = if (isRunning) 1.0f else 0.30f
    val animatedIntensity by animateFloatAsState(
        targetValue = targetIntensity,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "AuraIntensity"
    )

    val particles = remember {
        List(14) {
            ParticleData(
                xPct = Random.nextFloat(),
                yPct = Random.nextFloat(),
                speed = 0.2f + Random.nextFloat() * 0.8f,
                size = 1.2f + Random.nextFloat() * 2.0f,
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
            "Silver Whisper" -> {
                for (i in 0..2) {
                    val angleOffset = i * 120f + progress * 360f * 0.2f
                    val rad = Math.toRadians(angleOffset.toDouble())
                    val r = baseRadius * (0.98f + 0.04f * sin(rad * 2))
                    val sx = centerX + (r * cos(rad)).toFloat()
                    val sy = centerY + (r * sin(rad)).toFloat()
                    drawCircle(
                        color = BrushedSilver.copy(alpha = 0.25f * animatedIntensity),
                        radius = (3.dp.toPx() * pulse),
                        center = Offset(sx, sy)
                    )
                }
            }
            "Heartbeat Pulse" -> {
                val heartbeatR = baseRadius * (0.9f + 0.1f * pulse)
                drawCircle(
                    color = PearlWhite.copy(alpha = 0.3f * animatedIntensity * (2f - pulse)),
                    radius = heartbeatR,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            "Orbital Ring" -> {
                val orbitRad = Math.toRadians((progress * 360f).toDouble())
                val ox = centerX + (baseRadius * cos(orbitRad)).toFloat()
                val oy = centerY + (baseRadius * sin(orbitRad)).toFloat()
                drawCircle(
                    color = PearlWhite.copy(alpha = 0.7f * animatedIntensity),
                    radius = 2.5.dp.toPx(),
                    center = Offset(ox, oy)
                )
            }
            "Frost Crystals" -> {
                for (i in 0..5) {
                    val angle = Math.toRadians((i * 60 + progress * 360f * 0.1f).toDouble())
                    val fx = centerX + (baseRadius * cos(angle)).toFloat()
                    val fy = centerY + (baseRadius * sin(angle)).toFloat()
                    drawCircle(
                        color = Platinum.copy(alpha = 0.4f * animatedIntensity * pulse),
                        radius = (2.dp.toPx() * pulse),
                        center = Offset(fx, fy)
                    )
                }
            }
            "Golden Sands" -> {
                particles.forEach { p ->
                    val angleRad = Math.toRadians((p.angle + progress * 180f * p.speed).toDouble())
                    val pr = baseRadius * (0.85f + 0.15f * sin(angleRad))
                    val sx = centerX + (pr * cos(angleRad)).toFloat()
                    val sy = centerY + (pr * sin(angleRad)).toFloat()
                    drawCircle(
                        color = VintageGold.copy(alpha = 0.6f * animatedIntensity),
                        radius = p.size.dp.toPx(),
                        center = Offset(sx, sy)
                    )
                }
            }
            "Sonic Echo" -> {
                for (r in 1..2) {
                    val echoR = baseRadius * ((progress + r * 0.5f) % 1.0f)
                    val echoAlpha = (1.0f - (echoR / baseRadius)).coerceIn(0f, 1f) * animatedIntensity * 0.35f
                    drawCircle(
                        color = PearlWhite.copy(alpha = echoAlpha),
                        radius = echoR,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            "Living Shadow" -> {
                val shadowAngle = Math.toRadians((progress * 360f * 0.3f).toDouble())
                val sx = centerX + (baseRadius * 0.3f * cos(shadowAngle)).toFloat()
                val sy = centerY + (baseRadius * 0.3f * sin(shadowAngle)).toFloat()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.4f * animatedIntensity), Color.Transparent),
                        center = Offset(sx, sy),
                        radius = baseRadius * 1.1f
                    ),
                    center = Offset(sx, sy),
                    radius = baseRadius * 1.1f
                )
            }
            "Silk Threads" -> {
                val threadPath = Path().apply {
                    val segments = 6
                    for (i in 0..segments) {
                        val a = Math.toRadians(((progress * 360f * 0.2f) + (i * 360f / segments)).toDouble())
                        val r = baseRadius * (0.95f + 0.05f * sin(a * 2))
                        val x = (centerX + r * cos(a)).toFloat()
                        val y = (centerY + r * sin(a)).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(
                    path = threadPath,
                    color = Platinum.copy(alpha = 0.3f * animatedIntensity),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            "Moonlight Glow" -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PearlWhite.copy(alpha = 0.25f * animatedIntensity * pulse),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = baseRadius * 1.2f
                    ),
                    center = Offset(centerX, centerY),
                    radius = baseRadius * 1.2f
                )
            }
            "Lightning" -> {
                val boltPath = Path().apply {
                    val segments = 8
                    var currX = centerX - baseRadius * 0.8f
                    var currY = centerY
                    moveTo(currX, currY)
                    for (i in 1..segments) {
                        val nextX = centerX - baseRadius * 0.8f + (i * (baseRadius * 1.6f / segments))
                        val offset = (sin((progress * 360f + i * 45).toDouble()) * 12f * animatedIntensity).toFloat()
                        currY = centerY + offset
                        lineTo(nextX, currY)
                    }
                }
                drawPath(
                    path = boltPath,
                    color = PearlWhite.copy(alpha = 0.5f * animatedIntensity),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            "Fire" -> {
                for (i in 0..8) {
                    val pAngle = Math.toRadians((i * 45 + progress * 180f).toDouble())
                    val flameR = baseRadius * (0.85f + 0.15f * sin(pAngle * 2) * animatedIntensity)
                    val fx = centerX + (flameR * cos(pAngle)).toFloat()
                    val fy = centerY + (flameR * sin(pAngle)).toFloat()
                    drawCircle(
                        color = Platinum.copy(alpha = (0.3f * (1f - progress)) * animatedIntensity),
                        radius = (4.dp.toPx() * pulse),
                        center = Offset(fx, fy)
                    )
                }
            }
            "Wave" -> {
                for (r in 1..3) {
                    val waveR = baseRadius * ((progress + r * 0.3f) % 1.0f) * 1.15f
                    val waveAlpha = (1.0f - (waveR / (baseRadius * 1.15f))).coerceIn(0f, 1f) * animatedIntensity
                    drawCircle(
                        color = PearlWhite.copy(alpha = waveAlpha * 0.35f),
                        radius = waveR,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            "Rain" -> {
                particles.forEach { p ->
                    val rx = p.xPct * w
                    val ry = ((p.yPct + progress * p.speed) % 1.0f) * h
                    drawLine(
                        color = PearlWhite.copy(alpha = 0.3f * animatedIntensity),
                        start = Offset(rx, ry),
                        end = Offset(rx - 1f, ry + 8f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            "Smoke" -> {
                for (i in 0..3) {
                    val offsetR = baseRadius * 0.25f * sin((progress * 360f + i * 90).toDouble()).toFloat()
                    val sx = centerX + offsetR
                    val sy = centerY - (i * 6f * animatedIntensity)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Platinum.copy(alpha = 0.18f * animatedIntensity), Color.Transparent),
                            center = Offset(sx, sy),
                            radius = baseRadius * 0.7f * pulse
                        ),
                        center = Offset(sx, sy),
                        radius = baseRadius * 0.7f * pulse
                    )
                }
            }
            "Stardust" -> {
                particles.forEach { p ->
                    val pAngleRad = Math.toRadians((p.angle + progress * 180f * p.speed).toDouble())
                    val pr = baseRadius * (0.6f + 0.4f * sin(pAngleRad))
                    val sx = centerX + (pr * cos(pAngleRad)).toFloat()
                    val sy = centerY + (pr * sin(pAngleRad)).toFloat()
                    drawCircle(
                        color = PearlWhite.copy(alpha = 0.6f * animatedIntensity),
                        radius = p.size.dp.toPx(),
                        center = Offset(sx, sy)
                    )
                }
            }
            "Pulse Ring" -> {
                val ringR = baseRadius * pulse
                drawCircle(
                    color = PearlWhite.copy(alpha = 0.3f * animatedIntensity),
                    radius = ringR,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            "Glow Mist" -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PearlWhite.copy(alpha = 0.25f * animatedIntensity),
                            Platinum.copy(alpha = 0.1f * animatedIntensity),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = baseRadius * 1.15f * pulse
                    ),
                    center = Offset(centerX, centerY),
                    radius = baseRadius * 1.15f * pulse
                )
            }
            "Energy Threads" -> {
                for (i in 0..4) {
                    val threadAngle = Math.toRadians((i * 72 + progress * 180f).toDouble())
                    val tx = centerX + (baseRadius * cos(threadAngle)).toFloat()
                    val ty = centerY + (baseRadius * sin(threadAngle)).toFloat()
                    drawLine(
                        color = Platinum.copy(alpha = 0.35f * animatedIntensity),
                        start = Offset(centerX, centerY),
                        end = Offset(tx, ty),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            else -> { // "Ribbons & Sparks" default
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PearlWhite.copy(alpha = 0.15f * animatedIntensity),
                            Platinum.copy(alpha = 0.05f * animatedIntensity),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = baseRadius * 1.15f * pulse
                    ),
                    center = Offset(centerX, centerY),
                    radius = baseRadius * 1.15f * pulse
                )

                val ribbonPath = Path().apply {
                    val segments = 8
                    for (i in 0..segments) {
                        val a = Math.toRadians(((progress * 360f) + (i * 360f / segments)).toDouble())
                        val r = baseRadius * (1.0f + 0.05f * sin(a * 2 + progress * 360f * 0.02) * animatedIntensity)
                        val x = (centerX + r * cos(a)).toFloat()
                        val y = (centerY + r * sin(a)).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }

                drawPath(
                    path = ribbonPath,
                    color = Platinum.copy(alpha = 0.25f * animatedIntensity),
                    style = Stroke(width = 1.5.dp.toPx())
                )
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
