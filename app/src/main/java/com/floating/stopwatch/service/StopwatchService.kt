package com.floating.stopwatch.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.floating.stopwatch.MainActivity
import com.floating.stopwatch.R
import com.floating.stopwatch.data.SettingsRepository
import com.floating.stopwatch.domain.StopwatchEngine
import com.floating.stopwatch.domain.StopwatchState
import com.floating.stopwatch.ui.components.TimeDisplay
import com.floating.stopwatch.ui.theme.LuxuryColors
import com.floating.stopwatch.domain.HapticController
import kotlinx.coroutines.*
import kotlin.math.roundToInt
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class StopwatchService : Service() {

    companion object {
        const val CHANNEL_ID = "StopwatchOverlayChannel"
        const val NOTIFICATION_ID = 4842
        private var sharedEngine: StopwatchEngine? = null

        fun getEngine(): StopwatchEngine {
            if (sharedEngine == null) {
                sharedEngine = StopwatchEngine()
            }
            return sharedEngine!!
        }
    }

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private var params: WindowManager.LayoutParams? = null

    private lateinit var settingsRepository: SettingsRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var hapticController: HapticController
    private var overlayLifecycleOwner: ComposeOverlayLifecycleOwner? = null

    override fun onCreate() {
        super.onCreate()

        // Secure safety check: If overlay permission is not granted, abort immediately to prevent BadTokenException crash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        settingsRepository = SettingsRepository(applicationContext)
        hapticController = HapticController(applicationContext)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        initOverlayWindow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun initOverlayWindow() {
        val owner = ComposeOverlayLifecycleOwner().apply {
            onCreate()
            onStart()
            onResume()
        }
        overlayLifecycleOwner = owner

        composeView = ComposeView(this).apply {
            // Bind manually configured Lifecycle, ViewModelStore and SavedStateRegistry owners defensively
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)

            setContent {
                val engine = getEngine()
                val state by engine.state.collectAsState()
                val elapsedTimeMs by engine.elapsedTimeMs.collectAsState()
                val laps by engine.laps.collectAsState()

                val mainSize by settingsRepository.mainSize.collectAsState(initial = 1.0f)
                val floatingSize by settingsRepository.floatingSize.collectAsState(initial = 0.5f)
                val floatingWidth by settingsRepository.floatingWidth.collectAsState(initial = 170.0f)
                val floatingHeight by settingsRepository.floatingHeight.collectAsState(initial = 56.0f)
                val showCentisecondsFloating by settingsRepository.showCentisecondsFloating.collectAsState(initial = true)
                val stylePreset by settingsRepository.stylePreset.collectAsState(initial = "Glass Premium")
                val colorPreset by settingsRepository.colorPreset.collectAsState(initial = "Gold")
                val customColorHex by settingsRepository.customColorHex.collectAsState(initial = "#C9A66B")
                val experienceLevel by settingsRepository.experienceLevel.collectAsState(initial = "Premium")
                val hapticIntensity by settingsRepository.hapticIntensity.collectAsState(initial = "Medium")

                val shapePreset by settingsRepository.shapePreset.collectAsState(initial = "rounded")
                val fontSizeScale by settingsRepository.fontSizeScale.collectAsState(initial = 1.0f)
                val gradientEnabled by settingsRepository.gradientEnabled.collectAsState(initial = false)
                val layoutOrientation by settingsRepository.layoutOrientation.collectAsState(initial = "horizontal")
                val floatingPadding by settingsRepository.floatingPadding.collectAsState(initial = 6.0f)

                val accentColor = if (colorPreset == "Custom") {
                    try { Color(android.graphics.Color.parseColor(customColorHex)) } catch (e: Exception) { LuxuryColors.AccentGold }
                } else {
                    LuxuryColors.fromName(colorPreset)
                }

                // Window position settings
                val initialX by settingsRepository.floatingX.collectAsState(initial = -1.0f)
                val initialY by settingsRepository.floatingY.collectAsState(initial = -1.0f)

                LaunchedEffect(initialX, initialY) {
                    if (initialX != -1.0f && initialY != -1.0f && params != null) {
                        params?.x = initialX.roundToInt()
                        params?.y = initialY.roundToInt()
                        windowManager.updateViewLayout(composeView, params)
                    }
                }

                LaunchedEffect(floatingWidth, floatingHeight) {
                    if (params != null) {
                        val w = if (floatingWidth < 1f) 1 else floatingWidth.toInt()
                        val h = if (floatingHeight < 1f) 1 else floatingHeight.toInt()
                        params?.width = w.dpToPx()
                        params?.height = h.dpToPx()
                        windowManager.updateViewLayout(composeView, params)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Actual themed Container
                    ThemedOverlayContainer(
                        state = state,
                        elapsedTimeMs = elapsedTimeMs,
                        lapsCount = laps.size,
                        showCentiseconds = showCentisecondsFloating,
                        stylePreset = stylePreset,
                        accentColor = accentColor,
                        experienceLevel = experienceLevel,
                        size = floatingSize,
                        shapePreset = shapePreset,
                        fontSizeScale = fontSizeScale,
                        gradientEnabled = gradientEnabled,
                        layoutOrientation = layoutOrientation,
                        paddingDpValue = floatingPadding,
                        onMovementDrag = { dx, dy ->
                            params?.let {
                                it.x += dx.roundToInt()
                                it.y += dy.roundToInt()
                                windowManager.updateViewLayout(composeView, it)
                            }
                        },
                        onMovementRelease = {
                            params?.let {
                                smartEdgeSnapAndClamp(it)
                                serviceScope.launch {
                                    settingsRepository.setFloatingPosition(it.x.toFloat(), it.y.toFloat())
                                }
                            }
                        },
                        onAction = { action ->
                            when (action) {
                                "Start" -> {
                                    hapticController.trigger(hapticIntensity, "Start")
                                    engine.start()
                                }
                                "Stop" -> {
                                    hapticController.trigger(hapticIntensity, "Stop")
                                    engine.pause()
                                }
                                "Reset" -> {
                                    hapticController.trigger(hapticIntensity, "Reset")
                                    engine.reset()
                                }
                                "Lap" -> {
                                    hapticController.trigger(hapticIntensity, "Lap")
                                    engine.lap()
                                }
                            }
                        }
                    )
                }
            }
        }

        // Layout parameters using specific dimension bounds mapping to 170dp x 56dp (P2 - secure layout without clipping)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Initialize with values fetched from settings or fallback defaults
        val initialWidth = 170

        params = WindowManager.LayoutParams(
            initialWidth.dpToPx(),
            56.dpToPx(),
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        windowManager.addView(composeView, params)
    }

    private fun smartEdgeSnapAndClamp(lp: WindowManager.LayoutParams) {
        val metrics = applicationContext.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        val margin = 24.dpToPx()
        val snapThreshold = 48.dpToPx()

        val leftX = margin
        val rightX = screenWidth - lp.width - margin
        val topY = margin
        val bottomY = screenHeight - lp.height - margin

        // Clamp to screen boundaries
        if (lp.x < leftX) lp.x = leftX
        if (lp.x > rightX) lp.x = rightX
        if (lp.y < topY) lp.y = topY
        if (lp.y > bottomY) lp.y = bottomY

        // Edge snap logic
        if (lp.x - leftX < snapThreshold) {
            lp.x = leftX
        } else if (rightX - lp.x < snapThreshold) {
            lp.x = rightX
        }

        if (lp.y - topY < snapThreshold) {
            lp.y = topY
        } else if (bottomY - lp.y < snapThreshold) {
            lp.y = bottomY
        }

        windowManager.updateViewLayout(composeView, lp)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Device rotation: keep in-bounds
        params?.let {
            smartEdgeSnapAndClamp(it)
        }
    }

    @Composable
    fun ThemedOverlayContainer(
        state: StopwatchState,
        elapsedTimeMs: Long,
        lapsCount: Int,
        showCentiseconds: Boolean,
        stylePreset: String,
        accentColor: Color,
        experienceLevel: String,
        size: Float,
        shapePreset: String,
        fontSizeScale: Float,
        gradientEnabled: Boolean,
        layoutOrientation: String,
        paddingDpValue: Float,
        onMovementDrag: (Float, Float) -> Unit,
        onMovementRelease: () -> Unit,
        onAction: (String) -> Unit
    ) {
        // Shapes presets customizations mapping (Item 3, 4)
        val finalCornerRadius = when (shapePreset) {
            "capsule" -> 32.dp
            "circle" -> 99.dp
            "sharp" -> 0.dp
            else -> 16.dp // standard rounded
        }

        // Clip-prevention safety padding: curved shapes like circular, capsule, glass require minimum offset boundaries to protect text elements.
        val shapeDependentMinPadding = when (shapePreset) {
            "circle", "capsule", "glass" -> 4.0f
            else -> 1.0f
        }
        val safePadding = paddingDpValue.coerceAtLeast(shapeDependentMinPadding)

        var showMenu by remember { mutableStateOf(false) }

        // Layout with layered backdrop to fix Glassmorphism blur (Item 4)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    showMenu = !showMenu
                    android.util.Log.d("StopwatchApp", "Overlay click registered! showMenu state changed to: $showMenu")
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = { onMovementRelease() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onMovementDrag(dragAmount.x, dragAmount.y)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Layer 1: Dedicated Background Layer alone with Blur/Glow applied to protect foreground digits (Item 4)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (stylePreset == "Glass Premium" || shapePreset == "glass") {
                            Modifier
                                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(finalCornerRadius))
                                .blur(16.dp)
                        } else if (stylePreset == "Obsidian") {
                            Modifier.background(Color(0xFF0A0A0A).copy(alpha = 0.88f), RoundedCornerShape(finalCornerRadius))
                        } else if (stylePreset == "Titanium") {
                            val titaniumBrush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF2C2F33), Color(0xFF1E2124))
                            )
                            Modifier.background(titaniumBrush, RoundedCornerShape(finalCornerRadius))
                        } else {
                            Modifier.background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(finalCornerRadius))
                        }
                    )
            )

            // Layer 2: Foreground Layer containing text/menus (Always crisp & sharp 100%)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(safePadding.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showMenu) {
                    // Luxury Minimalist Menu with clean deep dark fallback (No bright red diagnostic backdrop)
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.95f), RoundedCornerShape(finalCornerRadius))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Close Action
                        Text(
                            text = "CLOSE",
                            color = Color(0xFFC94A4A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    onAction("Stop")
                                    stopSelf()
                                }
                                .padding(4.dp)
                        )

                        // Reset Action
                        Text(
                            text = "RESET",
                            color = LuxuryColors.CreamyWhite,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable {
                                    onAction("Reset")
                                    showMenu = false
                                }
                                .padding(4.dp)
                        )

                        // Start / Pause
                        Text(
                            text = if (state == StopwatchState.Running) "PAUSE" else "START",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    onAction(if (state == StopwatchState.Running) "Stop" else "Start")
                                    showMenu = false
                                }
                                .padding(4.dp)
                        )

                        // Settings / Return to App Launcher
                        Text(
                            text = "APP",
                            color = LuxuryColors.WarmGray,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable {
                                    val launchIntent = Intent(this@StopwatchService, MainActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    startActivity(launchIntent)
                                    showMenu = false
                                }
                                .padding(4.dp)
                        )
                    }
                } else {
                    // Normal Minimalist View (RTL support, Status Indicators and crisp text bounds with vertical/horizontal mapping)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // State Indicator Dot (Only shown if shape is not circle/capsule tight boundaries)
                        if (shapePreset != "circle") {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (state == StopwatchState.Running) Color(0xFF4AC98F) else Color(0xFFC94A4A))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Stopwatch main display text (with explicit orientation checks - Section 2 - Item 6 & 10)
                        TimeDisplay(
                            elapsedTimeMs = elapsedTimeMs,
                            showCentiseconds = showCentiseconds,
                            baseStyle = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 22.sp),
                            scaleFactor = fontSizeScale, // completely decoupled scale control (Item 6)
                            gradientGoldEnabled = gradientEnabled, // gradient gold check (Item 5)
                            isVertical = layoutOrientation == "vertical", // vertical orientation check (Item 10)
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    private fun Int.dpToPx(): Int {
        val density = applicationContext.resources.displayMetrics.density
        // Ensure minimum pixel size is 1 to prevent WindowManager crashing with invalid/non-positive bounds
        val px = (this * density).toInt()
        return if (px < 1) 1 else px
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
            .setContentTitle("Floating Stopwatch Active")
            .setContentText("Tap to return to Main screen.")
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
        serviceScope.cancel()

        // Destruct and clear overlay lifecycle state
        overlayLifecycleOwner?.apply {
            onPause()
            onStop()
            onDestroy()
        }
        overlayLifecycleOwner = null

        composeView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
