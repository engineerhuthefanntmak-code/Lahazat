package com.floating.stopwatch.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.floating.stopwatch.ui.theme.LuxuryColors
import kotlin.math.*

@Composable
fun HeritageVisualSystem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    patternName: String = "Andalusian Star",
    meshEnabled: Boolean = true,
    opacity: Float = 0.15f,
    meshIntensity: Float = 0.20f,
    speed: Float = 1.0f,
    accentColor: Color = LuxuryColors.AccentGold
) {
    if (!enabled) return

    val infiniteTransition = rememberInfiniteTransition(label = "HeritageTransition")

    val durationMs = (18000 / speed.coerceAtLeast(0.1f)).toInt()

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HeritagePhase"
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (meshEnabled) {
            HeritageMesh(
                phase = phase,
                intensity = meshIntensity,
                accentColor = accentColor,
                modifier = Modifier.fillMaxSize()
            )
        }

        HeritagePatternRenderer(
            patternName = patternName,
            phase = phase,
            opacity = opacity,
            accentColor = accentColor,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun HeritageMesh(
    phase: Float,
    intensity: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val cx1 = width * (0.3f + 0.15f * sin(phase))
        val cy1 = height * (0.3f + 0.15f * cos(phase * 0.7f))

        val cx2 = width * (0.7f + 0.15f * cos(phase * 0.8f))
        val cy2 = height * (0.7f + 0.15f * sin(phase * 0.6f))

        val radius1 = max(width, height) * (0.5f + 0.1f * sin(phase * 0.5f))
        val radius2 = max(width, height) * (0.5f + 0.1f * cos(phase * 0.4f))

        val brush1 = Brush.radialGradient(
            colors = listOf(
                accentColor.copy(alpha = intensity * 0.6f),
                accentColor.copy(alpha = intensity * 0.15f),
                Color.Transparent
            ),
            center = Offset(cx1, cy1),
            radius = radius1
        )

        val brush2 = Brush.radialGradient(
            colors = listOf(
                accentColor.copy(alpha = intensity * 0.4f),
                accentColor.copy(alpha = intensity * 0.1f),
                Color.Transparent
            ),
            center = Offset(cx2, cy2),
            radius = radius2
        )

        drawRect(brush = brush1)
        drawRect(brush = brush2)
    }
}

@Composable
fun HeritagePatternRenderer(
    patternName: String,
    phase: Float,
    opacity: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val color = accentColor.copy(alpha = opacity)
        val strokeWidth = 1.2.dp.toPx()

        when (patternName) {
            "Andalusian Star" -> drawAndalusianStar(w, h, phase, color, strokeWidth)
            "Andalusian Lattice" -> drawAndalusianLattice(w, h, phase, color, strokeWidth)
            "Arabic Eightfold" -> drawArabicEightfold(w, h, phase, color, strokeWidth)
            "Islamic Geometric Rosette" -> drawIslamicRosette(w, h, phase, color, strokeWidth)
            "Muqarnas Geometry" -> drawMuqarnasGeometry(w, h, phase, color, strokeWidth)
            "Kufic Grid" -> drawKuficGrid(w, h, phase, color, strokeWidth)
            "Arabesque Geometry" -> drawArabesqueGeometry(w, h, phase, color, strokeWidth)
            "Eight-Point Star Lattice" -> drawEightPointStarLattice(w, h, phase, color, strokeWidth)
            "Interlaced Heritage Knot" -> drawInterlacedHeritageKnot(w, h, phase, color, strokeWidth)
            "Continuous Geometric Mesh" -> drawContinuousGeometricMesh(w, h, phase, color, strokeWidth)
            else -> drawAndalusianStar(w, h, phase, color, strokeWidth)
        }
    }
}

// 1. Andalusian Star
private fun DrawScope.drawAndalusianStar(w: Float, h: Float, phase: Float, color: Color, strokeWidth: Float) {
    val tileSize = min(w, h) / 4f
    val cols = (w / tileSize).toInt() + 2
    val rows = (h / tileSize).toInt() + 2

    val breatheScale = 1f + 0.03f * sin(phase)

    for (col in -1..cols) {
        for (row in -1..rows) {
            val cx = col * tileSize
            val cy = row * tileSize
            val r = tileSize * 0.42f * breatheScale

            rotate(degrees = (sin(phase * 0.2f) * 5f), pivot = Offset(cx, cy)) {
                val path = Path()
                for (i in 0 until 8) {
                    val outerAngle = i * (PI / 4.0)
                    val innerAngle = outerAngle + (PI / 8.0)
                    val ox = cx + r * cos(outerAngle).toFloat()
                    val oy = cy + r * sin(outerAngle).toFloat()
                    val ix = cx + (r * 0.5f) * cos(innerAngle).toFloat()
                    val iy = cy + (r * 0.5f) * sin(innerAngle).toFloat()

                    if (i == 0) path.moveTo(ox, oy) else path.lineTo(ox, oy)
                    path.lineTo(ix, iy)
                }
                path.close()
                drawPath(path, color, style = Stroke(strokeWidth))
            }
        }
    }
}

// 2. Andalusian Lattice
private fun DrawScope.drawAndalusianLattice(w: Float, h: Float, phase: Float, color: Color, strokeWidth: Float) {
    val step = min(w, h) / 6f
    val shift = (sin(phase * 0.5f) * step * 0.05f)

    var x = -step
    while (x < w + step) {
        drawLine(color, Offset(x + shift, 0f), Offset(x + h + shift, h), strokeWidth = strokeWidth)
        drawLine(color, Offset(x + shift, h), Offset(x + h + shift, 0f), strokeWidth = strokeWidth)
        x += step
    }

    var y = -step
    while (y < h + step) {
        drawCircle(color, radius = 2.dp.toPx(), center = Offset(w / 2f, y), style = Stroke(strokeWidth))
        y += step * 1.5f
    }
}

// 3. Arabic Eightfold
private fun DrawScope.drawArabicEightfold(w: Float, h: Float, phase: Float, color: Color, strokeWidth: Float) {
    val tileSize = min(w, h) / 3.5f
    val cols = (w / tileSize).toInt() + 2
    val rows = (h / tileSize).toInt() + 2

    for (col in -1..cols) {
        for (row in -1..rows) {
            val cx = col * tileSize + (tileSize / 2f)
            val cy = row * tileSize + (tileSize / 2f)
            val r = tileSize * 0.45f

            drawCircle(color, radius = r, center = Offset(cx, cy), style = Stroke(strokeWidth))
            drawRect(color, topLeft = Offset(cx - r * 0.7f, cy - r * 0.7f), size = Size(r * 1.4f, r * 1.4f), style = Stroke(strokeWidth))

            rotate(degrees = 45f + (cos(phase * 0.3f) * 3f), pivot = Offset(cx, cy)) {
                drawRect(color, topLeft = Offset(cx - r * 0.7f, cy - r * 0.7f), size = Size(r * 1.4f, r * 1.4f), style = Stroke(strokeWidth))
            }
        }
    }
}

// 4. Islamic Geometric Rosette
private fun DrawScope.drawIslamicRosette(w: Float, h: Float, phase: Float, color: Color, strokeWidth: Float) {
    val cx = w / 2f
    val cy = h / 2f
    val maxR = min(w, h) * 0.45f
    val petals = 12

    for (layer in 1..4) {
        val r = maxR * (layer / 4f)
        val path = Path()
        for (i in 0 until petals) {
            val angle = i * (2f * PI / petals) + (phase * 0.05f * if (layer % 2 == 0) 1f else -1f)
            val px = cx + r * cos(angle).toFloat()
            val py = cy + r * sin(angle).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawPath(path, color, style = Stroke(strokeWidth))
    }
}

// 5. Muqarnas Geometry
private fun DrawScope.drawMuqarnasGeometry(w: Float, h: Float, phase: Float, color: Color, strokeWidth: Float) {
    val cellW = min(w, h) / 6f
    val rows = (h / cellW).toInt() + 2
    val cols = (w / cellW).toInt() + 2

    for (r in 0..rows) {
        val y = r * cellW
        val offset = if (r % 2 == 0) 0f else cellW * 0.5f
        for (c in -1..cols) {
            val x = c * cellW + offset
            val path = Path()
            path.moveTo(x, y)
            path.lineTo(x + cellW * 0.5f, y - cellW * 0.5f)
            path.lineTo(x + cellW, y)
            path.lineTo(x + cellW * 0.5f, y + cellW * 0.5f)
            path.close()
            drawPath(path, color, style = Stroke(strokeWidth))
        }
    }
}

// 6. Kufic Grid
private fun DrawScope.drawKuficGrid(w: Float, h: Float, phase: Float, color: Color, strokeWidth: Float) {
    val unit = min(w, h) / 12f
    val cols = (w / unit).toInt()
    val rows = (h / unit).toInt()

    for (c in 0..cols step 2) {
        for (r in 0..rows step 2) {
            val x = c * unit
            val y = r * unit
            drawRect(color, topLeft = Offset(x, y), size = Size(unit, unit), style = Stroke(strokeWidth))
            drawLine(color, Offset(x, y + unit / 2f), Offset(x + unit, y + unit / 2f), strokeWidth = strokeWidth)
        }
    }
}

// 7. Arabesque Geometry
private fun DrawScope.drawArabesqueGeometry(w: Float, h: Float, phase: Float, color: Color, strokeWidth: Float) {
    val tileSize = min(w, h) / 4f
    val cols = (w / tileSize).toInt() + 2
    val rows = (h / tileSize).toInt() + 2

    for (c in -1..cols) {
        for (r in -1..rows) {
            val cx = c * tileSize
            val cy = r * tileSize
            val rad = tileSize * 0.5f
            val path = Path()
            path.addArc(
                oval = androidx.compose.ui.geometry.Rect(cx - rad, cy - rad, cx + rad, cy + rad),
                startAngleDegrees = 0f + (sin(phase) * 5f),
                sweepAngleDegrees = 180f
            )
            drawPath(path, color, style = Stroke(strokeWidth))
        }
    }
}

// 8. Eight-Point Star Lattice
private fun DrawScope.drawEightPointStarLattice(w: Float, h: Float, phase: Float, color: Color, strokeWidth: Float) {
    val size = min(w, h) / 5f
    val cols = (w / size).toInt() + 2
    val rows = (h / size).toInt() + 2

    for (c in -1..cols) {
        for (r in -1..rows) {
            val cx = c * size
            val cy = r * size
            val starR = size * 0.4f
            val path = Path()
            for (i in 0 until 8) {
                val a1 = i * (PI / 4.0)
                val a2 = a1 + (PI / 8.0)
                val x1 = cx + starR * cos(a1).toFloat()
                val y1 = cy + starR * sin(a1).toFloat()
                val x2 = cx + (starR * 0.6f) * cos(a2).toFloat()
                val y2 = cy + (starR * 0.6f) * sin(a2).toFloat()
                if (i == 0) path.moveTo(x1, y1) else path.lineTo(x1, y1)
                path.lineTo(x2, y2)
            }
            path.close()
            drawPath(path, color, style = Stroke(strokeWidth))
        }
    }
}

// 9. Interlaced Heritage Knot
private fun DrawScope.drawInterlacedHeritageKnot(w: Float, h: Float, phase: Float, color: Color, strokeWidth: Float) {
    val knotSize = min(w, h) / 4f
    val cols = (w / knotSize).toInt() + 2
    val rows = (h / knotSize).toInt() + 2

    for (c in -1..cols) {
        for (r in -1..rows) {
            val cx = c * knotSize + (knotSize / 2f)
            val cy = r * knotSize + (knotSize / 2f)
            val r1 = knotSize * 0.3f
            val r2 = knotSize * 0.45f

            drawCircle(color, radius = r1, center = Offset(cx, cy), style = Stroke(strokeWidth))
            drawCircle(color, radius = r2, center = Offset(cx, cy), style = Stroke(strokeWidth))

            drawLine(color, Offset(cx - r2, cy), Offset(cx + r2, cy), strokeWidth = strokeWidth)
            drawLine(color, Offset(cx, cy - r2), Offset(cx, cy + r2), strokeWidth = strokeWidth)
        }
    }
}

// 10. Continuous Geometric Mesh
private fun DrawScope.drawContinuousGeometricMesh(w: Float, h: Float, phase: Float, color: Color, strokeWidth: Float) {
    val meshSize = min(w, h) / 8f
    val cols = (w / meshSize).toInt() + 2
    val rows = (h / meshSize).toInt() + 2

    for (c in -1..cols) {
        for (r in -1..rows) {
            val x = c * meshSize
            val y = r * meshSize
            drawLine(color, Offset(x, y), Offset(x + meshSize, y + meshSize), strokeWidth = strokeWidth)
            drawLine(color, Offset(x + meshSize, y), Offset(x, y + meshSize), strokeWidth = strokeWidth)
            drawRect(color, topLeft = Offset(x, y), size = Size(meshSize, meshSize), style = Stroke(strokeWidth * 0.8f))
        }
    }
}
