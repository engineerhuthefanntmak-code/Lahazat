package com.floating.stopwatch

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.floating.stopwatch.data.SettingsRepository
import com.floating.stopwatch.domain.HapticController
import com.floating.stopwatch.domain.StopwatchEngine
import com.floating.stopwatch.service.StopwatchService
import com.floating.stopwatch.ui.MainViewModel
import com.floating.stopwatch.ui.screens.MainScreen
import com.floating.stopwatch.ui.screens.SettingsScreen
import com.floating.stopwatch.ui.theme.LuxuryColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class MainActivity : androidx.fragment.app.FragmentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var mainViewModel: MainViewModel
    private lateinit var hapticController: HapticController

    // Foldable postures window tracker
    private var foldingFeatureState = mutableStateOf<FoldingFeature?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        settingsRepository = SettingsRepository(applicationContext)
        mainViewModel = MainViewModel(
            engine = StopwatchService.getEngine(),
            countdownEngine = StopwatchService.getCountdownEngine()
        )
        hapticController = HapticController(applicationContext)

        // Track fold/hinge updates with explicit safe fallbacks in case Jetpack WindowManager throws on traditional non-foldable devices
        lifecycleScope.launch {
            try {
                WindowInfoTracker.getOrCreate(this@MainActivity)
                    .windowLayoutInfo(this@MainActivity)
                    .collectLatest { layoutInfo ->
                        val folding = layoutInfo.displayFeatures
                            .filterIsInstance<FoldingFeature>()
                            .firstOrNull()
                        foldingFeatureState.value = folding
                    }
            } catch (e: Exception) {
                // Fallback gracefully: ignore exception and set folding to null (traditional posture layout)
                foldingFeatureState.value = null
            }
        }

        setContent {
            var currentScreen by remember { mutableStateOf("Main") }
            var isUnlockedByBiometrics by remember { mutableStateOf(false) }

            val colorPreset by settingsRepository.colorPreset.collectAsState(initial = "Gold")
            val customColorHex by settingsRepository.customColorHex.collectAsState(initial = "#C9A66B")
            val hapticIntensity by settingsRepository.hapticIntensity.collectAsState(initial = "Medium")
            val themeMode by settingsRepository.themeMode.collectAsState(initial = "Midnight")

            val accentColor = if (colorPreset == "Custom") {
                try { Color(android.graphics.Color.parseColor(customColorHex)) } catch (e: Exception) { LuxuryColors.AccentGold }
            } else {
                LuxuryColors.fromName(colorPreset)
            }

            // Standard permission verification state flow for Alert Overlay
            var hasOverlayPermission by remember {
                mutableStateOf(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Settings.canDrawOverlays(this@MainActivity)
                    } else {
                        true
                    }
                )
            }

            // Monitor state update correctly on resumed/activity context focus change
            DisposableEffect(Unit) {
                onResumeCallback = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        hasOverlayPermission = Settings.canDrawOverlays(this@MainActivity)
                    }
                }
                onDispose {
                    onResumeCallback = null
                }
            }

            LaunchedEffect(hasOverlayPermission) {
                if (hasOverlayPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this@MainActivity)) {
                    lifecycleScope.launch {
                        if (settingsRepository.hasAnyWidgetActive.first()) {
                            startFloatingService()
                        }
                    }
                }
            }

            // Handle safe Back press to return to MainScreen instead of exiting the entire application (Section 2 - Item 5)
            androidx.activity.compose.BackHandler(enabled = currentScreen == "Settings") {
                currentScreen = "Main"
            }

            if (!hasOverlayPermission) {
                OverlayPermissionExplanationScreen(
                    onGrantClick = {
                        requestOverlayPermission()
                    },
                    onSkipClick = {
                        hasOverlayPermission = true
                    }
                )
            } else {
                when (currentScreen) {
                    "Main" -> {
                        MainScreen(
                            viewModel = mainViewModel,
                            hapticController = hapticController,
                            hapticIntensity = hapticIntensity,
                            showCentiseconds = true,
                            mainSize = 1.0f,
                            accentColor = accentColor,
                            themeMode = themeMode,
                            onNavigateToSettings = { currentScreen = "Settings" }
                        )
                    }
                    "Settings" -> {
                        SettingsScreen(
                            settingsRepository = settingsRepository,
                            onBack = { currentScreen = "Main" }
                        )
                    }
                }
            }
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 1024)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1024) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    lifecycleScope.launch {
                        if (settingsRepository.hasAnyWidgetActive.first()) {
                            startFloatingService()
                        }
                    }
                }
            }
        }
    }

    private fun triggerBiometricAuthentication(activity: androidx.fragment.app.FragmentActivity, onAuthenticated: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthenticated()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Premium Biometric Lock")
            .setSubtitle("Authenticate to view premium stopwatch insights")
            .setNegativeButtonText("Cancel")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            // Safe fallback if biometric hardware is not set up / ready
            onAuthenticated()
        }
    }

    private fun startFloatingService() {
        // Double check canDrawOverlays before launching intent defensively
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            return
        }
        val intent = Intent(this, StopwatchService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    // Live update when activity is resumed
    private var onResumeCallback: (() -> Unit)? = null

    override fun onResume() {
        super.onResume()
        onResumeCallback?.invoke()
        // verify permission status live
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                lifecycleScope.launch {
                    if (settingsRepository.hasAnyWidgetActive.first()) {
                        startFloatingService()
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP) {
            if (StopwatchService.handleVolumePress(increment = true)) {
                return true
            }
        } else if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (StopwatchService.handleVolumePress(increment = false)) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun OverlayPermissionExplanationScreen(
    onGrantClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryColors.WarmBlack)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "FLOATING OVERLAY",
                style = TextStyle(
                    color = LuxuryColors.CreamyWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraLight,
                    letterSpacing = 4.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "The premium Luxury Minimal Stopwatch can float directly on top of your other active applications for real-time tracking. This requires the 'Display over other apps' system permission.",
                style = TextStyle(
                    color = LuxuryColors.WarmGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryColors.AccentGold),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "GRANT PERMISSION",
                    color = LuxuryColors.WarmBlack,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SKIP AND OPEN IN-APP TIMER",
                style = TextStyle(
                    color = LuxuryColors.WarmGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier
                    .clickable { onSkipClick() }
                    .padding(8.dp)
            )
        }
    }
}
