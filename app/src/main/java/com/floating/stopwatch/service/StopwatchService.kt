package com.floating.stopwatch.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.floating.stopwatch.MainActivity
import com.floating.stopwatch.R
import com.floating.stopwatch.data.SettingsRepository
import com.floating.stopwatch.domain.HapticController
import com.floating.stopwatch.domain.StopwatchEngine
import com.floating.stopwatch.ui.components.TimeDisplay
import com.floating.stopwatch.ui.theme.LuxuryColors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.roundToInt
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class StopwatchService : Service() {

    companion object {
        const val CHANNEL_ID = "StopwatchOverlayChannel"
        const val NOTIFICATION_ID = 4842

        // Multi-widget tracking list of instances
        private val activeServices = mutableListOf<StopwatchService>()

        private var sharedEngine: StopwatchEngine? = null

        fun getEngine(): StopwatchEngine {
            if (sharedEngine == null) {
                sharedEngine = StopwatchEngine()
            }
            return sharedEngine!!
        }

        fun triggerHapticOnAll(intensity: String, effect: String) {
            activeServices.forEach {
                try {
                    it.hapticController.trigger(intensity, effect)
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    private lateinit var windowManager: WindowManager
    private lateinit var settingsRepository: SettingsRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var hapticController: HapticController

    // Multi-Widget context structures
    private val activeOverlays = mutableMapOf<Int, ActiveOverlay>()
    private val widgetStates = List(5) { index -> WidgetState(index) }
    private val tickerJobs = mutableMapOf<Int, Job>()

    private class ActiveOverlay(
        val index: Int,
        val lifecycleOwner: ComposeOverlayLifecycleOwner,
        val composeView: ComposeView,
        val params: WindowManager.LayoutParams
    )

    private class WidgetState(
        val index: Int,
        val type: MutableStateFlow<String> = MutableStateFlow("stopwatch"),
        val running: MutableStateFlow<Boolean> = MutableStateFlow(false),
        val elapsedOrValue: MutableStateFlow<Long> = MutableStateFlow(0L),
        val baseTime: MutableStateFlow<Long> = MutableStateFlow(0L),
        val countdownDuration: MutableStateFlow<Int> = MutableStateFlow(300), // default 5 mins
        val tapCount: MutableStateFlow<Int> = MutableStateFlow(0),
        val isVolumeCounterActive: MutableStateFlow<Boolean> = MutableStateFlow(false),
        val milestones: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    )

    // Battery level state flow
    private val _batteryPercentage = MutableStateFlow(100)
    private val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()

    private val batteryReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) {
                    val pct = (level * 100 / scale.toFloat()).toInt()
                    _batteryPercentage.value = pct
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        activeServices.add(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        settingsRepository = SettingsRepository(applicationContext)
        hapticController = HapticController(applicationContext)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        // Monitor and auto-sync active widgets list
        startWidgetLifecycleManager()

        // Register battery receiver for real-time tracking
        registerReceiver(batteryReceiver, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startWidgetLifecycleManager() {
        // Observe settings for up to 5 widgets and spawn/dismiss them reactively
        for (i in 0..4) {
            serviceScope.launch {
                settingsRepository.isWidgetActive(i).collectLatest { active ->
                    if (active) {
                        if (!activeOverlays.containsKey(i)) {
                            // Fetch persisted state first
                            val type = settingsRepository.getWidgetType(i).first()
                            val value = settingsRepository.getWidgetValue(i).first()
                            val running = settingsRepository.isWidgetRunning(i).first()

                            widgetStates[i].type.value = type
                            widgetStates[i].elapsedOrValue.value = value
                            widgetStates[i].running.value = running
                            if (type == "counter") {
                                widgetStates[i].tapCount.value = value.toInt()
                            }

                            spawnWidget(i)

                            if (running) {
                                widgetStates[i].baseTime.value = SystemClock.elapsedRealtime()
                                startTickerForWidget(i)
                            }
                        }
                    } else {
                        dismissWidget(i)
                    }
                }
            }

            // Sync type changes dynamically
            serviceScope.launch {
                settingsRepository.getWidgetType(i).collectLatest { type ->
                    widgetStates[i].type.value = type
                    if (type == "counter") {
                        widgetStates[i].tapCount.value = widgetStates[i].elapsedOrValue.value.toInt()
                    }
                }
            }
        }
    }

    private fun spawnWidget(index: Int) {
        val owner = ComposeOverlayLifecycleOwner().apply {
            onCreate()
            onStart()
            onResume()
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)

            setContent {
                val state = widgetStates[index]
                val type by state.type.collectAsState()
                val running by state.running.collectAsState()
                val elapsedOrValue by state.elapsedOrValue.collectAsState()
                val tapCount by state.tapCount.collectAsState()
                val isVolumeActive by state.isVolumeCounterActive.collectAsState()
                val milestones by state.milestones.collectAsState()

                val mainSize by settingsRepository.mainSize.collectAsState(initial = 1.0f)
                val floatingSize by settingsRepository.floatingSize.collectAsState(initial = 0.5f)
                val floatingWidth by settingsRepository.getWidgetWidth(index).collectAsState(initial = 170.0f)
                val floatingHeight by settingsRepository.getWidgetHeight(index).collectAsState(initial = 56.0f)
                val showCentisecondsFloating by settingsRepository.showCentisecondsFloating.collectAsState(initial = true)
                val stylePreset by settingsRepository.stylePreset.collectAsState(initial = "Glass Premium")
                val colorPreset by settingsRepository.colorPreset.collectAsState(initial = "Gold")
                val customColorHex by settingsRepository.customColorHex.collectAsState(initial = "#C9A66B")
                val hapticIntensity by settingsRepository.hapticIntensity.collectAsState(initial = "Medium")

                val shapePreset by settingsRepository.shapePreset.collectAsState(initial = "rounded")
                val fontSizeScale by settingsRepository.fontSizeScale.collectAsState(initial = 1.0f)
                val gradientEnabled by settingsRepository.gradientEnabled.collectAsState(initial = false)
                val layoutOrientation by settingsRepository.layoutOrientation.collectAsState(initial = "horizontal")
                val floatingPadding by settingsRepository.floatingPadding.collectAsState(initial = 6.0f)
                val floatingOpacity by settingsRepository.floatingOpacity.collectAsState(initial = 0.85f)
                val glowingBorder by settingsRepository.glowingBorder.collectAsState(initial = false)
                val showBatteryIndicator by settingsRepository.showBatteryIndicator.collectAsState(initial = true)
                val currentBatteryPct by batteryPercentage.collectAsState()

                val accentColor = if (colorPreset == "Custom") {
                    try { Color(android.graphics.Color.parseColor(customColorHex)) } catch (e: Exception) { LuxuryColors.AccentGold }
                } else {
                    LuxuryColors.fromName(colorPreset)
                }

                // Coordinate bindings
                val initialX by settingsRepository.getWidgetX(index).collectAsState(initial = -1.0f)
                val initialY by settingsRepository.getWidgetY(index).collectAsState(initial = -1.0f)

                LaunchedEffect(initialX, initialY) {
                    val overlay = activeOverlays[index]
                    if (initialX != -1.0f && initialY != -1.0f && overlay != null) {
                        overlay.params.x = initialX.roundToInt()
                        overlay.params.y = initialY.roundToInt()
                        windowManager.updateViewLayout(overlay.composeView, overlay.params)
                    }
                }

                LaunchedEffect(floatingWidth, floatingHeight) {
                    val overlay = activeOverlays[index]
                    if (overlay != null) {
                        val w = if (floatingWidth < 1f) 1 else floatingWidth.toInt()
                        val h = if (floatingHeight < 1f) 1 else floatingHeight.toInt()
                        overlay.params.width = if (w.dpToPx() < 1) 1 else w.dpToPx()
                        overlay.params.height = if (h.dpToPx() < 1) 1 else h.dpToPx()
                        windowManager.updateViewLayout(overlay.composeView, overlay.params)
                    }
                }

                // Volume button dynamic flags intercept controller
                LaunchedEffect(isVolumeActive, type) {
                    val overlay = activeOverlays[index]
                    if (overlay != null) {
                        if (isVolumeActive && type == "counter") {
                            // Focusable to catch volume buttons
                            overlay.params.flags = overlay.params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                        } else {
                            // Non-focusable to pass keys back to the system
                            overlay.params.flags = overlay.params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        }
                        windowManager.updateViewLayout(overlay.composeView, overlay.params)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    ThemedOverlayContainer(
                        index = index,
                        widgetType = type,
                        running = running,
                        elapsedTimeMs = elapsedOrValue,
                        tapCount = tapCount,
                        isVolumeActive = isVolumeActive,
                        milestones = milestones,
                        showCentiseconds = showCentisecondsFloating,
                        stylePreset = stylePreset,
                        accentColor = accentColor,
                        shapePreset = shapePreset,
                        fontSizeScale = fontSizeScale,
                        gradientEnabled = gradientEnabled,
                        layoutOrientation = layoutOrientation,
                        paddingDpValue = floatingPadding,
                        opacity = floatingOpacity,
                        glowingBorder = glowingBorder,
                        showBatteryIndicator = showBatteryIndicator,
                        batteryPercentage = currentBatteryPct,
                        onMovementDrag = { dx, dy ->
                            activeOverlays[index]?.let {
                                it.params.x += dx.roundToInt()
                                it.params.y += dy.roundToInt()
                                windowManager.updateViewLayout(it.composeView, it.params)
                            }
                        },
                        onMovementRelease = {
                            activeOverlays[index]?.let {
                                smartEdgeSnapAndClamp(it.params)
                                serviceScope.launch {
                                    settingsRepository.setWidgetPosition(index, it.params.x.toFloat(), it.params.y.toFloat())
                                }
                            }
                        },
                        onAction = { action ->
                            when (action) {
                                "Start" -> {
                                    hapticController.trigger(hapticIntensity, "Start")
                                    state.running.value = true
                                    state.baseTime.value = SystemClock.elapsedRealtime()
                                    startTickerForWidget(index)
                                    serviceScope.launch { settingsRepository.setWidgetRunning(index, true) }
                                }
                                "Stop" -> {
                                    hapticController.trigger(hapticIntensity, "Stop")
                                    state.running.value = false
                                    stopTickerForWidget(index)
                                    serviceScope.launch { settingsRepository.setWidgetRunning(index, false) }
                                }
                                "Reset" -> {
                                    hapticController.trigger(hapticIntensity, "Reset")
                                    state.running.value = false
                                    stopTickerForWidget(index)
                                    if (type == "countdown") {
                                        state.elapsedOrValue.value = state.countdownDuration.value * 1000L
                                        serviceScope.launch { settingsRepository.setWidgetValue(index, state.countdownDuration.value * 1000L) }
                                    } else {
                                        state.elapsedOrValue.value = 0L
                                        state.tapCount.value = 0
                                        serviceScope.launch { settingsRepository.setWidgetValue(index, 0L) }
                                    }
                                    serviceScope.launch { settingsRepository.setWidgetRunning(index, false) }
                                }
                                "Increment" -> {
                                    hapticController.trigger(hapticIntensity, "Lap")
                                    state.tapCount.value++
                                    state.elapsedOrValue.value = state.tapCount.value.toLong()
                                    serviceScope.launch { settingsRepository.setWidgetValue(index, state.tapCount.value.toLong()) }
                                }
                                "Decrement" -> {
                                    hapticController.trigger(hapticIntensity, "Reset")
                                    if (state.tapCount.value > 0) {
                                        state.tapCount.value--
                                        state.elapsedOrValue.value = state.tapCount.value.toLong()
                                        serviceScope.launch { settingsRepository.setWidgetValue(index, state.tapCount.value.toLong()) }
                                    }
                                }
                                "ToggleVolume" -> {
                                    hapticController.trigger(hapticIntensity, "Lap")
                                    state.isVolumeCounterActive.value = !state.isVolumeCounterActive.value
                                }
                                "Milestone" -> {
                                    hapticController.trigger(hapticIntensity, "Lap")
                                    val currentMilestone = if (type == "countdown") {
                                        "Focus Session remaining: " + formatDuration(elapsedOrValue)
                                    } else {
                                        "Time record: " + formatDuration(elapsedOrValue)
                                    }
                                    state.milestones.value = state.milestones.value + currentMilestone
                                }
                            }
                        }
                    )
                }
            }
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            170.dpToPx(),
            56.dpToPx(),
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100 + index * 40
            y = 200 + index * 80
        }

        activeOverlays[index] = ActiveOverlay(index, owner, composeView, params)
        windowManager.addView(composeView, params)
    }

    private fun dismissWidget(index: Int) {
        stopTickerForWidget(index)
        val overlay = activeOverlays.remove(index) ?: return
        overlay.lifecycleOwner.apply {
            onPause()
            onStop()
            onDestroy()
        }
        try {
            windowManager.removeView(overlay.composeView)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun startTickerForWidget(index: Int) {
        tickerJobs[index]?.cancel()
        val state = widgetStates[index]
        tickerJobs[index] = serviceScope.launch(Dispatchers.Default) {
            while (true) {
                if (state.running.value) {
                    if (state.type.value == "stopwatch") {
                        val elapsed = state.elapsedOrValue.value + (SystemClock.elapsedRealtime() - state.baseTime.value)
                        state.elapsedOrValue.value = elapsed
                        state.baseTime.value = SystemClock.elapsedRealtime()
                        settingsRepository.setWidgetValue(index, elapsed)
                    } else if (state.type.value == "countdown") {
                        val currentRemaining = state.elapsedOrValue.value - (SystemClock.elapsedRealtime() - state.baseTime.value)
                        state.baseTime.value = SystemClock.elapsedRealtime()
                        if (currentRemaining <= 0L) {
                            state.elapsedOrValue.value = 0L
                            state.running.value = false
                            settingsRepository.setWidgetValue(index, 0L)
                            settingsRepository.setWidgetRunning(index, false)
                            triggerCountdownCompletion(index)
                            break
                        } else {
                            state.elapsedOrValue.value = currentRemaining
                            settingsRepository.setWidgetValue(index, currentRemaining)
                        }
                    }
                }
                delay(10)
            }
        }
    }

    private fun stopTickerForWidget(index: Int) {
        tickerJobs[index]?.cancel()
        tickerJobs.remove(index)
    }

    private fun triggerCountdownCompletion(index: Int) {
        serviceScope.launch {
            hapticController.trigger("Strong", "Reset")
            // Send complete notification alert
            val notification = NotificationCompat.Builder(this@StopwatchService, CHANNEL_ID)
                .setContentTitle("Focus Session Complete!")
                .setContentText("Focus session countdown for Widget #$index has finished.")
                .setSmallIcon(R.drawable.ic_stat_stopwatch)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            val manager = getSystemService(NotificationManager::class.java)
            manager?.notify(5000 + index, notification)
        }
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        val cents = (ms % 1000) / 10
        return String.format("%02d:%02d.%02d", mins, secs, cents)
    }

    private fun smartEdgeSnapAndClamp(lp: WindowManager.LayoutParams) {
        val metrics = applicationContext.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        val leftX = 0
        val rightX = screenWidth - lp.width
        val topY = 0
        val bottomY = screenHeight - lp.height

        if (lp.x < leftX) lp.x = leftX
        if (lp.x > rightX) lp.x = rightX
        if (lp.y < topY) lp.y = topY
        if (lp.y > bottomY) lp.y = bottomY

        // Clean layout refresh
        activeOverlays.values.find { it.params == lp }?.let {
            windowManager.updateViewLayout(it.composeView, lp)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        activeOverlays.values.forEach {
            smartEdgeSnapAndClamp(it.params)
        }
    }

    @Composable
    fun ThemedOverlayContainer(
        index: Int,
        widgetType: String,
        running: Boolean,
        elapsedTimeMs: Long,
        tapCount: Int,
        isVolumeActive: Boolean,
        milestones: List<String>,
        showCentiseconds: Boolean,
        stylePreset: String,
        accentColor: Color,
        shapePreset: String,
        fontSizeScale: Float,
        gradientEnabled: Boolean,
        layoutOrientation: String,
        paddingDpValue: Float,
        opacity: Float,
        glowingBorder: Boolean,
        showBatteryIndicator: Boolean,
        batteryPercentage: Int,
        onMovementDrag: (Float, Float) -> Unit,
        onMovementRelease: () -> Unit,
        onAction: (String) -> Unit
    ) {
        val finalCornerRadius = when (shapePreset) {
            "capsule" -> 32.dp
            "circle" -> 99.dp
            "sharp" -> 0.dp
            else -> 16.dp
        }

        val safePadding = paddingDpValue.coerceAtLeast(0.0f)
        var showMenu by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (widgetType == "counter" && !showMenu && !isVolumeActive) {
                                onAction("Increment")
                            } else {
                                showMenu = !showMenu
                            }
                        },
                        onLongPress = {
                            showMenu = !showMenu
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = { onMovementRelease() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onMovementDrag(dragAmount.x, dragAmount.y)
                        }
                    )
                }
                .onKeyEvent { keyEvent ->
                    if (isVolumeActive && widgetType == "counter" && keyEvent.type == KeyEventType.KeyDown) {
                        if (keyEvent.key == Key.VolumeUp) {
                            onAction("Increment")
                            true
                        } else if (keyEvent.key == Key.VolumeDown) {
                            onAction("Decrement")
                            true
                        } else false
                    } else false
                },
            contentAlignment = Alignment.Center
        ) {
            // Backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (stylePreset == "Glass Premium" || shapePreset == "glass") {
                            Modifier
                                .background(Color.White.copy(alpha = 0.12f * opacity), RoundedCornerShape(finalCornerRadius))
                                .blur(16.dp)
                        } else if (stylePreset == "Obsidian") {
                            Modifier.background(Color(0xFF0A0A0A).copy(alpha = 0.88f * opacity), RoundedCornerShape(finalCornerRadius))
                        } else if (stylePreset == "Titanium") {
                            val titaniumBrush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF2C2F33), Color(0xFF1E2124))
                            )
                            Modifier.background(titaniumBrush, RoundedCornerShape(finalCornerRadius))
                        } else {
                            Modifier.background(Color.Black.copy(alpha = opacity), RoundedCornerShape(finalCornerRadius))
                        }
                    )
                    .then(
                        if (glowingBorder) {
                            Modifier.background(Color.Transparent, RoundedCornerShape(finalCornerRadius))
                                .then(
                                    Modifier.shadow(elevation = 2.dp, shape = RoundedCornerShape(finalCornerRadius), ambientColor = accentColor, spotColor = accentColor)
                                )
                        } else {
                            Modifier
                        }
                    )
            )

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(safePadding.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showMenu) {
                    // Responsive Color Dots Menu
                    val scaleFactor = (14.dp.value * fontSizeScale).coerceAtLeast(10.0f).dp
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.95f), RoundedCornerShape(finalCornerRadius))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Start Dot (🟢)
                        Box(
                            modifier = Modifier
                                .size(scaleFactor)
                                .clip(CircleShape)
                                .background(Color(0xFF4AC98F))
                                .clickable {
                                    if (widgetType != "counter") {
                                        onAction("Start")
                                    } else {
                                        onAction("Increment")
                                    }
                                    showMenu = false
                                }
                        )

                        // Stop Dot (🟡)
                        Box(
                            modifier = Modifier
                                .size(scaleFactor)
                                .clip(CircleShape)
                                .background(Color(0xFFF5A623))
                                .clickable {
                                    onAction("Stop")
                                    showMenu = false
                                }
                        )

                        // Reset Dot (🔵)
                        Box(
                            modifier = Modifier
                                .size(scaleFactor)
                                .clip(CircleShape)
                                .background(Color(0xFF4A90E2))
                                .clickable {
                                    onAction("Reset")
                                    showMenu = false
                                }
                        )

                        // Milestone Pin Dot (🟣)
                        Box(
                            modifier = Modifier
                                .size(scaleFactor)
                                .clip(CircleShape)
                                .background(Color(0xFF9013FE))
                                .clickable {
                                    onAction("Milestone")
                                    showMenu = false
                                }
                        )

                        // Volume Toggle Dot (🟠) - Only visible/active for counters
                        if (widgetType == "counter") {
                            Box(
                                modifier = Modifier
                                    .size(scaleFactor)
                                    .clip(CircleShape)
                                    .background(if (isVolumeActive) Color(0xFFFF9500) else Color(0xFF5856D6))
                                    .clickable {
                                        onAction("ToggleVolume")
                                        showMenu = false
                                    }
                            )
                        }

                        // Close Dot (🔴)
                        Box(
                            modifier = Modifier
                                .size(scaleFactor)
                                .clip(CircleShape)
                                .background(Color(0xFFC94A4A))
                                .clickable {
                                    serviceScope.launch {
                                        settingsRepository.setWidgetActive(index, false)
                                    }
                                }
                        )
                    }
                } else {
                    // Foreground Values (Stopwatch / Countdown / Tap Counter)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (showBatteryIndicator && shapePreset != "circle") {
                            val scaledIconSize = (16.dp.value * fontSizeScale).coerceAtLeast(10.0f).dp
                            val scaledFontSize = (8.sp.value * fontSizeScale).coerceAtLeast(6.0f).sp
                            Box(
                                modifier = Modifier
                                    .size(scaledIconSize)
                                    .border(
                                        border = BorderStroke(1.dp, if (running) Color(0xFF4AC98F) else Color(0xFFC94A4A)),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$batteryPercentage%",
                                    color = if (running) Color(0xFF4AC98F) else Color(0xFFC94A4A),
                                    fontSize = scaledFontSize,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (widgetType == "counter") {
                            // Render pure touch-based counts numbers
                            Text(
                                text = "TAP: $tapCount",
                                style = TextStyle(
                                    color = if (isVolumeActive) Color(0xFFFF9500) else LuxuryColors.CreamyWhite,
                                    fontSize = (22.sp.value * fontSizeScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            if (isVolumeActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF9500))
                                )
                            }
                        } else {
                            TimeDisplay(
                                elapsedTimeMs = elapsedTimeMs,
                                showCentiseconds = showCentiseconds,
                                baseStyle = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 22.sp),
                                scaleFactor = fontSizeScale,
                                gradientGoldEnabled = gradientEnabled,
                                isVertical = layoutOrientation == "vertical",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun Int.dpToPx(): Int {
        val density = applicationContext.resources.displayMetrics.density
        val px = (this * density).toInt()
        return if (px < 0) 0 else px
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Stopwatch Overlay Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Floating Stopwatch & Focus Engine Active")
            .setContentText("Tap to return to Settings and manage active widgets.")
            .setSmallIcon(R.drawable.ic_stat_stopwatch)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        activeServices.remove(this)
        serviceScope.cancel()

        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) { e.printStackTrace() }

        // Secure unmount and clean up all spawned widget overlay life-cycles
        activeOverlays.keys.toList().forEach { dismissWidget(it) }
    }
}
