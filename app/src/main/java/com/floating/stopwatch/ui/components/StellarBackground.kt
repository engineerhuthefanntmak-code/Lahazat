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

@Composable
fun StellarBackground(
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
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var timeNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isResumed) {
        if (isResumed) {
            var lastFrame = 0L
            while (true) {
                withFrameNanos { frameTime ->
                    if (lastFrame != 0L) {
                        val delta = frameTime - lastFrame
                        timeNanos += delta
                    }
                    lastFrame = frameTime
                }
            }
        }
    }

    // Deterministically generate sparse star field (65 stars total, 2 subtle depth layers)
    val stars = remember {
        val random = Random(20260822L)
        val count = 65
        val list = ArrayList<Star>(count)

        val softWhite = Color(0xFFF0F0F2)
        val warmWhite = Color(0xFFFFF8EB)

        for (i in 0 until count) {
            val xRatio = random.nextFloat()
            val yRatio = random.nextFloat()

            val layer = if (random.nextFloat() < 0.65f) 0 else 1

            val radiusDp = when (layer) {
                0 -> 0.4f + random.nextFloat() * 0.3f
                else -> 0.8f + random.nextFloat() * 0.4f
            }

            val baseAlpha = when (layer) {
                0 -> 0.10f + random.nextFloat() * 0.25f
                else -> 0.30f + random.nextFloat() * 0.30f
            }

            val isWarm = random.nextFloat() < 0.06f // 6% warm white
            val starColor = if (isWarm) warmWhite else softWhite

            val isTwinkling = random.nextFloat() < 0.10f // 10% twinkling
            val twinklePhase = random.nextFloat() * 6.283185f
            val twinkleSpeed = 0.6f + random.nextFloat() * 0.8f

            list.add(
                Star(
                    xRatio = xRatio,
                    yRatio = yRatio,
                    radiusDp = radiusDp,
                    baseAlpha = baseAlpha,
                    layer = layer,
                    color = starColor,
                    isTwinkling = isTwinkling,
                    twinklePhase = twinklePhase,
                    twinkleSpeed = twinkleSpeed
                )
            )
        }
        list
    }

    // Pre-allocated object pool of meteors (~8 max concurrent meteors)
    val meteorPool = remember { Array(10) { Meteor() } }
    var nextSpawnTimeSec by remember { mutableFloatStateOf(0f) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f) return@Canvas

        // 1. Pure #000000 Background
        drawRect(color = Color(0xFF000000))

        val timeSec = timeNanos / 1_000_000_000f

        // 2. Meteor Spawning Engine (Average 500ms spawn interval: 300ms to 700ms)
        if (timeSec >= nextSpawnTimeSec) {
            val random = Random((timeSec * 1000).toLong())
            val inactiveMeteor = meteorPool.firstOrNull { !it.active }

            if (inactiveMeteor != null) {
                inactiveMeteor.active = true
                inactiveMeteor.startTimeSec = timeSec
                inactiveMeteor.durationSec = 0.6f + random.nextFloat() * 0.5f // 0.6s to 1.1s
                inactiveMeteor.coreRadiusPx = (1.0f + random.nextFloat() * 0.6f) * density
                inactiveMeteor.haloRadiusPx = (4.0f + random.nextFloat() * 3.0f) * density
                inactiveMeteor.tailLengthPx = (80f + random.nextFloat() * 100f) * density
                inactiveMeteor.maxAlpha = 0.25f + random.nextFloat() * 0.30f // Restrained low opacity

                val diagonal = hypot(width, height)
                val trajectoryLength = diagonal * (0.25f + random.nextFloat() * 0.25f)
                val isLeftToRight = random.nextBoolean()
                val angleDeg = if (isLeftToRight) 25f + random.nextFloat() * 35f else 120f + random.nextFloat() * 35f
                val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()

                val startXRatio = random.nextFloat()
                val startYRatio = random.nextFloat() * 0.6f

                inactiveMeteor.startX = startXRatio * width
                inactiveMeteor.startY = startYRatio * height
                inactiveMeteor.endX = inactiveMeteor.startX + cos(angleRad) * trajectoryLength
                inactiveMeteor.endY = inactiveMeteor.startY + sin(angleRad) * trajectoryLength
            }

            nextSpawnTimeSec = timeSec + (0.3f + random.nextFloat() * 0.4f) // avg ~0.5s
        }

        // 3. Render Stars
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

        // 4. Render Meteors
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

                // Thin tapered fading tail
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

                // Soft subtle halo
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

                // Tiny luminous head
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f * alphaFade),
                    radius = meteor.coreRadiusPx,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}
