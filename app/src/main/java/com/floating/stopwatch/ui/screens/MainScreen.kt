package com.floating.stopwatch.ui.screens

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.data.SettingsRepository
import com.floating.stopwatch.domain.*
import com.floating.stopwatch.ui.AppMode
import com.floating.stopwatch.ui.MainViewModel
import com.floating.stopwatch.ui.components.DragAdjustField
import com.floating.stopwatch.ui.components.AmbientDimOverlay
import com.floating.stopwatch.ui.components.BackgroundAtmosphere
import com.floating.stopwatch.ui.components.CompanionIndicator
import com.floating.stopwatch.ui.components.TimeDisplay
import com.floating.stopwatch.ui.theme.LuxuryColors
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    settingsRepository: SettingsRepository,
    hapticController: HapticController,
    hapticIntensity: String,
    showCentiseconds: Boolean,
    mainSize: Float,
    accentColor: Color,
    themeMode: String,
    onNavigateToSettings: () -> Unit
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val state by viewModel.state.collectAsState()
    val elapsedTimeMs by viewModel.elapsedTimeMs.collectAsState()
    val countdownRemainingMs by viewModel.countdownRemainingMs.collectAsState()
    val isCountdownRunning by viewModel.isCountdownRunning.collectAsState()
    val counterValue by viewModel.counterValue.collectAsState()
    val laps by viewModel.laps.collectAsState()

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    val intervalEngine = viewModel.intervalEngine
    val intervalState by intervalEngine.state.collectAsState()

    val legacyEngine = viewModel.legacyEngine
    val legacyState by legacyEngine.state.collectAsState()

    val scaleStopwatch by settingsRepository.scaleStopwatch.collectAsState(initial = 1.0f)
    val scaleCountdown by settingsRepository.scaleCountdown.collectAsState(initial = 1.0f)
    val scaleCounter by settingsRepository.scaleCounter.collectAsState(initial = 1.0f)
    val scaleInterval by settingsRepository.scaleInterval.collectAsState(initial = 1.0f)
    val scaleLegacy by settingsRepository.scaleLegacy.collectAsState(initial = 1.0f)

    val seamEnabled by settingsRepository.seamEnabled.collectAsState(initial = true)
    val gravityEnabled by settingsRepository.gravityEnabled.collectAsState(initial = true)

    val backgroundAtmosphere by settingsRepository.backgroundAtmosphere.collectAsState(initial = "Pure Black")
    var settingsPresentationState by remember { mutableStateOf<SettingsPresentationState>(SettingsPresentationState.Closed) }
    var showFloatingConfigDialog by remember { mutableStateOf(false) }

    val isCurrentlyRunning = when (currentMode) {
        AppMode.Stopwatch -> state == StopwatchState.Running
        AppMode.Countdown -> isCountdownRunning
        AppMode.Counter -> false
        AppMode.Intervals -> intervalState == IntervalState.RUNNING
        AppMode.Legacy -> legacyState == LegacyState.RUNNING
    }

    var areControlsVisible by remember { mutableStateOf(true) }
    var isSecondaryVisible by remember { mutableStateOf(true) }
    var autoHideJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    fun resetAutoHideTimer() {
        areControlsVisible = true
        isSecondaryVisible = true
        autoHideJob?.cancel()
        if (isCurrentlyRunning) {
            autoHideJob = scope.launch {
                kotlinx.coroutines.delay(3000L)
                areControlsVisible = false
                isSecondaryVisible = false
            }
        }
    }

    LaunchedEffect(isCurrentlyRunning, currentMode) {
        resetAutoHideTimer()
    }

    DisposableEffect(Unit) {
        onDispose { autoHideJob?.cancel() }
    }

    // Milestone Haptics for Counter
    val milestoneSet = remember { setOf(11L, 33L, 66L, 99L, 100L) }
    var lastSignalledMilestone by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(counterValue) {
        if (counterValue in milestoneSet && counterValue != lastSignalledMilestone) {
            hapticController.trigger(hapticIntensity, "CounterMilestone")
            lastSignalledMilestone = counterValue
        } else if (counterValue !in milestoneSet) {
            lastSignalledMilestone = null
        }
    }

    val masterSoundEnabled by settingsRepository.masterSoundEnabled.collectAsState(initial = true)
    val selectedSoundType by settingsRepository.selectedSoundType.collectAsState(initial = "Soft Click")

    // Completion Sound Triggers
    var lastCompletedCountdownTime by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(countdownRemainingMs, isCountdownRunning) {
        if (countdownRemainingMs == 0L && !isCountdownRunning && lastCompletedCountdownTime != 0L && lastCompletedCountdownTime != null) {
            CompletionSoundPlayer.playSound(selectedSoundType, masterSoundEnabled)
            lastCompletedCountdownTime = 0L
        } else if (countdownRemainingMs > 0L) {
            lastCompletedCountdownTime = countdownRemainingMs
        }
    }

    var lastSignalledIntervalState by remember { mutableStateOf<IntervalState?>(null) }
    LaunchedEffect(intervalState) {
        if (intervalState == IntervalState.COMPLETED && lastSignalledIntervalState != IntervalState.COMPLETED) {
            CompletionSoundPlayer.playSound(selectedSoundType, masterSoundEnabled)
            lastSignalledIntervalState = IntervalState.COMPLETED
        } else if (intervalState != IntervalState.COMPLETED) {
            lastSignalledIntervalState = intervalState
        }
    }

    val controlsAlpha by animateFloatAsState(
        targetValue = if (areControlsVisible) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 250),
        label = "ControlsAlpha"
    )

    val secondaryAlpha by animateFloatAsState(
        targetValue = if (isSecondaryVisible) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 250),
        label = "SecondaryAlpha"
    )

    var triggerPulse by remember { mutableStateOf(false) }
    val scalePulse by animateFloatAsState(
        targetValue = if (triggerPulse) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        finishedListener = { if (triggerPulse) triggerPulse = false },
        label = "Pulse"
    )

    var isAmbientDimActive by remember { mutableStateOf(false) }

    if (isAmbientDimActive) {
        AmbientDimOverlay(
            onExitDoubleTap = { isAmbientDimActive = false }
        )
        return
    }

    val currentBgColor = when (themeMode) {
        "Warm Paper", "Warm Paper Light" -> Color(0xFFF7F5F0)
        "Pure White Light" -> Color(0xFFFFFFFF)
        else -> LuxuryColors.WarmBlack
    }

    val currentTextColor = when (themeMode) {
        "Warm Paper", "Warm Paper Light", "Pure White Light" -> Color(0xFF1C1A17)
        else -> LuxuryColors.CreamyWhite
    }

    val currentGrayColor = when (themeMode) {
        "Warm Paper", "Warm Paper Light", "Pure White Light" -> Color(0xFF6B6661)
        else -> LuxuryColors.WarmGray
    }

    var totalDragY by remember { mutableFloatStateOf(0f) }
    var totalDragX by remember { mutableFloatStateOf(0f) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundAtmosphere(
                atmosphere = if (isAmbientDimActive) "Pure Black" else backgroundAtmosphere,
                modifier = Modifier.fillMaxSize()
            )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .pointerInput(Unit) { detectTapGestures(onTap = { resetAutoHideTimer() }) }
                .pointerInput(currentMode) {
                    detectDragGestures(
                        onDragStart = {
                            totalDragY = 0f
                            totalDragX = 0f
                            resetAutoHideTimer()
                        },
                        onDrag = { _, dragAmount ->
                            totalDragY += dragAmount.y
                            totalDragX += dragAmount.x
                        },
                        onDragEnd = {
                            if (kotlin.math.abs(totalDragY) > 80f && kotlin.math.abs(totalDragY) > 1.5f * kotlin.math.abs(totalDragX)) {
                                if (totalDragY < 0) {
                                    hapticController.trigger(hapticIntensity, "Lap")
                                    viewModel.cycleMode()
                                } else {
                                    hapticController.trigger(hapticIntensity, "Lap")
                                    viewModel.previousMode()
                                }
                            }
                            totalDragY = 0f
                            totalDragX = 0f
                        },
                        onDragCancel = {
                            totalDragY = 0f
                            totalDragX = 0f
                        }
                    )
                }
        ) {
            val context = LocalContext.current

            // Top Left Mode Header & Floating Companion (SEAM / GRAVITY)
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp)
                    .graphicsLayer { alpha = controlsAlpha }
            ) {
                Column(
                    modifier = Modifier
                        .clickable {
                            resetAutoHideTimer()
                            hapticController.trigger(hapticIntensity, "Lap")
                            viewModel.cycleMode()
                        }
                        .padding(4.dp)
                ) {
                    Text(
                        text = when (currentMode) {
                            AppMode.Stopwatch -> "مؤقت الساعات ▾"
                            AppMode.Countdown -> "العد التنازلي ▾"
                            AppMode.Counter -> "العداد التراكمي ▾"
                            AppMode.Intervals -> "المراحل الزمنية ▾"
                            AppMode.Legacy -> "الأهداف والإنجازات ▾"
                        },
                        style = TextStyle(
                            color = currentTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraLight,
                            letterSpacing = 2.sp
                        )
                    )
                }

                // Companion Indicator directly below mode header
                if (currentMode == AppMode.Countdown) {
                    val countdownInitialMs by viewModel.countdownInitialMs.collectAsState()
                    val ratio = if (countdownInitialMs > 0L) (1f - (countdownRemainingMs.toFloat() / countdownInitialMs.toFloat())) else 0f
                    CompanionIndicator(
                        mode = currentMode,
                        progressRatio = ratio,
                        isEnabled = seamEnabled,
                        accentColor = accentColor,
                        grayColor = currentGrayColor,
                        onToggle = {
                            resetAutoHideTimer()
                            scope.launch { settingsRepository.setSeamEnabled(!seamEnabled) }
                        },
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                } else if (currentMode == AppMode.Legacy) {
                    val activeLegacy by legacyEngine.activeLegacy.collectAsState()
                    val ratio = activeLegacy?.progressPercentage?.div(100f) ?: 0f
                    CompanionIndicator(
                        mode = currentMode,
                        progressRatio = ratio,
                        isEnabled = gravityEnabled,
                        accentColor = accentColor,
                        grayColor = currentGrayColor,
                        onToggle = {
                            resetAutoHideTimer()
                            scope.launch { settingsRepository.setGravityEnabled(!gravityEnabled) }
                        },
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }

            // Center Content Region
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (currentMode) {
                    AppMode.Stopwatch -> StopwatchDisplay(
                        elapsedTimeMs = elapsedTimeMs,
                        showCentiseconds = showCentiseconds,
                        state = state,
                        mainSize = mainSize * scaleStopwatch,
                        accentColor = accentColor,
                        textColor = currentTextColor,
                        grayColor = currentGrayColor,
                        secondaryAlpha = secondaryAlpha,
                        scalePulse = scalePulse
                    )
                    AppMode.Countdown -> CountdownDisplay(
                        viewModel = viewModel,
                        countdownRemainingMs = countdownRemainingMs,
                        isCountdownRunning = isCountdownRunning,
                        mainSize = mainSize * scaleCountdown,
                        textColor = currentTextColor,
                        grayColor = currentGrayColor,
                        secondaryAlpha = secondaryAlpha,
                        scalePulse = scalePulse,
                        onInteraction = { resetAutoHideTimer() }
                    )
                    AppMode.Counter -> CounterDisplay(
                        counterValue = counterValue,
                        mainSize = mainSize * scaleCounter,
                        accentColor = accentColor,
                        textColor = currentTextColor,
                        grayColor = currentGrayColor,
                        secondaryAlpha = secondaryAlpha,
                        scalePulse = scalePulse,
                        onAmbientDimClick = { isAmbientDimActive = true }
                    )
                    AppMode.Intervals -> IntervalsDisplay(
                        intervalEngine = intervalEngine,
                        settingsRepository = settingsRepository,
                        mainSize = mainSize * scaleInterval,
                        accentColor = accentColor,
                        textColor = currentTextColor,
                        grayColor = currentGrayColor,
                        secondaryAlpha = secondaryAlpha,
                        scalePulse = scalePulse,
                        resetAutoHideTimer = { resetAutoHideTimer() }
                    )
                    AppMode.Legacy -> LegacyContent(
                        legacyEngine = legacyEngine,
                        accentColor = accentColor,
                        currentTextColor = currentTextColor,
                        currentGrayColor = currentGrayColor,
                        mainSize = mainSize * scaleLegacy,
                        secondaryAlpha = secondaryAlpha,
                        scalePulse = scalePulse,
                        resetAutoHideTimer = { resetAutoHideTimer() }
                    )
                }
            }

            // Bottom Action Buttons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 54.dp)
                    .graphicsLayer { alpha = controlsAlpha },
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeActionButtons(
                    currentMode = currentMode,
                    viewModel = viewModel,
                    intervalEngine = intervalEngine,
                    legacyEngine = legacyEngine,
                    state = state,
                    isCountdownRunning = isCountdownRunning,
                    intervalState = intervalState,
                    legacyState = legacyState,
                    accentColor = accentColor,
                    textColor = currentTextColor,
                    grayColor = currentGrayColor,
                    hapticController = hapticController,
                    hapticIntensity = hapticIntensity,
                    onInteraction = { resetAutoHideTimer() },
                    onPressPulse = { triggerPulse = it }
                )
            }

            if (laps.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .graphicsLayer { alpha = controlsAlpha }
                        .clickable {
                            resetAutoHideTimer()
                            showBottomSheet = true
                        }
                        .padding(8.dp)
                ) {
                    Text(
                        text = "عرض دورات اللفات (${laps.size})",
                        style = TextStyle(
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }

            // Top Right: Floating Quick Access & Settings Overlay (Rendered with zIndex=100f above Center Content)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(100f)
                    .graphicsLayer {
                        alpha = if (settingsPresentationState != SettingsPresentationState.Closed || showFloatingConfigDialog) 1.0f else controlsAlpha
                    }
            ) {
                SettingsOverlay(
                    settingsRepository = settingsRepository,
                    accentColor = accentColor,
                    currentGrayColor = currentGrayColor,
                    currentTextColor = currentTextColor,
                    presentationState = settingsPresentationState,
                    onStateChange = { newState ->
                        resetAutoHideTimer()
                        settingsPresentationState = newState
                    },
                    onFloatClick = {
                        resetAutoHideTimer()
                        if (android.provider.Settings.canDrawOverlays(context)) {
                            val intent = Intent(context, com.floating.stopwatch.service.StopwatchService::class.java)
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                            val targetIndex = when (currentMode) {
                                AppMode.Stopwatch -> 0
                                AppMode.Countdown -> 1
                                AppMode.Counter -> 2
                                AppMode.Intervals -> 3
                                AppMode.Legacy -> 4
                            }
                            val targetType = when (currentMode) {
                                AppMode.Stopwatch -> "stopwatch"
                                AppMode.Countdown -> "countdown"
                                AppMode.Counter -> "counter"
                                AppMode.Intervals -> "intervals"
                                AppMode.Legacy -> "legacy"
                            }
                            scope.launch {
                                settingsRepository.setWidgetType(targetIndex, targetType)
                                settingsRepository.setWidgetActive(targetIndex, true)
                            }
                        } else {
                            val intent = Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    },
                    onFloatLongClick = {
                        resetAutoHideTimer()
                        showFloatingConfigDialog = true
                    }
                )
            }
        }
    }

    if (showBottomSheet) {
        LapsBottomSheet(
            laps = laps,
            sheetState = sheetState,
            bgColor = currentBgColor,
            textColor = currentTextColor,
            grayColor = currentGrayColor,
            accentColor = accentColor,
            onDismiss = { showBottomSheet = false }
        )
    }

    if (showFloatingConfigDialog) {
        val targetIndex = when (currentMode) {
            AppMode.Stopwatch -> 0
            AppMode.Countdown -> 1
            AppMode.Counter -> 2
            AppMode.Intervals -> 3
            AppMode.Legacy -> 4
        }
        val modeName = when (currentMode) {
            AppMode.Stopwatch -> "مؤقت الساعات"
            AppMode.Countdown -> "العد التنازلي"
            AppMode.Counter -> "العداد التراكمي"
            AppMode.Intervals -> "المراحل الزمنية"
            AppMode.Legacy -> "الأهداف والإنجازات"
        }
        FloatingModeConfigDialog(
            modeIndex = targetIndex,
            modeName = modeName,
            settingsRepository = settingsRepository,
            accentColor = accentColor,
            onDismiss = { showFloatingConfigDialog = false }
        )
    }
    }
}


@Composable
private fun StopwatchDisplay(
    elapsedTimeMs: Long,
    showCentiseconds: Boolean,
    state: StopwatchState,
    mainSize: Float,
    accentColor: Color,
    textColor: Color,
    grayColor: Color,
    secondaryAlpha: Float,
    scalePulse: Float
) {
    val isAtZeroForFiveSecs = elapsedTimeMs == 0L && state == StopwatchState.Ready
    val infiniteTransition = rememberInfiniteTransition(label = "PulseAtZero")
    val breathingScale by if (isAtZeroForFiveSecs) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BreathingScale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TimeDisplay(
            elapsedTimeMs = elapsedTimeMs,
            showCentiseconds = showCentiseconds,
            baseStyle = TextStyle(color = textColor, fontSize = 54.sp),
            scaleFactor = mainSize,
            accentColor = accentColor,
            gradientGoldEnabled = false,
            modifier = Modifier
                .scale(scalePulse * breathingScale)
                .semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite }
        )

        Spacer(modifier = Modifier.height(16.dp))

        val stateArabic = when (state) {
            StopwatchState.Ready -> "جاهز"
            StopwatchState.Running -> "قيد التشغيل"
            StopwatchState.Paused -> "متوقف مؤقتاً"
        }
        Text(
            text = stateArabic,
            style = TextStyle(
                color = grayColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 3.sp
            ),
            modifier = Modifier.graphicsLayer { alpha = secondaryAlpha }
        )
    }
}

@Composable
private fun CountdownDisplay(
    viewModel: MainViewModel,
    countdownRemainingMs: Long,
    isCountdownRunning: Boolean,
    mainSize: Float,
    textColor: Color,
    grayColor: Color,
    secondaryAlpha: Float,
    scalePulse: Float,
    onInteraction: () -> Unit
) {
    val countdownDigitSize = (54f * mainSize).sp
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val dragThreshold = 18f * density
    var hDragAcc by remember { mutableFloatStateOf(0f) }
    var mDragAcc by remember { mutableFloatStateOf(0f) }
    var sDragAcc by remember { mutableFloatStateOf(0f) }

    val totalSeconds = countdownRemainingMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scalePulse)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hours Drag Zone
                Box(
                    modifier = Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onInteraction() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                hDragAcc += dragAmount.y
                                if (hDragAcc <= -dragThreshold) {
                                    viewModel.adjustCountdownHours(1)
                                    hDragAcc = 0f
                                } else if (hDragAcc >= dragThreshold) {
                                    viewModel.adjustCountdownHours(-1)
                                    hDragAcc = 0f
                                }
                            },
                            onDragEnd = { hDragAcc = 0f }
                        )
                    }
                ) {
                    Text(
                        text = String.format(Locale.US, "%02d", hours),
                        style = TextStyle(color = textColor, fontSize = countdownDigitSize, fontFamily = com.floating.stopwatch.ui.theme.DiwaniFontFamily, fontWeight = FontWeight.Light)
                    )
                }

                Text(" : ", style = TextStyle(color = textColor, fontSize = countdownDigitSize, fontWeight = FontWeight.Light))

                // Minutes Drag Zone
                Box(
                    modifier = Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onInteraction() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                mDragAcc += dragAmount.y
                                if (mDragAcc <= -dragThreshold) {
                                    viewModel.adjustCountdownMinutes(1)
                                    mDragAcc = 0f
                                } else if (mDragAcc >= dragThreshold) {
                                    viewModel.adjustCountdownMinutes(-1)
                                    mDragAcc = 0f
                                }
                            },
                            onDragEnd = { mDragAcc = 0f }
                        )
                    }
                ) {
                    Text(
                        text = String.format(Locale.US, "%02d", minutes),
                        style = TextStyle(color = textColor, fontSize = countdownDigitSize, fontFamily = com.floating.stopwatch.ui.theme.DiwaniFontFamily, fontWeight = FontWeight.Light)
                    )
                }

                Text(" : ", style = TextStyle(color = textColor, fontSize = countdownDigitSize, fontWeight = FontWeight.Light))

                // Seconds Drag Zone
                Box(
                    modifier = Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onInteraction() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                sDragAcc += dragAmount.y
                                if (sDragAcc <= -dragThreshold) {
                                    viewModel.adjustCountdownSeconds(1)
                                    sDragAcc = 0f
                                } else if (sDragAcc >= dragThreshold) {
                                    viewModel.adjustCountdownSeconds(-1)
                                    sDragAcc = 0f
                                }
                            },
                            onDragEnd = { sDragAcc = 0f }
                        )
                    }
                ) {
                    Text(
                        text = String.format(Locale.US, "%02d", seconds),
                        style = TextStyle(color = textColor, fontSize = countdownDigitSize, fontFamily = com.floating.stopwatch.ui.theme.DiwaniFontFamily, fontWeight = FontWeight.Light)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer { alpha = secondaryAlpha }
            ) {
                Text(text = "ساعات", style = TextStyle(color = grayColor, fontSize = 11.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp))
                Text(" : ", style = TextStyle(color = grayColor, fontSize = 11.sp, fontWeight = FontWeight.Light))
                Text(text = "دقائق", style = TextStyle(color = grayColor, fontSize = 11.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp))
                Text(" : ", style = TextStyle(color = grayColor, fontSize = 11.sp, fontWeight = FontWeight.Light))
                Text(text = "ثوانٍ", style = TextStyle(color = grayColor, fontSize = 11.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (!isCountdownRunning) "اسحب للأعلى/الأسفل للضبط" else "تركيز العد التنازلي",
                style = TextStyle(color = grayColor, fontSize = 11.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp),
                modifier = Modifier.graphicsLayer { alpha = secondaryAlpha }
            )
        }
    }
}

@Composable
private fun CounterDisplay(
    counterValue: Long,
    mainSize: Float,
    accentColor: Color,
    textColor: Color,
    grayColor: Color,
    secondaryAlpha: Float,
    scalePulse: Float,
    onAmbientDimClick: () -> Unit
) {
    val counterDigitSize = (72f * mainSize).sp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$counterValue",
            style = TextStyle(
                color = textColor,
                fontSize = counterDigitSize,
                fontWeight = FontWeight.Bold,
                fontFamily = com.floating.stopwatch.ui.theme.DiwaniFontFamily
            ),
            modifier = Modifier.scale(scalePulse)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "العداد التراكمي",
            style = TextStyle(
                color = grayColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp
            ),
            modifier = Modifier.graphicsLayer { alpha = secondaryAlpha }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "نمط الإعتام الموفر",
            style = TextStyle(
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            ),
            modifier = Modifier
                .graphicsLayer { alpha = secondaryAlpha }
                .clickable { onAmbientDimClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun IntervalsDisplay(
    intervalEngine: IntervalEngine,
    settingsRepository: SettingsRepository,
    mainSize: Float,
    accentColor: Color,
    textColor: Color,
    grayColor: Color,
    secondaryAlpha: Float,
    scalePulse: Float,
    resetAutoHideTimer: () -> Unit
) {
    val activeTemplate by intervalEngine.activeTemplate.collectAsState()
    val currentRound by intervalEngine.currentRound.collectAsState()
    val stageRemainingMs by intervalEngine.stageRemainingMs.collectAsState()
    val currentStage = intervalEngine.getCurrentStage()
    val nextStage = intervalEngine.getNextStage()
    val scope = rememberCoroutineScope()

    var showBuilderDialog by remember { mutableStateOf(false) }

    if (activeTemplate != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = activeTemplate!!.name.uppercase(),
                    style = TextStyle(color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "[تعديل]",
                    style = TextStyle(color = grayColor, fontSize = 10.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp),
                    modifier = Modifier
                        .graphicsLayer { alpha = secondaryAlpha }
                        .clickable {
                            resetAutoHideTimer()
                            showBuilderDialog = true
                        }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val stageNameArabic = when (currentStage?.type) {
                IntervalStageType.WORK -> "مرحلة العمل"
                IntervalStageType.REST -> "مرحلة الراحة"
                null -> "جاهز"
            }

            Text(
                text = stageNameArabic,
                style = TextStyle(
                    color = if (currentStage?.type == IntervalStageType.WORK) accentColor else textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            TimeDisplay(
                elapsedTimeMs = stageRemainingMs,
                showCentiseconds = true,
                baseStyle = TextStyle(color = textColor, fontSize = 48.sp),
                scaleFactor = mainSize,
                accentColor = accentColor,
                modifier = Modifier.scale(scalePulse)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "الجولة $currentRound / ${activeTemplate!!.repetitions}",
                style = TextStyle(color = grayColor, fontSize = 12.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp),
                modifier = Modifier.graphicsLayer { alpha = secondaryAlpha }
            )

            if (nextStage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val nextSecs = nextStage.durationMs / 1000
                val nextTypeArabic = if (nextStage.type == IntervalStageType.WORK) "العمل" else "الراحة"
                Text(
                    text = "التالي: $nextTypeArabic (${nextSecs} ث)",
                    style = TextStyle(color = grayColor.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Normal, letterSpacing = 1.sp),
                    modifier = Modifier.graphicsLayer { alpha = secondaryAlpha }
                )
            }
        }
    }

    if (showBuilderDialog && activeTemplate != null) {
        IntervalQuickEditDialog(
            initialTemplate = activeTemplate!!,
            onDismiss = { showBuilderDialog = false },
            onSave = { updatedTemplate ->
                intervalEngine.loadTemplate(updatedTemplate)
                scope.launch {
                    settingsRepository.setIntervalConfig(
                        name = updatedTemplate.name,
                        workMs = updatedTemplate.workDurationMs,
                        restMs = updatedTemplate.restDurationMs,
                        rounds = updatedTemplate.repetitions
                    )
                }
                showBuilderDialog = false
            }
        )
    }
}

@Composable
private fun ModeActionButtons(
    currentMode: AppMode,
    viewModel: MainViewModel,
    intervalEngine: IntervalEngine,
    legacyEngine: LegacyEngine,
    state: StopwatchState,
    isCountdownRunning: Boolean,
    intervalState: IntervalState,
    legacyState: LegacyState,
    accentColor: Color,
    textColor: Color,
    grayColor: Color,
    hapticController: HapticController,
    hapticIntensity: String,
    onInteraction: () -> Unit,
    onPressPulse: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()

    val currentStopwatchState by rememberUpdatedState(state)
    val currentCountdownRunning by rememberUpdatedState(isCountdownRunning)
    val currentIntervalState by rememberUpdatedState(intervalState)
    val currentLegacyState by rememberUpdatedState(legacyState)
    val currentHapticIntensity by rememberUpdatedState(hapticIntensity)

    when (currentMode) {
        AppMode.Stopwatch -> {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clickable {
                        onInteraction()
                        if (currentStopwatchState == StopwatchState.Running) {
                            hapticController.trigger(currentHapticIntensity, "Lap")
                            viewModel.lap()
                        } else if (currentStopwatchState == StopwatchState.Paused) {
                            hapticController.trigger(currentHapticIntensity, "Reset")
                            viewModel.reset()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, grayColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (currentStopwatchState == StopwatchState.Paused) "إعادة ضبط" else "دورة",
                            style = TextStyle(color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp)
                        )
                    }
                }
            }

            val buttonColor = if (currentStopwatchState == StopwatchState.Running) Color(0xFF9E2A2B) else accentColor
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(buttonColor)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onInteraction()
                                onPressPulse(true)
                                tryAwaitRelease()
                                onPressPulse(false)
                            },
                            onTap = {
                                onInteraction()
                                if (currentStopwatchState == StopwatchState.Running) {
                                    hapticController.trigger(currentHapticIntensity, "Stop")
                                    viewModel.pause()
                                } else {
                                    hapticController.trigger(currentHapticIntensity, "Start")
                                    viewModel.start()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentStopwatchState == StopwatchState.Running) "إيقاف" else "بدء",
                    style = TextStyle(color = LuxuryColors.WarmBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )
            }
        }
        AppMode.Countdown -> {
            var isPressingCountdownReset by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onInteraction()
                                isPressingCountdownReset = true
                                var zeroTriggered = false
                                val job = scope.launch {
                                    kotlinx.coroutines.delay(500L)
                                    zeroTriggered = true
                                    hapticController.trigger(hapticIntensity, "Reset")
                                    viewModel.resetCountdownToZero()
                                }
                                val released = tryAwaitRelease()
                                isPressingCountdownReset = false
                                if (released && !zeroTriggered) {
                                    job.cancel()
                                    hapticController.trigger(hapticIntensity, "Reset")
                                    viewModel.resetCountdown()
                                } else if (!released) {
                                    job.cancel()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, if (isPressingCountdownReset) accentColor else grayColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("إعادة ضبط", style = TextStyle(color = if (isPressingCountdownReset) accentColor else textColor, fontSize = 11.sp, letterSpacing = 1.sp))
                    }
                }
            }

            val countdownBtnColor = if (isCountdownRunning) Color(0xFF9E2A2B) else accentColor
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(countdownBtnColor)
                    .clickable {
                        onInteraction()
                        if (isCountdownRunning) {
                            hapticController.trigger(hapticIntensity, "Stop")
                            viewModel.pauseCountdown()
                        } else {
                            hapticController.trigger(hapticIntensity, "Start")
                            viewModel.startCountdown()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCountdownRunning) "إيقاف مؤقت" else "بدء",
                    style = TextStyle(color = LuxuryColors.WarmBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )
            }
        }
        AppMode.Counter -> {
            var isPressingReset by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onInteraction()
                                isPressingReset = true
                                var resetTriggered = false
                                val job = scope.launch {
                                    kotlinx.coroutines.delay(500L)
                                    resetTriggered = true
                                    hapticController.trigger(hapticIntensity, "Reset")
                                    viewModel.resetCounter()
                                }
                                tryAwaitRelease()
                                isPressingReset = false
                                if (!resetTriggered) job.cancel()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, if (isPressingReset) accentColor else grayColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "إعادة ضبط",
                            style = TextStyle(color = if (isPressingReset) accentColor else textColor, fontSize = 11.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clickable {
                        onInteraction()
                        hapticController.trigger(hapticIntensity, "Lap")
                        viewModel.decrementCounter()
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, grayColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("− 1", style = TextStyle(color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .clickable {
                        onInteraction()
                        hapticController.trigger(hapticIntensity, "Lap")
                        viewModel.incrementCounter()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("+ 1", style = TextStyle(color = LuxuryColors.WarmBlack, fontSize = 20.sp, fontWeight = FontWeight.Bold))
            }
        }
        AppMode.Intervals -> {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clickable {
                        onInteraction()
                        hapticController.trigger(hapticIntensity, "Reset")
                        intervalEngine.reset()
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, grayColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("إعادة ضبط", style = TextStyle(color = textColor, fontSize = 11.sp, letterSpacing = 1.sp))
                    }
                }
            }

            val isRunning = intervalState == IntervalState.RUNNING
            val intervalBtnColor = if (isRunning) Color(0xFF9E2A2B) else accentColor
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(intervalBtnColor)
                    .clickable {
                        onInteraction()
                        if (isRunning) {
                            hapticController.trigger(hapticIntensity, "Stop")
                            intervalEngine.pause()
                        } else {
                            hapticController.trigger(hapticIntensity, "Start")
                            intervalEngine.start(scope)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRunning) "إيقاف مؤقت" else "بدء",
                    style = TextStyle(color = LuxuryColors.WarmBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )
            }
        }
        AppMode.Legacy -> {
            val activeLegacy by legacyEngine.activeLegacy.collectAsState()
            if (activeLegacy != null) {
                var isPressingDelete by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .pointerInput(activeLegacy?.id) {
                            detectTapGestures(
                                onPress = {
                                    onInteraction()
                                    isPressingDelete = true
                                    var deleteTriggered = false
                                    val targetId = activeLegacy?.id
                                    val job = scope.launch {
                                        kotlinx.coroutines.delay(500L)
                                        if (targetId != null) {
                                            deleteTriggered = true
                                            hapticController.trigger(currentHapticIntensity, "Reset")
                                            legacyEngine.deleteLegacy(targetId)
                                        }
                                    }
                                    val released = tryAwaitRelease()
                                    isPressingDelete = false
                                    if (!deleteTriggered) {
                                        job.cancel()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, if (isPressingDelete) Color(0xFFE53935) else Color(0xFFC94A4A))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("حذف", style = TextStyle(color = if (isPressingDelete) Color(0xFFE53935) else Color(0xFFC94A4A), fontSize = 11.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold))
                        }
                    }
                }

                val isRunning = legacyState == LegacyState.RUNNING
                val legacyBtnColor = if (isRunning) Color(0xFF9E2A2B) else accentColor
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(legacyBtnColor)
                        .clickable {
                            onInteraction()
                            if (isRunning) {
                                hapticController.trigger(hapticIntensity, "Stop")
                                legacyEngine.pause()
                            } else {
                                hapticController.trigger(hapticIntensity, "Start")
                                legacyEngine.start(scope)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRunning) "إيقاف مؤقت" else if (legacyState == LegacyState.PAUSED) "استئناف" else "بدء",
                        style = TextStyle(color = LuxuryColors.WarmBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LapsBottomSheet(
    laps: List<Lap>,
    sheetState: SheetState,
    bgColor: Color,
    textColor: Color,
    grayColor: Color,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = grayColor) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "سجل اللفات والتحليلات",
                style = TextStyle(color = textColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraLight, letterSpacing = 4.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val fastestLap = if (laps.size >= 2) laps.minByOrNull { it.lapTimeMs } else null
            val slowestLap = if (laps.size >= 2) laps.maxByOrNull { it.lapTimeMs } else null

            if (laps.isNotEmpty()) {
                val avgLapTime = laps.map { it.lapTimeMs }.average().toLong()
                val avgMins = (avgLapTime / 1000) / 60
                val avgSecs = (avgLapTime / 1000) % 60
                val avgCents = (avgLapTime % 1000) / 10
                val formattedAvg = String.format(Locale.US, "%02d:%02d.%02d", avgMins, avgSecs, avgCents)

                Card(
                    colors = CardDefaults.cardColors(containerColor = grayColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "تحليلات الجلسة", color = grayColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            val context = LocalContext.current
                            Text(
                                text = "مشاركة البطاقة",
                                color = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier
                                    .clickable {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "بطاقة جلسة لحظات")
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "🏆 بطاقة ملخص الجلسة 🏆\n" +
                                                "------------------------------\n" +
                                                "• متوسط زمن اللفة: $formattedAvg\n" +
                                                (if (fastestLap != null) "• أسرع لفة: اللفة ${fastestLap.lapIndex} (${fastestLap.lapTimeMs / 1000f} ث)\n" else "") +
                                                "• إجمالي عدد اللفات: ${laps.size}\n" +
                                                "------------------------------\n" +
                                                "تطبيق لحظات"
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة معلومات الجلسة"))
                                    }
                                    .padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "متوسط اللفة", color = grayColor, fontSize = 11.sp)
                                Text(text = formattedAvg, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (fastestLap != null) {
                                Column {
                                    Text(text = "أسرع لفة", color = Color(0xFF4AC98F), fontSize = 11.sp)
                                    val fS = fastestLap.lapTimeMs / 1000
                                    Text(text = "اللفة ${fastestLap.lapIndex} (${fS}ث)", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(laps.reversed()) { lap ->
                    val lapColor = when {
                        fastestLap != null && lap.lapIndex == fastestLap.lapIndex -> Color(0xFF4AC98F)
                        slowestLap != null && lap.lapIndex == slowestLap.lapIndex -> Color(0xFFC94A4A)
                        else -> textColor
                    }
                    LapRowItem(
                        lap = lap,
                        textColor = lapColor,
                        grayColor = grayColor
                    )
                    HorizontalDivider(color = grayColor.copy(alpha = 0.2f))
                }
            }
        }
    }
}

fun formatIntervalDuration(totalSecs: Int): String {
    val hrs = totalSecs / 3600
    val mins = (totalSecs % 3600) / 60
    val secs = totalSecs % 60
    return if (hrs > 0) {
        String.format(Locale.US, "%dh %02dm %02ds", hrs, mins, secs)
    } else if (mins > 0) {
        String.format(Locale.US, "%dm %02ds", mins, secs)
    } else {
        "${secs}s"
    }
}

fun formatDurationHoursMinutes(totalSecs: Int): String {
    return String.format(Locale.US, "%02d:%02d", totalSecs / 3600, (totalSecs % 3600) / 60)
}

@Composable
fun IntervalQuickEditDialog(
    initialTemplate: IntervalTemplate,
    onDismiss: () -> Unit,
    onSave: (IntervalTemplate) -> Unit
) {
    var templateName by remember { mutableStateOf(initialTemplate.name) }
    var workSecs by remember { mutableIntStateOf((initialTemplate.workDurationMs / 1000).toInt()) }
    var restSecs by remember { mutableIntStateOf((initialTemplate.restDurationMs / 1000).toInt()) }
    var repetitions by remember { mutableIntStateOf(initialTemplate.repetitions) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF2C2C2E)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "إعدادات المراحل الزمنية",
                    style = TextStyle(color = LuxuryColors.AccentGold, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("اسم المكون", color = LuxuryColors.WarmGray, fontSize = 10.sp) },
                    textStyle = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 12.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                DragAdjustField(
                    label = "مدة العمل",
                    value = workSecs.toFloat(),
                    minValue = 1f,
                    maxValue = 18000f,
                    pixelsPerUnit = 4f,
                    accentColor = LuxuryColors.AccentGold,
                    valueFormatter = { formatDurationHoursMinutes(it.toInt()) },
                    onValueChange = { workSecs = it.toInt().coerceIn(1, 18000) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                DragAdjustField(
                    label = "مدة الراحة",
                    value = restSecs.toFloat(),
                    minValue = 1f,
                    maxValue = 3600f,
                    pixelsPerUnit = 4f,
                    accentColor = LuxuryColors.AccentGold,
                    valueFormatter = { formatDurationHoursMinutes(it.toInt()) },
                    onValueChange = { restSecs = it.toInt().coerceIn(1, 3600) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("عدد الجولات: $repetitions", color = LuxuryColors.CreamyWhite, fontSize = 11.sp)
                    Row {
                        Box(
                            modifier = Modifier
                                .clickable { if (repetitions > 1) repetitions -= 1 }
                                .padding(8.dp)
                        ) {
                            Text("-", color = LuxuryColors.AccentGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clickable { repetitions += 1 }
                                .padding(8.dp)
                        ) {
                            Text("+", color = LuxuryColors.AccentGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "إلغاء",
                        color = LuxuryColors.WarmGray,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val updated = IntervalTemplate(
                                id = initialTemplate.id,
                                name = templateName.ifBlank { "تدريب" },
                                workDurationMs = workSecs * 1000L,
                                restDurationMs = restSecs * 1000L,
                                repetitions = repetitions
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryColors.AccentGold)
                    ) {
                        Text("حفظ", color = LuxuryColors.WarmBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
    val formattedCum = String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centiseconds)

    val lapSecs = lap.lapTimeMs / 1000
    val lapMins = (lapSecs % 3600) / 60
    val lapS = lapSecs % 60
    val lapCent = (lap.lapTimeMs % 1000) / 10
    val formattedLap = String.format(Locale.US, "%02d:%02d.%02d", lapMins, lapS, lapCent)

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
                text = "اللفة ${lap.lapIndex}",
                style = TextStyle(color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            )
            Text(
                text = formattedCum,
                style = TextStyle(color = grayColor, fontSize = 11.sp, fontWeight = FontWeight.Light)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formattedLap,
                style = TextStyle(color = textColor, fontSize = 14.sp, fontFamily = com.floating.stopwatch.ui.theme.DiwaniFontFamily, fontWeight = FontWeight.SemiBold)
            )
            if (lap.lapIndex > 1) {
                Spacer(modifier = Modifier.width(12.dp))
                val colorDelta = if (lap.diffFromPreviousMs > 0) Color(0xFFC94A4A) else Color(0xFF4AC98F)
                Text(
                    text = formattedDelta,
                    style = TextStyle(color = colorDelta, fontSize = 11.sp, fontWeight = FontWeight.Light)
                )
            }
        }
    }
}
