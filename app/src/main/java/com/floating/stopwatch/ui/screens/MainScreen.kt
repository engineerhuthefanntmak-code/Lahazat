package com.floating.stopwatch.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.domain.Lap
import com.floating.stopwatch.domain.StopwatchState
import com.floating.stopwatch.ui.MainViewModel
import com.floating.stopwatch.ui.components.TimeDisplay
import com.floating.stopwatch.ui.theme.LuxuryColors
import com.floating.stopwatch.domain.HapticController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    hapticController: HapticController,
    hapticIntensity: String,
    showCentiseconds: Boolean,
    mainSize: Float,
    accentColor: Color,
    themeMode: String,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val elapsedTimeMs by viewModel.elapsedTimeMs.collectAsState()
    val laps by viewModel.laps.collectAsState()

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    // Start/Stop pulse animations
    var triggerPulse by remember { mutableStateOf(false) }
    val scalePulse by animateFloatAsState(
        targetValue = if (triggerPulse) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        finishedListener = {
            if (triggerPulse) triggerPulse = false
        },
        label = "Pulse"
    )

    // Layout configuration based on the illumination Mode
    val currentBgColor = when (themeMode) {
        "Midnight" -> Color(0xFF000000)
        "Warm Paper" -> Color(0xFFF7F5F0)
        else -> LuxuryColors.WarmBlack
    }

    val currentTextColor = when (themeMode) {
        "Warm Paper" -> Color(0xFF1C1A17)
        else -> LuxuryColors.CreamyWhite
    }

    val currentGrayColor = when (themeMode) {
        "Warm Paper" -> Color(0xFF6B6661)
        else -> LuxuryColors.WarmGray
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentBgColor)
            .padding(24.dp)
    ) {
        // Settings click triggers navigation
        Text(
            text = "SETTINGS",
            style = TextStyle(
                color = currentGrayColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable { onNavigateToSettings() }
                .padding(8.dp)
        )

        // Top label "STOPWATCH"
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp)
        ) {
            Text(
                text = "STOPWATCH",
                style = TextStyle(
                    color = currentTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraLight,
                    letterSpacing = 4.sp
                )
            )
        }

        // Breathing pulse animation when stopwatch is at 0 for more than 5 seconds
        val isAtZeroForFiveSecs = elapsedTimeMs == 0L && state == StopwatchState.Ready
        val infiniteTransition = rememberInfiniteTransition(label = "PulseAtZero")
        val breathingScale by if (isAtZeroForFiveSecs) {
            infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.03f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "BreathingScale"
            )
        } else {
            remember { mutableStateOf(1.0f) }
        }

        // Center Stopwatch display
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimeDisplay(
                elapsedTimeMs = elapsedTimeMs,
                showCentiseconds = showCentiseconds,
                baseStyle = TextStyle(color = currentTextColor, fontSize = 54.sp),
                scaleFactor = mainSize,
                modifier = Modifier
                    .scale(scalePulse * breathingScale)
                    .semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.name.uppercase(),
                style = TextStyle(
                    color = currentGrayColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 3.sp
                )
            )
        }

        // Action Buttons Row
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 54.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lap / Reset button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clickable {
                        if (state == StopwatchState.Running) {
                            hapticController.trigger(hapticIntensity, "Lap")
                            viewModel.lap()
                        } else if (state == StopwatchState.Paused) {
                            hapticController.trigger(hapticIntensity, "Reset")
                            viewModel.reset()
                        }
                    }
                    .semantics {
                        liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite
                    },
                contentAlignment = Alignment.Center
            ) {
                // Circular stroke design
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, currentGrayColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (state == StopwatchState.Paused) "RESET" else "LAP",
                            style = TextStyle(
                                color = currentTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            // Big Start/Stop golden button
            val buttonColor = if (state == StopwatchState.Running) Color(0xFF9E2A2B) else accentColor
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(buttonColor)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                triggerPulse = true
                                tryAwaitRelease()
                                triggerPulse = false
                            },
                            onTap = {
                                if (state == StopwatchState.Running) {
                                    hapticController.trigger(hapticIntensity, "Stop")
                                    viewModel.pause()
                                } else {
                                    hapticController.trigger(hapticIntensity, "Start")
                                    viewModel.start()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state == StopwatchState.Running) "STOP" else "START",
                    style = TextStyle(
                        color = LuxuryColors.WarmBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        // Small indicator link to check laps bottom sheet
        if (laps.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clickable { showBottomSheet = true }
                    .padding(8.dp)
            ) {
                Text(
                    text = "VIEW LAPS (${laps.size})",
                    style = TextStyle(
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
                )
            }
        }
    }

    // Slide-up bottom sheet for luxury clean laps listing
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = currentBgColor,
            dragHandle = { BottomSheetDefaults.DragHandle(color = currentGrayColor) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "LAP TIMES",
                    style = TextStyle(
                        color = currentTextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraLight,
                        letterSpacing = 4.sp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(laps.reversed()) { lap ->
                        LapRowItem(lap = lap, textColor = currentTextColor, grayColor = currentGrayColor)
                        Divider(color = currentGrayColor.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun LapRowItem(lap: Lap, textColor: Color, grayColor: Color) {
    val totalSeconds = lap.cumulativeTimeMs / 1000
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val centiseconds = (lap.cumulativeTimeMs % 1000) / 10
    val formattedCum = String.format("%02d:%02d.%02d", minutes, seconds, centiseconds)

    val lapSecs = lap.lapTimeMs / 1000
    val lapMins = (lapSecs % 3600) / 60
    val lapS = lapSecs % 60
    val lapCent = (lap.lapTimeMs % 1000) / 10
    val formattedLap = String.format("%02d:%02d.%02d", lapMins, lapS, lapCent)

    val deltaSign = if (lap.diffFromPreviousMs >= 0) "+" else ""
    val deltaSecs = lap.diffFromPreviousMs / 1000
    val deltaCent = (Math.abs(lap.diffFromPreviousMs) % 1000) / 10
    val formattedDelta = "$deltaSign${deltaSecs}.${deltaCent}s"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "LAP ${lap.lapIndex}",
                style = TextStyle(
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = formattedCum,
                style = TextStyle(
                    color = grayColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formattedLap,
                style = TextStyle(
                    color = textColor,
                    fontSize = 14.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            )
            if (lap.lapIndex > 1) {
                Spacer(modifier = Modifier.width(12.dp))
                val colorDelta = if (lap.diffFromPreviousMs > 0) Color(0xFFC94A4A) else Color(0xFF4AC98F)
                Text(
                    text = formattedDelta,
                    style = TextStyle(
                        color = colorDelta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light
                    )
                )
            }
        }
    }
}
