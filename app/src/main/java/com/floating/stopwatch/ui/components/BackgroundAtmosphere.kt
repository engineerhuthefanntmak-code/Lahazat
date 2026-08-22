package com.floating.stopwatch.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private data class Star(
    val xRatio: Float,
    val yRatio: Float,
    val radiusDp: Float,
    val baseAlpha: Float,
    val layer: Int,
    val color: Color,
    val isTwinkling: Boolean,
    val twinklePhase: Float,
    val twinkleSpeed: Float
)

private class Meteor {
    var active: Boolean = false
    var startX: Float = 0f
    var startY: Float = 0f
    var endX: Float = 0f
    var endY: Float = 0f
    var startTimeSec: Float = 0f
    var durationSec: Float = 0.8f
    var coreRadiusPx: Float = 2f
    var haloRadiusPx: Float = 6f
    var tailLengthPx: Float = 120f
    var maxAlpha: Float = 0.4f
    var currentX: Float = 0f
    var currentY: Float = 0f
    var progress: Float = 0f

    fun reset() {
        active = false
        progress = 0f
    }
}

private data class Particle(
    val xRatio: Float,
    val yRatio: Float,
    val radiusDp: Float,
    val baseAlpha: Float,
    val speedY: Float,
    val speedX: Float,
    val phase: Float
)

@Composable
fun BackgroundAtmosphere(
    atmosphere: String,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val lifecycleOwner = LocalLifecycleOwner.current

    var isResumed by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            isResumed = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var timeNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isResumed, atmosphere) {
        if (isResumed && atmosphere != "Pure Black") {
            var lastFrame = 0L
            while (true) {
                withFrameNanos { frameTime ->
                    if (lastFrame != 0L) {
                        timeNanos += (frameTime - lastFrame)
                    }
                    lastFrame = frameTime
                }
            }
        }
    }

    // Deterministically generated sparse star field (65 stars)
    val stars = remember {
        val random = Random(20260822L)
        val count = 65
        val list = ArrayList<Star>(count)
        val softWhite = Color(0xFFF0F0F2)
        val warmWhite = Color(0xFFFFF8EB)

        for (i in 0 until count) {
            val layer = if (random.nextFloat() < 0.65f) 0 else 1
            val radiusDp = if (layer == 0) 0.4f + random.nextFloat() * 0.3f else 0.8f + random.nextFloat() * 0.4f
            val baseAlpha = if (layer == 0) 0.10f + random.nextFloat() * 0.25f else 0.30f + random.nextFloat() * 0.30f
            val isWarm = random.nextFloat() < 0.06f
            val starColor = if (isWarm) warmWhite else softWhite
            val isTwinkling = random.nextFloat() < 0.10f

            list.add(
                Star(
                    xRatio = random.nextFloat(),
                    yRatio = random.nextFloat(),
                    radiusDp = radiusDp,
                    baseAlpha = baseAlpha,
                    layer = layer,
                    color = starColor,
                    isTwinkling = isTwinkling,
                    twinklePhase = random.nextFloat() * 6.283185f,
                    twinkleSpeed = 0.6f + random.nextFloat() * 0.8f
                )
            )
        }
        list
    }

    // Pre-allocated object pool of meteors
    val meteorPool = remember { Array(10) { Meteor() } }
    var nextSpawnTimeSec by remember { mutableFloatStateOf(0f) }

    // Deterministically generated dust particles (25 microscopic particles)
    val dustParticles = remember {
        val random = Random(20260823L)
        val list = ArrayList<Particle>(25)
        for (i in 0 until 25) {
            list.add(
                Particle(
                    xRatio = random.nextFloat(),
                    yRatio = random.nextFloat(),
                    radiusDp = 0.3f + random.nextFloat() * 0.4f,
                    baseAlpha = 0.05f + random.nextFloat() * 0.15f,
                    speedY = -0.015f - random.nextFloat() * 0.02f,
                    speedX = -0.008f + random.nextFloat() * 0.016f,
                    phase = random.nextFloat() * 6.283185f
                )
            )
        }
        list
    }

    // Deterministically generated ember particles (18 soft warm white/gold particles)
    val emberParticles = remember {
        val random = Random(20260824L)
        val list = ArrayList<Particle>(18)
        for (i in 0 until 18) {
            list.add(
                Particle(
                    xRatio = random.nextFloat(),
                    yRatio = random.nextFloat(),
                    radiusDp = 0.6f + random.nextFloat() * 0.6f,
                    baseAlpha = 0.08f + random.nextFloat() * 0.18f,
                    speedY = -0.025f - random.nextFloat() * 0.02f,
                    speedX = -0.005f + random.nextFloat() * 0.01f,
                    phase = random.nextFloat() * 6.283185f
                )
            )
        }
        list
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f) return@Canvas

        // 1. BASE LAYER: PURE BLACK (#000000) IS ALWAYS THE FOUNDATION
        drawRect(color = Color(0xFF000000))

        if (atmosphere == "Pure Black") return@Canvas

        val timeSec = timeNanos / 1_000_000_000f

        when (atmosphere) {
            "Stellar" -> {
                // Meteor Spawning Engine (avg ~500ms spawn interval)
                if (timeSec >= nextSpawnTimeSec) {
                    val random = Random((timeSec * 1000).toLong())
                    val inactiveMeteor = meteorPool.firstOrNull { !it.active }

                    if (inactiveMeteor != null) {
                        inactiveMeteor.active = true
                        inactiveMeteor.startTimeSec = timeSec
                        inactiveMeteor.durationSec = 0.6f + random.nextFloat() * 0.5f
                        inactiveMeteor.coreRadiusPx = (1.0f + random.nextFloat() * 0.6f) * density
                        inactiveMeteor.haloRadiusPx = (4.0f + random.nextFloat() * 3.0f) * density
                        inactiveMeteor.tailLengthPx = (80f + random.nextFloat() * 100f) * density
                        inactiveMeteor.maxAlpha = 0.25f + random.nextFloat() * 0.30f

                        val diagonal = hypot(width, height)
                        val trajectoryLength = diagonal * (0.25f + random.nextFloat() * 0.25f)
                        val isLeftToRight = random.nextBoolean()
                        val angleDeg = if (isLeftToRight) 25f + random.nextFloat() * 35f else 120f + random.nextFloat() * 35f
                        val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()

                        inactiveMeteor.startX = random.nextFloat() * width
                        inactiveMeteor.startY = random.nextFloat() * 0.6f * height
                        inactiveMeteor.endX = inactiveMeteor.startX + cos(angleRad) * trajectoryLength
                        inactiveMeteor.endY = inactiveMeteor.startY + sin(angleRad) * trajectoryLength
                    }

                    nextSpawnTimeSec = timeSec + (0.3f + random.nextFloat() * 0.4f)
                }

                // Render Stars
                for (i in stars.indices) {
                    val star = stars[i]
                    val x = star.xRatio * width
                    val y = star.yRatio * height
                    val radiusPx = star.radiusDp * density

                    var currentAlpha = star.baseAlpha
                    if (star.isTwinkling) {
                        val twinkleMod = sin(timeSec * star.twinkleSpeed * 2.0f + star.twinklePhase) * 0.12f
                        currentAlpha = (currentAlpha + twinkleMod).coerceIn(0.05f, 0.85f)
                    }

                    drawCircle(
                        color = star.color.copy(alpha = currentAlpha),
                        radius = radiusPx,
                        center = Offset(x, y)
                    )
                }

                // Render Meteors
                for (i in meteorPool.indices) {
                    val meteor = meteorPool[i]
                    if (!meteor.active) continue

                    val progressRaw = (timeSec - meteor.startTimeSec) / meteor.durationSec
                    if (progressRaw >= 1.0f) {
                        meteor.reset()
                        continue
                    }

                    meteor.progress = FastOutSlowInEasing.transform(progressRaw.coerceIn(0f, 1f))
                    meteor.currentX = meteor.startX + (meteor.endX - meteor.startX) * meteor.progress
                    meteor.currentY = meteor.startY + (meteor.endY - meteor.startY) * meteor.progress

                    val progress = meteor.progress
                    val currentX = meteor.currentX
                    val currentY = meteor.currentY

                    val alphaFade = when {
                        progress < 0.20f -> progress / 0.20f
                        progress > 0.75f -> (1.0f - progress) / 0.25f
                        else -> 1.0f
                    }.coerceIn(0f, 1f) * meteor.maxAlpha

                    val dx = currentX - meteor.startX
                    val dy = currentY - meteor.startY
                    val distTravelled = hypot(dx, dy)

                    if (distTravelled > 2f) {
                        val dirX = dx / distTravelled
                        val dirY = dy / distTravelled

                        val actualTailLen = meteor.tailLengthPx.coerceAtMost(distTravelled)
                        val tailStartX = currentX - dirX * actualTailLen
                        val tailStartY = currentY - dirY * actualTailLen

                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.15f * alphaFade),
                                    Color.White.copy(alpha = 0.85f * alphaFade)
                                ),
                                start = Offset(tailStartX, tailStartY),
                                end = Offset(currentX, currentY)
                            ),
                            start = Offset(tailStartX, tailStartY),
                            end = Offset(currentX, currentY),
                            strokeWidth = 1.2f * density,
                            cap = StrokeCap.Round
                        )

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f * alphaFade),
                                    Color.Transparent
                                ),
                                center = Offset(currentX, currentY),
                                radius = meteor.haloRadiusPx
                            ),
                            center = Offset(currentX, currentY),
                            radius = meteor.haloRadiusPx
                        )

                        drawCircle(
                            color = Color.White.copy(alpha = 0.95f * alphaFade),
                            radius = meteor.coreRadiusPx,
                            center = Offset(currentX, currentY)
                        )
                    }
                }
            }
            "Dust" -> {
                val dustColor = Color(0xFFE8E8EC)
                for (i in dustParticles.indices) {
                    val p = dustParticles[i]
                    val currY = ((p.yRatio + p.speedY * timeSec) % 1.0f + 1.0f) % 1.0f
                    val currX = ((p.xRatio + p.speedX * timeSec + sin(timeSec * 0.2f + p.phase) * 0.02f) % 1.0f + 1.0f) % 1.0f

                    val px = currX * width
                    val py = currY * height
                    val alpha = (p.baseAlpha + sin(timeSec * 0.5f + p.phase) * 0.03f).coerceIn(0.02f, 0.25f)

                    drawCircle(
                        color = dustColor.copy(alpha = alpha),
                        radius = p.radiusDp * density,
                        center = Offset(px, py)
                    )
                }
            }
            "Ember" -> {
                val warmGold = Color(0xFFC9A66B)
                for (i in emberParticles.indices) {
                    val p = emberParticles[i]
                    val currY = ((p.yRatio + p.speedY * timeSec) % 1.0f + 1.0f) % 1.0f
                    val currX = ((p.xRatio + p.speedX * timeSec + sin(timeSec * 0.3f + p.phase) * 0.03f) % 1.0f + 1.0f) % 1.0f

                    val px = currX * width
                    val py = currY * height
                    val alpha = (p.baseAlpha + sin(timeSec * 0.8f + p.phase) * 0.05f).coerceIn(0.03f, 0.30f)

                    drawCircle(
                        color = warmGold.copy(alpha = alpha),
                        radius = p.radiusDp * density,
                        center = Offset(px, py)
                    )
                }
            }
            "Aurora" -> {
                val shift1 = sin(timeSec * 0.12f) * 0.15f
                val shift2 = cos(timeSec * 0.18f) * 0.10f

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x0EFFFFFF),
                            Color(0x04F5F3EF),
                            Color.Transparent
                        ),
                        center = Offset(width * (0.5f + shift1), height * (0.3f + shift2)),
                        radius = height * 0.65f
                    )
                )
            }
            "Void" -> {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xCC000000)
                        ),
                        center = Offset(width * 0.5f, height * 0.5f),
                        radius = hypot(width, height) * 0.6f
                    )
                )
            }
        }
    }
}
