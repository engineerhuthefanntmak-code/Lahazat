package com.floating.stopwatch.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.ui.theme.LuxuryColors
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun HeritageOpeningScreen(
    accentColor: Color = LuxuryColors.AccentGold,
    onOpeningComplete: () -> Unit
) {
    var animationPhase by remember { mutableIntStateOf(1) }

    val phase1Alpha by animateFloatAsState(
        targetValue = if (animationPhase >= 1) 1f else 0f,
        animationSpec = tween(400, easing = LinearOutSlowInEasing),
        label = "P1Alpha"
    )

    val phase2Scale by animateFloatAsState(
        targetValue = if (animationPhase >= 2) 1f else 0.6f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "P2Scale"
    )

    val phase3MotifProgress by animateFloatAsState(
        targetValue = if (animationPhase >= 3) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "P3MotifProgress"
    )

    val phase4DetailAlpha by animateFloatAsState(
        targetValue = if (animationPhase >= 4) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "P4DetailAlpha"
    )

    val phase5TextAlpha by animateFloatAsState(
        targetValue = if (animationPhase >= 5) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "P5TextAlpha"
    )

    val phase6TransitionAlpha by animateFloatAsState(
        targetValue = if (animationPhase >= 6) 0f else 1f,
        animationSpec = tween(500, easing = LinearEasing),
        finishedListener = {
            if (animationPhase >= 6) {
                onOpeningComplete()
            }
        },
        label = "P6TransitionAlpha"
    )

    LaunchedEffect(Unit) {
        // Phase 1: PRESENCE
        delay(200)
        animationPhase = 2 // Phase 2: GEOMETRY
        delay(350)
        animationPhase = 3 // Phase 3: HERO MOTIF
        delay(450)
        animationPhase = 4 // Phase 4: MICRO DETAIL
        delay(350)
        animationPhase = 5 // Phase 5: IDENTITY
        delay(500)
        animationPhase = 6 // Phase 6: TRANSITION
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryColors.WarmBlack.copy(alpha = phase1Alpha * phase6TransitionAlpha)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Central Hero Motif Canvas
            Canvas(
                modifier = Modifier
                    .size(220.dp)
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val maxRadius = min(size.width, size.height) * 0.45f

                // Phase 2: Outer Geometry Structure
                if (phase2Scale > 0f) {
                    val rOuter = maxRadius * phase2Scale
                    drawCircle(
                        color = accentColor.copy(alpha = 0.25f * phase1Alpha * phase6TransitionAlpha),
                        radius = rOuter,
                        center = Offset(cx, cy),
                        style = Stroke(1.5.dp.toPx())
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = 0.15f * phase1Alpha * phase6TransitionAlpha),
                        radius = rOuter * 0.85f,
                        center = Offset(cx, cy),
                        style = Stroke(1.dp.toPx())
                    )
                }

                // Phase 3: Central Hero Motif (8-pointed geometric timepiece star)
                if (phase3MotifProgress > 0f) {
                    val starR = maxRadius * 0.7f * phase3MotifProgress
                    rotate(degrees = (1f - phase3MotifProgress) * 90f, pivot = Offset(cx, cy)) {
                        val path = Path()
                        for (i in 0 until 8) {
                            val a1 = i * (PI / 4.0)
                            val a2 = a1 + (PI / 8.0)
                            val x1 = cx + starR * cos(a1).toFloat()
                            val y1 = cy + starR * sin(a1).toFloat()
                            val x2 = cx + (starR * 0.5f) * cos(a2).toFloat()
                            val y2 = cy + (starR * 0.5f) * sin(a2).toFloat()
                            if (i == 0) path.moveTo(x1, y1) else path.lineTo(x1, y1)
                            path.lineTo(x2, y2)
                        }
                        path.close()
                        drawPath(
                            path = path,
                            color = accentColor.copy(alpha = 0.9f * phase6TransitionAlpha),
                            style = Stroke(2.dp.toPx())
                        )
                    }
                }

                // Phase 4: Micro Detail precision tick marks
                if (phase4DetailAlpha > 0f) {
                    val tickR = maxRadius * 0.85f
                    for (i in 0 until 12) {
                        val angle = i * (PI / 6.0)
                        val x1 = cx + (tickR - 8.dp.toPx()) * cos(angle).toFloat()
                        val y1 = cy + (tickR - 8.dp.toPx()) * sin(angle).toFloat()
                        val x2 = cx + tickR * cos(angle).toFloat()
                        val y2 = cy + tickR * sin(angle).toFloat()
                        drawLine(
                            color = accentColor.copy(alpha = phase4DetailAlpha * phase6TransitionAlpha),
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Phase 5: Application Identity
            if (phase5TextAlpha > 0f) {
                Text(
                    text = "FLOATING SUITE",
                    style = TextStyle(
                        color = LuxuryColors.CreamyWhite.copy(alpha = phase5TextAlpha * phase6TransitionAlpha),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraLight,
                        letterSpacing = 6.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "HERITAGE CHRONOGRAPH",
                    style = TextStyle(
                        color = accentColor.copy(alpha = phase5TextAlpha * phase6TransitionAlpha),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 3.sp
                    )
                )
            }
        }
    }
}
