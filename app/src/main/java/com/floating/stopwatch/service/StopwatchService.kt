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
import com.floating.stopwatch.ui.components.EnergyAuraEffect
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

        fun handleVolumePress(increment: Boolean): Boolean {
            var handled = false
            activeServices.forEach { service ->
                service.widgetStates.forEach { ws ->
                    if (ws.type.value == "counter") {
                        if (increment) {
                            ws.tapCount.value++
                            val newVal = ws.tapCount.value
                            ws.elapsedOrValue.value = newVal.toLong()
                            service.serviceScope.launch {
                                service.settingsRepository.setWidgetValue(ws.index, newVal.toLong())
                            }
                            service.checkCounterMilestoneAndVibrate(newVal)
                            handled = true
                        } else if (ws.tapCount.value > 0) {
                            ws.tapCount.value--
                            val newVal = ws.tapCount.value
                            ws.elapsedOrValue.value = newVal.toLong()
                            service.serviceScope.launch {
                                service.settingsRepository.setWidgetValue(ws.index, newVal.toLong())
                            }
                            service.checkCounterMilestoneAndVibrate(newVal)
                            handled = true
                        }
                    }
                }
            }
            return handled
        }
    }

    private lateinit var windowManager: WindowManager
    private lateinit var settingsRepository: SettingsRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var hapticController: HapticController

    // Multi-Widget context structures
    private val activeOverlays = mutableMapOf<Int, ActiveOverlay>()
    private val widgetStates = List(3) { index -> WidgetState(index) }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }

        // Monitor and auto-sync active widgets list
        startWidgetLifecycleManager()

        // Screen-off volume button handler for counter
        serviceScope.launch {
            combine(
                settingsRepository.volumeCounterScreenOffEnabled,
                snapshotFlow { widgetStates.any { it.type.value == "counter" } },
                snapshotFlow { widgetStates.any { it.type.value == "counter" && it.isVolumeCounterActive.value } }
            ) { enabled, hasCounter, isVolActive -> (enabled || isVolActive) && hasCounter }.collectLatest { active ->
                setupMediaSessionForScreenOffVolume(active, true)
            }
        }

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
        // Observe settings for up to 3 fixed widgets and spawn/dismiss them reactively
        for (i in 0..2) {
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

                            if (running && type != "counter") {
                                widgetStates[i].baseTime.value = SystemClock.elapsedRealtime()
                                startTickerForWidget(i)
                            } else if (type == "counter") {
                                widgetStates[i].running.value = false
                                serviceScope.launch { settingsRepository.setWidgetRunning(i, false) }
                            }
                        }
                    } else {
                        dismissWidget(i)
                    }
                }
            }

            // Sync countdown duration changes dynamically
            serviceScope.launch {
                settingsRepository.getWidgetCountdownDuration(i).collectLatest { seconds ->
                    widgetStates[i].countdownDuration.value = seconds
                    if (widgetStates[i].type.value == "countdown" && !widgetStates[i].running.value) {
                        widgetStates[i].elapsedOrValue.value = seconds * 1000L
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

            // Sync width and height changes dynamically
            serviceScope.launch {
                combine(
                    settingsRepository.getWidgetWidth(i),
                    settingsRepository.getWidgetHeight(i)
                ) { w, h -> Pair(w, h) }.collectLatest { (wDp, hDp) ->
                    val overlay = activeOverlays[i]
                    if (overlay != null && overlay.composeView.isAttachedToWindow) {
                        val wPx = wDp.toInt().dpToPx().coerceAtLeast(1)
                        val hPx = hDp.toInt().dpToPx().coerceAtLeast(1)
                        if (overlay.params.width != wPx || overlay.params.height != hPx) {
                            overlay.params.width = wPx
                            overlay.params.height = hPx
                            try {
                                windowManager.updateViewLayout(overlay.composeView, overlay.params)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
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
                val energyAuraEnabled by settingsRepository.energyAuraEnabled.collectAsState(initial = true)
                val auraEffectType by settingsRepository.auraEffectType.collectAsState(initial = "Ribbons & Sparks")
                val layoutOrientation by settingsRepository.layoutOrientation.collectAsState(initial = "horizontal")
                val floatingPadding by settingsRepository.floatingPadding.collectAsState(initial = 6.0f)
                val floatingOpacity by settingsRepository.floatingOpacity.collectAsState(initial = 0.85f)
                val glowingBorder by settingsRepository.glowingBorder.collectAsState(initial = false)
                val statusIndicatorMode by settingsRepository.statusIndicatorMode.collectAsState(initial = "battery")
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
                    if (overlay != null && overlay.composeView.isAttachedToWindow) {
                        val wPx = floatingWidth.toInt().dpToPx().coerceAtLeast(1)
                        val hPx = floatingHeight.toInt().dpToPx().coerceAtLeast(1)
                        if (overlay.params.width != wPx || overlay.params.height != hPx) {
                            overlay.params.width = wPx
                            overlay.params.height = hPx
                            try {
                                windowManager.updateViewLayout(overlay.composeView, overlay.params)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
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
                        energyAuraEnabled = energyAuraEnabled,
                        auraEffectType = auraEffectType,
                        layoutOrientation = layoutOrientation,
                        paddingDpValue = floatingPadding,
                        opacity = floatingOpacity,
                        glowingBorder = glowingBorder,
                        statusIndicatorMode = statusIndicatorMode,
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
                                    state.tapCount.value++
                                    val newVal = state.tapCount.value
                                    state.elapsedOrValue.value = newVal.toLong()
                                    serviceScope.launch { settingsRepository.setWidgetValue(index, newVal.toLong()) }
                                    checkCounterMilestoneAndVibrate(newVal)
                                    if (newVal != 33 && newVal != 66 && newVal != 99 && newVal != 100) {
                                        hapticController.trigger(hapticIntensity, "Lap")
                                    }
                                }
                                "Decrement" -> {
                                    if (state.tapCount.value > 0) {
                                        state.tapCount.value--
                                        val newVal = state.tapCount.value
                                        state.elapsedOrValue.value = newVal.toLong()
                                        serviceScope.launch { settingsRepository.setWidgetValue(index, newVal.toLong()) }
                                        checkCounterMilestoneAndVibrate(newVal)
                                        if (newVal != 33 && newVal != 66 && newVal != 99 && newVal != 100) {
                                            hapticController.trigger(hapticIntensity, "Reset")
                                        }
                                    }
                                }
                                "ToggleVolume" -> {
                                    hapticController.trigger(hapticIntensity, "Lap")
                                    state.isVolumeCounterActive.value = !state.isVolumeCounterActive.value
                                }
                                "Milestone" -> {
                                    hapticController.trigger(hapticIntensity, "Lap")
                                    if (type == "stopwatch") {
                                        getEngine().lap()
                                    } else {
                                        val currentMilestone = if (type == "countdown") {
                                            "Focus Session remaining: " + formatDuration(elapsedOrValue)
                                        } else {
                                            "Time record: " + formatDuration(elapsedOrValue)
                                        }
                                        state.milestones.value = state.milestones.value + currentMilestone
                                    }
                                }
                                "OpenApp" -> {
                                    val intent = Intent(this@StopwatchService, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    }
                                    startActivity(intent)
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
            170.dpToPx().coerceAtLeast(1),
            56.dpToPx().coerceAtLeast(1),
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100 + index * 40
            y = 200 + index * 80
        }

        composeView.addOnAttachStateChangeListener(object : android.view.View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: android.view.View) {
                serviceScope.launch {
                    val wDp = settingsRepository.getWidgetWidth(index).first()
                    val hDp = settingsRepository.getWidgetHeight(index).first()
                    val overlay = activeOverlays[index]
                    if (overlay != null) {
                        val wPx = wDp.toInt().dpToPx().coerceAtLeast(1)
                        val hPx = hDp.toInt().dpToPx().coerceAtLeast(1)
                        if (overlay.params.width != wPx || overlay.params.height != hPx) {
                            overlay.params.width = wPx
                            overlay.params.height = hPx
                            try {
                                windowManager.updateViewLayout(overlay.composeView, overlay.params)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
            override fun onViewDetachedFromWindow(v: android.view.View) {}
        })

        activeOverlays[index] = ActiveOverlay(index, owner, composeView, params)
        windowManager.addView(composeView, params)
    }

    private var mediaSession: android.media.session.MediaSession? = null

    private fun setupMediaSessionForScreenOffVolume(enabled: Boolean, hasActiveCounter: Boolean) {
        if (enabled && hasActiveCounter) {
            if (mediaSession == null) {
                mediaSession = android.media.session.MediaSession(this, "StopwatchVolumeCounterSession").apply {
                    val volumeProvider = object : android.media.VolumeProvider(
                        android.media.VolumeProvider.VOLUME_CONTROL_RELATIVE,
                        100,
                        50
                    ) {
                        override fun onAdjustVolume(direction: Int) {
                            if (direction > 0) {
                                handleCounterVolumePress(increment = true)
                            } else if (direction < 0) {
                                handleCounterVolumePress(increment = false)
                            }
                        }
                    }
                    setPlaybackToRemote(volumeProvider)
                    setCallback(object : android.media.session.MediaSession.Callback() {
                        override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                            val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, android.view.KeyEvent::class.java)
                            } else {
                                @Suppress("DEPRECATION")
                                mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                            }
                            if (keyEvent != null && keyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                                if (keyEvent.keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP) {
                                    handleCounterVolumePress(increment = true)
                                    return true
                                } else if (keyEvent.keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN) {
                                    handleCounterVolumePress(increment = false)
                                    return true
                                }
                            }
                            return super.onMediaButtonEvent(mediaButtonIntent)
                        }
                    })
                }
            }
            val playbackState = android.media.session.PlaybackState.Builder()
                .setState(android.media.session.PlaybackState.STATE_PLAYING, 0L, 1.0f)
                .setActions(android.media.session.PlaybackState.ACTION_PLAY)
                .build()
            mediaSession?.setPlaybackState(playbackState)
            mediaSession?.isActive = true
        } else {
            mediaSession?.isActive = false
        }
    }

    private fun checkCounterMilestoneAndVibrate(newValue: Int) {
        if (newValue == 33 || newValue == 66 || newValue == 99 || newValue == 100) {
            serviceScope.launch {
                val intensity = settingsRepository.hapticIntensity.first()
                hapticController.trigger(intensity, "CounterMilestone")
            }
        }
    }

    private fun handleCounterVolumePress(increment: Boolean) {
        widgetStates.forEach { ws ->
            if (ws.type.value == "counter") {
                if (increment) {
                    ws.tapCount.value++
                    val newVal = ws.tapCount.value
                    ws.elapsedOrValue.value = newVal.toLong()
                    serviceScope.launch { settingsRepository.setWidgetValue(ws.index, newVal.toLong()) }
                    checkCounterMilestoneAndVibrate(newVal)
                } else if (ws.tapCount.value > 0) {
                    ws.tapCount.value--
                    val newVal = ws.tapCount.value
                    ws.elapsedOrValue.value = newVal.toLong()
                    serviceScope.launch { settingsRepository.setWidgetValue(ws.index, newVal.toLong()) }
                    checkCounterMilestoneAndVibrate(newVal)
                }
            }
        }
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
        if (state.type.value == "counter") return
        tickerJobs[index] = serviceScope.launch(Dispatchers.Default) {
            var lastSavedSec = 0L
            while (true) {
                if (state.running.value) {
                    if (state.type.value == "stopwatch") {
                        val elapsed = state.elapsedOrValue.value + (SystemClock.elapsedRealtime() - state.baseTime.value)
                        state.elapsedOrValue.value = elapsed
                        state.baseTime.value = SystemClock.elapsedRealtime()
                        val sec = elapsed / 1000
                        if (sec != lastSavedSec) {
                            lastSavedSec = sec
                            settingsRepository.setWidgetValue(index, elapsed)
                        }
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
                            val sec = currentRemaining / 1000
                            if (sec != lastSavedSec) {
                                lastSavedSec = sec
                                settingsRepository.setWidgetValue(index, currentRemaining)
                            }
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
        energyAuraEnabled: Boolean,
        auraEffectType: String,
        layoutOrientation: String,
        paddingDpValue: Float,
        opacity: Float,
        glowingBorder: Boolean,
        statusIndicatorMode: String,
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

        val auraPadding = if (energyAuraEnabled) 12.dp else 0.dp

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (energyAuraEnabled) {
                EnergyAuraEffect(
                    isRunning = running,
                    effectType = auraEffectType,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(auraPadding)
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
                    LuxuryTextDropdownMenu(
                        widgetType = widgetType,
                        fontSizeScale = fontSizeScale,
                        isVolumeActive = isVolumeActive,
                        onAction = { action ->
                            if (action == "Close") {
                                serviceScope.launch {
                                    settingsRepository.setWidgetActive(index, false)
                                }
                            } else {
                                onAction(action)
                            }
                        },
                        onDismiss = { showMenu = false }
                    )
                } else {
                    // Foreground Values (Stopwatch / Countdown / Tap Counter)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (statusIndicatorMode != "hidden" && shapePreset != "circle") {
                            val scaledIconSize = (16.dp.value * fontSizeScale).coerceAtLeast(10.0f).dp
                            val scaledFontSize = (8.sp.value * fontSizeScale).coerceAtLeast(6.0f).sp

                            if (statusIndicatorMode == "battery") {
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
                            } else if (statusIndicatorMode == "book") {
                                Text(
                                    text = "📖",
                                    fontSize = scaledFontSize * 1.5f
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

@Composable
fun LuxuryTextDropdownMenu(
    widgetType: String,
    fontSizeScale: Float,
    isVolumeActive: Boolean,
    onAction: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scaleFontSize = (11.sp.value * fontSizeScale).coerceAtLeast(10.0f).sp

    androidx.compose.ui.window.Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xF20A0A0A)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF2C2C2E)),
            modifier = Modifier
                .wrapContentSize()
                .padding(2.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.Start
            ) {
                val options = mutableListOf<Pair<String, String>>()
                if (widgetType == "counter") {
                    options.add("INCREMENT" to "Increment")
                    options.add("DECREMENT" to "Decrement")
                    options.add((if (isVolumeActive) "DISABLE VOLUME KEYS" else "ENABLE VOLUME KEYS") to "ToggleVolume")
                } else {
                    options.add("START" to "Start")
                    options.add("STOP" to "Stop")
                    options.add("RESET" to "Reset")
                    options.add("MILESTONE" to "Milestone")
                }
                options.add("SETTINGS" to "OpenApp")
                options.add("HIDE" to "Hide")
                options.add("CLOSE" to "Close")

                options.forEach { (label, action) ->
                    Text(
                        text = label,
                        style = TextStyle(
                            color = when (label) {
                                "START", "INCREMENT" -> Color(0xFF4AC98F)
                                "STOP" -> Color(0xFFF5A623)
                                "CLOSE" -> Color(0xFFC94A4A)
                                else -> LuxuryColors.CreamyWhite
                            },
                            fontSize = scaleFontSize,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (action == "Hide") {
                                    onDismiss()
                                } else {
                                    onAction(action)
                                    onDismiss()
                                }
                            }
                            .padding(vertical = 3.dp, horizontal = 6.dp)
                    )
                }
            }
        }
    }
}
