package com.floating.stopwatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.data.SettingsRepository
import com.floating.stopwatch.ui.theme.LuxuryColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val mainSize by settingsRepository.mainSize.collectAsState(initial = 1.0f)
    val floatingSize by settingsRepository.floatingSize.collectAsState(initial = 0.5f)
    val showCentisecondsMain by settingsRepository.showCentisecondsMain.collectAsState(initial = true)
    val showCentisecondsFloating by settingsRepository.showCentisecondsFloating.collectAsState(initial = true)
    val stylePreset by settingsRepository.stylePreset.collectAsState(initial = "Glass Premium")
    val colorPreset by settingsRepository.colorPreset.collectAsState(initial = "Gold")
    val customColorHex by settingsRepository.customColorHex.collectAsState(initial = "#C9A66B")
    val experienceLevel by settingsRepository.experienceLevel.collectAsState(initial = "Premium")
    val hapticIntensity by settingsRepository.hapticIntensity.collectAsState(initial = "Medium")
    val themeMode by settingsRepository.themeMode.collectAsState(initial = "Midnight")
    val biometricLock by settingsRepository.biometricLock.collectAsState(initial = false)

    // Custom RGB/HSV helper state for the color preset configuration
    var customR by remember { mutableFloatStateOf(201f) }
    var customG by remember { mutableFloatStateOf(166f) }
    var customB by remember { mutableFloatStateOf(107f) }

    fun updateCustomColor() {
        val hex = String.format("#%02X%02X%02X", customR.toInt(), customG.toInt(), customB.toInt())
        scope.launch {
            settingsRepository.setCustomColorHex(hex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SETTINGS",
                        style = TextStyle(
                            color = LuxuryColors.CreamyWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 3.sp
                        )
                    )
                },
                navigationIcon = {
                    Text(
                        text = "BACK",
                        style = TextStyle(
                            color = LuxuryColors.WarmGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier
                            .clickable { onBack() }
                            .padding(16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuxuryColors.WarmBlack
                )
            )
        },
        containerColor = LuxuryColors.WarmBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Main size Slider (0.0 to 1.0)
            Text(
                text = "MAIN TIME SIZE SCALE",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp)
            )
            Slider(
                value = mainSize,
                onValueChange = { scope.launch { settingsRepository.setMainSize(it) } },
                valueRange = 0.5f..1.5f,
                colors = SliderDefaults.colors(
                    thumbColor = LuxuryColors.AccentGold,
                    activeTrackColor = LuxuryColors.AccentGold
                )
            )
            Text(
                text = "Value: ${String.format("%.2f", mainSize)}",
                style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 12.sp),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Floating Width Slider (120 to 280 dp) - Independent control
            val floatingWidth by settingsRepository.floatingWidth.collectAsState(initial = 170.0f)
            val floatingHeight by settingsRepository.floatingHeight.collectAsState(initial = 56.0f)

            Text(
                text = "FLOATING OVERLAY WIDTH",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp)
            )
            Slider(
                value = floatingWidth,
                onValueChange = { scope.launch { settingsRepository.setFloatingWidth(it) } },
                valueRange = 120.0f..280.0f,
                colors = SliderDefaults.colors(
                    thumbColor = LuxuryColors.AccentGold,
                    activeTrackColor = LuxuryColors.AccentGold
                )
            )
            Text(
                text = "Width: ${floatingWidth.toInt()}dp (Limits: 120dp to 280dp)",
                style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 12.sp),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Floating Height Slider (48 to 80 dp) - Independent control
            Text(
                text = "FLOATING OVERLAY HEIGHT",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp)
            )
            Slider(
                value = floatingHeight,
                onValueChange = { scope.launch { settingsRepository.setFloatingHeight(it) } },
                valueRange = 48.0f..80.0f,
                colors = SliderDefaults.colors(
                    thumbColor = LuxuryColors.AccentGold,
                    activeTrackColor = LuxuryColors.AccentGold
                )
            )
            Text(
                text = "Height: ${floatingHeight.toInt()}dp (Limits: 48dp to 80dp)",
                style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 12.sp),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Show Centiseconds Toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SHOW CENTISECONDS (MAIN SCREEN)",
                    style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 13.sp)
                )
                Switch(
                    checked = showCentisecondsMain,
                    onCheckedChange = { scope.launch { settingsRepository.setShowCentisecondsMain(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = LuxuryColors.AccentGold)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BIOMETRIC APP PRIVACY LOCK",
                    style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 13.sp)
                )
                Switch(
                    checked = biometricLock,
                    onCheckedChange = { scope.launch { settingsRepository.setBiometricLock(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = LuxuryColors.AccentGold)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SHOW CENTISECONDS (FLOATING)",
                    style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 13.sp)
                )
                Switch(
                    checked = showCentisecondsFloating,
                    onCheckedChange = { scope.launch { settingsRepository.setShowCentisecondsFloating(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = LuxuryColors.AccentGold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Theme Mode Setting (Midnight / Warm Paper / Obsidian Dark)
            Text(
                text = "ILLUMINATION STYLE MODE",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val themeModes = listOf("Midnight", "Warm Paper", "Obsidian Dark")
            themeModes.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { scope.launch { settingsRepository.setThemeMode(mode) } }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (themeMode == mode),
                        onClick = { scope.launch { settingsRepository.setThemeMode(mode) } },
                        colors = RadioButtonDefaults.colors(selectedColor = LuxuryColors.AccentGold)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = mode, color = LuxuryColors.CreamyWhite, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Style Preset Choices
            Text(
                text = "STYLE PRESET",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val presets = listOf("Glass Premium", "Obsidian", "Titanium", "Ultra Minimal")
            presets.forEach { preset ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { scope.launch { settingsRepository.setStylePreset(preset) } }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (stylePreset == preset),
                        onClick = { scope.launch { settingsRepository.setStylePreset(preset) } },
                        colors = RadioButtonDefaults.colors(selectedColor = LuxuryColors.AccentGold)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = preset, color = LuxuryColors.CreamyWhite, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Experience Level Setting
            Text(
                text = "FLOATING EXPERIENCE LEVEL",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val experienceLevels = listOf("Ultra Minimal", "Premium", "Full Control")
            experienceLevels.forEach { level ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { scope.launch { settingsRepository.setExperienceLevel(level) } }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (experienceLevel == level),
                        onClick = { scope.launch { settingsRepository.setExperienceLevel(level) } },
                        colors = RadioButtonDefaults.colors(selectedColor = LuxuryColors.AccentGold)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = level, color = LuxuryColors.CreamyWhite, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Haptic Intensity Settings
            Text(
                text = "HAPTIC FEEDBACK INTENSITY",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val intensities = listOf("Off", "Light", "Medium", "Strong")
            intensities.forEach { intensity ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { scope.launch { settingsRepository.setHapticIntensity(intensity) } }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (hapticIntensity == intensity),
                        onClick = { scope.launch { settingsRepository.setHapticIntensity(intensity) } },
                        colors = RadioButtonDefaults.colors(selectedColor = LuxuryColors.AccentGold)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = intensity, color = LuxuryColors.CreamyWhite, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Preset Setting
            Text(
                text = "COLOR PRESET",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val colorPresets = listOf("Gold", "Galaxy Blue", "Titanium", "Emerald", "Sapphire", "Violet", "Rose", "Ice", "Amber", "Pure White", "Custom")
            colorPresets.forEach { color ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { scope.launch { settingsRepository.setColorPreset(color) } }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (colorPreset == color),
                        onClick = { scope.launch { settingsRepository.setColorPreset(color) } },
                        colors = RadioButtonDefaults.colors(selectedColor = LuxuryColors.AccentGold)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = color, color = LuxuryColors.CreamyWhite, fontSize = 14.sp)
                }
            }

            // Custom Color HSV / RGB Slider Picker if "Custom" is selected
            if (colorPreset == "Custom") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "FINE CUSTOM TUNE RGB SLIDERS",
                    style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 1.sp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "R: ${customR.toInt()}", color = Color.Red, fontSize = 12.sp)
                Slider(
                    value = customR,
                    onValueChange = { customR = it; updateCustomColor() },
                    valueRange = 0f..255f
                )

                Text(text = "G: ${customG.toInt()}", color = Color.Green, fontSize = 12.sp)
                Slider(
                    value = customG,
                    onValueChange = { customG = it; updateCustomColor() },
                    valueRange = 0f..255f
                )

                Text(text = "B: ${customB.toInt()}", color = Color.Blue, fontSize = 12.sp)
                Slider(
                    value = customB,
                    onValueChange = { customB = it; updateCustomColor() },
                    valueRange = 0f..255f
                )

                // Contrast checks warning details
                // Contrast formula: L = 0.2126 * R + 0.7152 * G + 0.0722 * B
                val relativeLuminance = (0.2126f * (customR/255) + 0.7152f * (customG/255) + 0.0722f * (customB/255))
                if (relativeLuminance < 0.2f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "WARNING: Custom color has extremely low contrast against Obsidian background presets. Ensure minimum readability.",
                        color = Color(0xFFC94A4A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "CONTRAST MATCH: Warm Obsidian & Minimal backgrounds compatible.",
                        color = Color(0xFF4AC98F),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
