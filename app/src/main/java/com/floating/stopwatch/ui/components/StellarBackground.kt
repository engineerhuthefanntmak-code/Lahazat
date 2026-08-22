package com.floating.stopwatch.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.runtime.withFrameNanos
import kotlin.math.*
import kotlin.random.Random

@Immutable
data class Star(
    val normalizedX: Float, // 0.0 .. 1.0
    val normalizedY: Float, // 0.0 .. 1.0
    val sizeDp: Float,
    val baseOpacity: Float,
    val layer: Int, // 0: far, 1: mid, 2: near
    val color: Color,
    val isTwinkling: Boolean,
    val twinklePhase: Float,
    val twinkleSpeed: Float,
    val hasHalo: Boolean,
    val haloRadiusDp: Float
)

@Immutable
class MeteorState(
    val startXRatio: Float,
    val startYRatio: Float,
    val endXRatio: Float,
    val endYRatio: Float,
    val startTimeMs: Long,
    val durationMs: Long,
    val tailLengthDp: Float,
    val angleRad: Float,
    val particles: List<ParticleSeed>
)

@Immutable
data class ParticleSeed(
    val offsetProgress: Float, // 0.0 .. 1.0 along tail
    val lateralOffsetDp: Float,
    val sizeDp: Float,
    val maxOpacity: Float
)

@Composable
fun StellarBackground(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Generate stable deterministic star field once
    val stars = remember {
        val random = Random(seed = 20260821)
        val list = mutableListOf<Star>()
        val totalStars = 180

        // Define a couple of negative space void zones (normalized x,y, radius) to keep untouched black space
        val voidZones = listOf(
            Triple(0.35f, 0.40f, 0.22f),
            Triple(0.70f, 0.65f, 0.18f),
            Triple(0.20f, 0.75f, 0.16f)
        )

        var attempts = 0
        while (list.size < totalStars && attempts < 1000) {
            attempts++
            val x = random.nextFloat()
            val y = random.nextFloat()

            // Check negative space void constraint
            val inVoid = voidZones.any { (vx, vy, vr) ->
                val dx = x - vx
                val dy = y - vy
                (dx * dx + dy * dy) < (vr * vr)
            }
            if (inVoid && random.nextFloat() > 0.15f) continue

            // Determine depth layer (Layer 0: 55%, Layer 1: 32%, Layer 2: 13%)
            val layerRoll = random.nextFloat()
            val layer = when {
                layerRoll < 0.55f -> 0
                layerRoll < 0.87f -> 1
                else -> 2
            }

            val sizeDp = when (layer) {
                0 -> 0.6f + random.nextFloat() * 0.5f // 0.6 - 1.1 dp
                1 -> 1.1f + random.nextFloat() * 0.7f // 1.1 - 1.8 dp
                else -> 1.8f + random.nextFloat() * 0.9f // 1.8 - 2.7 dp
            }

            val baseOpacity = when (layer) {
                0 -> 0.18f + random.nextFloat() * 0.25f
                1 -> 0.35f + random.nextFloat() * 0.35f
                else -> 0.60f + random.nextFloat() * 0.32f
            }

            // Colors: Soft white (#F5F3EF, #FFFFFF) with rare warm white (#FFF8E7, #FFFBF0)
            val isWarmWhite = layer == 2 && random.nextFloat() < 0.20f
            val color = when {
                isWarmWhite -> Color(0xFFFFF7E6)
                random.nextFloat() < 0.3f -> Color(0xFFF5F3EF)
                else -> Color(0xFFFFFFFF)
            }

            // Twinkling: ~12% of stars twinkle asynchronously
            val isTwinkling = (layer > 0) && (random.nextFloat() < 0.15f)
            val twinklePhase = random.nextFloat() * 2f * PI.toFloat()
            val twinkleSpeed = 0.8f + random.nextFloat() * 1.4f // slow

            // Halo: Signature stars (few near stars)
            val hasHalo = (layer == 2) && (random.nextFloat() < 0.18f)
            val haloRadiusDp = if (hasHalo) 5.0f + random.nextFloat() * 4.0f else 0f

            list.add(
                Star(
                    normalizedX = x,
                    normalizedY = y,
                    sizeDp = sizeDp,
                    baseOpacity = baseOpacity,
                    layer = layer,
                    color = color,
                    isTwinkling = isTwinkling,
                    twinklePhase = twinklePhase,
                    twinkleSpeed = twinkleSpeed,
                    hasHalo = hasHalo,
                    haloRadiusDp = haloRadiusDp
                )
            )
        }
        list.toList()
    }

    // Frame animation time in milliseconds
    var frameTimeMs by remember { mutableLongStateOf(0L) }

    // Meteor state management
    var currentMeteor by remember { mutableStateOf<MeteorState?>(null) }
    var nextMeteorTriggerMs by remember { mutableLongStateOf(3000L) } // First meteor after ~3s

    val lifecycleOwner = LocalLifecycleOwner.current

    // Lifecycle-aware animation loop using withFrameNanos
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val startNanos = System.nanoTime()
            while (true) {
                withFrameNanos { frameNanos ->
                    val elapsedMs = (frameNanos - startNanos) / 1_000_000L
                    frameTimeMs = elapsedMs

                    // Meteor schedule check
                    val activeMeteor = currentMeteor
                    if (activeMeteor == null) {
                        if (elapsedMs >= nextMeteorTriggerMs) {
                            val meteorRandom = Random(elapsedMs)
                            // Diagonal path: enter from top/right/left traveling diagonally across 60-90% screen
                            val angleDeg = 35f + meteorRandom.nextFloat() * 20f // 35 to 55 deg diagonal down-right
                            val angleRad = angleDeg * (PI.toFloat() / 180f)

                            val startXRatio = meteorRandom.nextFloat() * 0.5f // 0.0 to 0.5
                            val startYRatio = -0.05f // Start just above top boundary
                            val pathLengthRatio = 0.65f + meteorRandom.nextFloat() * 0.25f // 65% to 90%

                            val endXRatio = startXRatio + pathLengthRatio * cos(angleRad)
                            val endYRatio = startYRatio + pathLengthRatio * sin(angleRad)

                            val duration = 1300L + (meteorRandom.nextFloat() * 500L).toLong() // 1.3s to 1.8s
                            val tailLength = 160f + meteorRandom.nextFloat() * 80f // 160 to 240 dp

                            // Microscopic particles along tail
                            val particles = List(4) { i ->
                                ParticleSeed(
                                    offsetProgress = 0.2f + i * 0.2f + meteorRandom.nextFloat() * 0.1f,
                                    lateralOffsetDp = (meteorRandom.nextFloat() - 0.5f) * 6f,
                                    sizeDp = 0.8f + meteorRandom.nextFloat() * 0.6f,
                                    maxOpacity = 0.35f + meteorRandom.nextFloat() * 0.35f
                                )
                            }

                            currentMeteor = MeteorState(
                                startXRatio = startXRatio,
                                startYRatio = startYRatio,
                                endXRatio = endXRatio,
                                endYRatio = endYRatio,
                                startTimeMs = elapsedMs,
                                durationMs = duration,
                                tailLengthDp = tailLength,
                                angleRad = angleRad,
                                particles = particles
                            )
                        }
                    } else {
                        // Check if active meteor has finished
                        if (elapsedMs > activeMeteor.startTimeMs + activeMeteor.durationMs) {
                            currentMeteor = null
                            val meteorRandom = Random(elapsedMs)
                            val nextDelay = 8000L + (meteorRandom.nextFloat() * 4000L).toLong() // ~8-12s interval (~10s avg)
                            nextMeteorTriggerMs = elapsedMs + nextDelay
                        }
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        // 1. Draw PURE BLACK background
        drawRect(color = Color(0xFF000000))

        val nowSeconds = frameTimeMs / 1000f

        // Evaluate active meteor position if running
        val meteor = currentMeteor
        var meteorHeadX = -999f
        var meteorHeadY = -999f
        var meteorActive = false
        var meteorProgress = 0f

        if (meteor != null && frameTimeMs >= meteor.startTimeMs) {
            val progressRaw = (frameTimeMs - meteor.startTimeMs).toFloat() / meteor.durationMs.toFloat()
            if (progressRaw in 0f..1.0f) {
                // Smooth natural celestial easing (easeInOutCubic)
                meteorProgress = if (progressRaw < 0.5f) {
                    4f * progressRaw * progressRaw * progressRaw
                } else {
                    1f - (-2f * progressRaw + 2f).pow(3) / 2f
                }

                meteorHeadX = (meteor.startXRatio + (meteor.endXRatio - meteor.startXRatio) * meteorProgress) * width
                meteorHeadY = (meteor.startYRatio + (meteor.endYRatio - meteor.startYRatio) * meteorProgress) * height
                meteorActive = true
            }
        }

        // 2. Render Stars
        val pxPerDp = density.density
        val nearbyBrightenRadiusPx = 110f * pxPerDp

        for (star in stars) {
            val starX = star.normalizedX * width
            val starY = star.normalizedY * height

            // Compute twinkling modulation
            var currentOpacity = star.baseOpacity
            if (star.isTwinkling) {
                val wave = sin(nowSeconds * star.twinkleSpeed + star.twinklePhase)
                // Low amplitude modulation (+/- 0.18)
                currentOpacity = (currentOpacity + wave * 0.18f).coerceIn(0.05f, 1.0f)
            }

            // Nearby star brightening when meteor passes
            if (meteorActive) {
                val dx = starX - meteorHeadX
                val dy = starY - meteorHeadY
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < nearbyBrightenRadiusPx) {
                    val factor = 1.0f - (dist / nearbyBrightenRadiusPx)
                    // Smooth subtle boost (+0.2 max)
                    currentOpacity = (currentOpacity + factor * 0.22f).coerceAtMost(1.0f)
                }
            }

            val radiusPx = (star.sizeDp * pxPerDp) / 2f

            // Soft halo for signature stars
            if (star.hasHalo && currentOpacity > 0.3f) {
                val haloRadiusPx = star.haloRadiusDp * pxPerDp
                val haloAlpha = (currentOpacity * 0.12f).coerceIn(0f, 0.25f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            star.color.copy(alpha = haloAlpha),
                            star.color.copy(alpha = haloAlpha * 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(starX, starY),
                        radius = haloRadiusPx
                    ),
                    center = Offset(starX, starY),
                    radius = haloRadiusPx
                )
            }

            // Draw star core
            drawCircle(
                color = star.color.copy(alpha = currentOpacity),
                radius = radiusPx,
                center = Offset(starX, starY)
            )
        }

        // 3. Render Meteor (Shooting Star)
        if (meteorActive && meteor != null) {
            val activeMeteorState = meteor
            val tailLengthPx = activeMeteorState.tailLengthDp * pxPerDp
            val dxUnit = cos(activeMeteorState.angleRad)
            val dyUnit = sin(activeMeteorState.angleRad)

            // Tail end position
            val tailEndX = meteorHeadX - dxUnit * tailLengthPx
            val tailEndY = meteorHeadY - dyUnit * tailLengthPx

            // Fade meteor as it enters/exits edges
            val entryFade = (meteorProgress / 0.08f).coerceIn(0f, 1f)
            val exitFade = ((1.0f - meteorProgress) / 0.10f).coerceIn(0f, 1f)
            val globalMeteorAlpha = entryFade * exitFade

            if (globalMeteorAlpha > 0.01f) {
                // Outer tapered tail (multi-layer soft gradient brush)
                drawLinearGradientTail(
                    headX = meteorHeadX,
                    headY = meteorHeadY,
                    tailX = tailEndX,
                    tailY = tailEndY,
                    strokeWidth = 2.2f * pxPerDp,
                    alpha = 0.45f * globalMeteorAlpha,
                    color = Color(0xFFFFFBF0)
                )

                // Inner luminous tail core
                drawLinearGradientTail(
                    headX = meteorHeadX,
                    headY = meteorHeadY,
                    tailX = tailEndX,
                    tailY = tailEndY,
                    strokeWidth = 1.0f * pxPerDp,
                    alpha = 0.85f * globalMeteorAlpha,
                    color = Color.White
                )

                // Soft halo around meteor core
                val coreHaloRadius = 10f * pxPerDp
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.50f * globalMeteorAlpha),
                            Color(0xFFFFF8E7).copy(alpha = 0.20f * globalMeteorAlpha),
                            Color.Transparent
                        ),
                        center = Offset(meteorHeadX, meteorHeadY),
                        radius = coreHaloRadius
                    ),
                    center = Offset(meteorHeadX, meteorHeadY),
                    radius = coreHaloRadius
                )

                // Small luminous core dot
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f * globalMeteorAlpha),
                    radius = 1.8f * pxPerDp,
                    center = Offset(meteorHeadX, meteorHeadY)
                )

                // Microscopic tail particles
                val perpX = -dyUnit
                val perpY = dxUnit

                for (p in activeMeteorState.particles) {
                    val pDistPx = tailLengthPx * p.offsetProgress
                    val pBaseX = meteorHeadX - dxUnit * pDistPx
                    val pBaseY = meteorHeadY - dyUnit * pDistPx

                    val px = pBaseX + perpX * (p.lateralOffsetDp * pxPerDp)
                    val py = pBaseY + perpY * (p.lateralOffsetDp * pxPerDp)

                    val pAlpha = ((1f - p.offsetProgress) * p.maxOpacity * globalMeteorAlpha).coerceIn(0f, 1f)
                    if (pAlpha > 0.02f) {
                        drawCircle(
                            color = Color(0xFFFFFBF5).copy(alpha = pAlpha),
                            radius = (p.sizeDp * pxPerDp) / 2f,
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawLinearGradientTail(
    headX: Float,
    headY: Float,
    tailX: Float,
    tailY: Float,
    strokeWidth: Float,
    alpha: Float,
    color: Color
) {
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(
                color.copy(alpha = alpha),
                color.copy(alpha = alpha * 0.6f),
                color.copy(alpha = alpha * 0.15f),
                Color.Transparent
            ),
            start = Offset(headX, headY),
            end = Offset(tailX, tailY)
        ),
        start = Offset(headX, headY),
        end = Offset(tailX, tailY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}
