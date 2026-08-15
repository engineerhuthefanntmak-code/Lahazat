package com.floating.stopwatch.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

    val stylePreset by settingsRepository.stylePreset.collectAsState(initial = "Glass Premium")
    val colorPreset by settingsRepository.colorPreset.collectAsState(initial = "Gold")
    val customColorHex by settingsRepository.customColorHex.collectAsState(initial = "#C9A66B")
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

    // Shape presets, styling and widgets
    val shapes = listOf("rounded", "capsule", "circle", "sharp", "glass")
    val themeModes = listOf("Midnight", "Warm Paper", "Obsidian Dark")
    val presets = listOf("Glass Premium", "Obsidian", "Titanium", "Ultra Minimal")
    val intensities = listOf("Off", "Light", "Medium", "Strong")
    val colorPresets = listOf("Gold", "Galaxy Blue", "Titanium", "Emerald", "Sapphire", "Violet", "Rose", "Ice", "Amber", "Pure White", "Custom")

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
            // ------------------------------------------------------------
            // MULTI-WIDGET MANAGER & INDIVIDUAL SIZES
            // ------------------------------------------------------------
            Text(
                text = "ACTIVE FLOATING WIDGETS MANAGER",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            for (i in 0..2) {
                val isWidgetActive by settingsRepository.isWidgetActive(i).collectAsState(initial = i == 0)
                val widgetType by settingsRepository.getWidgetType(i).collectAsState(initial = when (i) { 1 -> "countdown"; 2 -> "counter"; else -> "stopwatch" })
                val countdownDuration by settingsRepository.getWidgetCountdownDuration(i).collectAsState(initial = 300)
                val wWidth by settingsRepository.getWidgetWidth(i).collectAsState(initial = 170.0f)
                val wHeight by settingsRepository.getWidgetHeight(i).collectAsState(initial = 56.0f)

                val defaultTitle = when (i) {
                    0 -> "STOPWATCH"
                    1 -> "COUNTDOWN"
                    else -> "COUNTER"
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = LuxuryColors.WarmGray.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$defaultTitle WIDGET",
                                color = LuxuryColors.CreamyWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(
                                checked = isWidgetActive,
                                onCheckedChange = { scope.launch { settingsRepository.setWidgetActive(i, it) } },
                                colors = SwitchDefaults.colors(checkedThumbColor = LuxuryColors.AccentGold)
                            )
                        }

                        if (isWidgetActive) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Mode selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf("stopwatch", "countdown", "counter").forEach { type ->
                                    Box(
                                        modifier = Modifier
                                            .border(
                                                width = 1.dp,
                                                color = if (widgetType == type) LuxuryColors.AccentGold else Color.Gray.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                scope.launch {
                                                    settingsRepository.setWidgetType(i, type)
                                                    if (type == "countdown") {
                                                        settingsRepository.setWidgetValue(i, countdownDuration * 1000L)
                                                    }
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = type.uppercase(),
                                            color = if (widgetType == type) LuxuryColors.AccentGold else LuxuryColors.WarmGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            if (widgetType == "countdown") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "CUSTOM FOCUS DURATION (HOURS : MINS : SECS)",
                                    color = LuxuryColors.WarmGray,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CountdownDurationPicker(
                                    totalSeconds = countdownDuration,
                                    onDurationChanged = { newSecs ->
                                        scope.launch {
                                            settingsRepository.setWidgetCountdownDuration(i, newSecs)
                                            settingsRepository.setWidgetValue(i, newSecs * 1000L)
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "INDIVIDUAL WIDTH: ${wWidth.toInt()}dp",
                                color = LuxuryColors.WarmGray,
                                fontSize = 10.sp
                            )
                            Slider(
                                value = wWidth,
                                onValueChange = { scope.launch { settingsRepository.setWidgetWidth(i, it) } },
                                valueRange = 1.0f..320.0f,
                                colors = SliderDefaults.colors(thumbColor = LuxuryColors.AccentGold, activeTrackColor = LuxuryColors.AccentGold)
                            )

                            Text(
                                text = "INDIVIDUAL HEIGHT: ${wHeight.toInt()}dp",
                                color = LuxuryColors.WarmGray,
                                fontSize = 10.sp
                            )
                            Slider(
                                value = wHeight,
                                onValueChange = { scope.launch { settingsRepository.setWidgetHeight(i, it) } },
                                valueRange = 1.0f..120.0f,
                                colors = SliderDefaults.colors(thumbColor = LuxuryColors.AccentGold, activeTrackColor = LuxuryColors.AccentGold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Floating Padding Slider (0 to 32 dp)
            val shapePreset by settingsRepository.shapePreset.collectAsState(initial = "rounded")
            val floatingPadding by settingsRepository.floatingPadding.collectAsState(initial = 6.0f)

            Text(
                text = "FLOATING OVERLAY INTERNAL PADDING",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp)
            )
            Slider(
                value = floatingPadding,
                onValueChange = {
                    scope.launch {
                        settingsRepository.setFloatingPadding(it)
                    }
                },
                valueRange = 0.0f..32.0f,
                colors = SliderDefaults.colors(
                    thumbColor = LuxuryColors.AccentGold,
                    activeTrackColor = LuxuryColors.AccentGold
                )
            )
            Text(
                text = "Padding: ${floatingPadding.toInt()}dp (Limits: 0dp to 32dp)",
                style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 12.sp),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Backdrop Opacity Slider
            val floatingOpacity by settingsRepository.floatingOpacity.collectAsState(initial = 0.85f)
            Text(
                text = "FLOATING BACKDROP OPACITY",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp)
            )
            Slider(
                value = floatingOpacity,
                onValueChange = {
                    scope.launch {
                        settingsRepository.setFloatingOpacity(it)
                    }
                },
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = LuxuryColors.AccentGold,
                    activeTrackColor = LuxuryColors.AccentGold
                )
            )
            Text(
                text = "Opacity: ${(floatingOpacity * 100).toInt()}%",
                style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 12.sp),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Glowing Border Toggle
            val glowingBorder by settingsRepository.glowingBorder.collectAsState(initial = false)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GOLDEN GLOWING BORDER",
                    style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 13.sp)
                )
                Switch(
                    checked = glowingBorder,
                    onCheckedChange = { scope.launch { settingsRepository.setGlowingBorder(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = LuxuryColors.AccentGold)
                )
            }

            // Custom UI customization keys
            val fontSizeScale by settingsRepository.fontSizeScale.collectAsState(initial = 1.0f)
            val gradientEnabled by settingsRepository.gradientEnabled.collectAsState(initial = false)
            val auraEffectType by settingsRepository.auraEffectType.collectAsState(initial = "Ribbons & Sparks")
            val meshGradientEnabled by settingsRepository.meshGradientEnabled.collectAsState(initial = true)
            val energyAuraEnabled by settingsRepository.energyAuraEnabled.collectAsState(initial = true)
            val volumeCounterScreenOffEnabled by settingsRepository.volumeCounterScreenOffEnabled.collectAsState(initial = false)
            val layoutOrientation by settingsRepository.layoutOrientation.collectAsState(initial = "horizontal")

            Spacer(modifier = Modifier.height(16.dp))

            // Widget Customization Settings section header
            Text(
                text = "PREMIUM OVERLAY CUSTOMIZATION",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Shape Presets Options
            Text(
                text = "SHAPE PRESET",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 1.sp),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            shapes.forEach { shape ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                settingsRepository.setShapePreset(shape)
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (shapePreset == shape),
                        onClick = {
                            scope.launch {
                                settingsRepository.setShapePreset(shape)
                            }
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = LuxuryColors.AccentGold)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = shape.uppercase(), color = LuxuryColors.CreamyWhite, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Font size scale control
            Text(
                text = "FONT SIZE SCALE",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp)
            )
            Slider(
                value = fontSizeScale,
                onValueChange = { scope.launch { settingsRepository.setFontSizeScale(it) } },
                valueRange = 0.5f..1.5f,
                colors = SliderDefaults.colors(
                    thumbColor = LuxuryColors.AccentGold,
                    activeTrackColor = LuxuryColors.AccentGold
                )
            )
            Text(
                text = "Size Scale: ${String.format("%.2f", fontSizeScale)}",
                style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 12.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Layout Orientation switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VERTICAL DISPLAY ORIENTATION",
                    style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 13.sp)
                )
                Switch(
                    checked = layoutOrientation == "vertical",
                    onCheckedChange = { scope.launch { settingsRepository.setLayoutOrientation(if (it) "vertical" else "horizontal") } },
                    colors = SwitchDefaults.colors(checkedThumbColor = LuxuryColors.AccentGold)
                )
            }

            if (energyAuraEnabled) {
                Text(
                    text = "AURA STYLE PRESET",
                    style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 1.sp),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                val auraTypes = listOf(
                    "Ribbons & Sparks", "Silver Whisper", "Heartbeat Pulse", "Orbital Ring",
                    "Frost Crystals", "Golden Sands", "Sonic Echo", "Living Shadow", "Silk Threads",
                    "Moonlight Glow", "Lightning", "Fire", "Wave", "Rain", "Smoke", "Stardust",
                    "Pulse Ring", "Glow Mist", "Energy Threads", "None/Off"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var expandedAuraMenu by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedAuraMenu = true },
                            border = BorderStroke(1.dp, LuxuryColors.AccentGold),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF0A0A0A))
                        ) {
                            Text(text = auraEffectType, color = LuxuryColors.CreamyWhite, fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = expandedAuraMenu,
                            onDismissRequest = { expandedAuraMenu = false },
                            modifier = Modifier.background(Color(0xFF0A0A0A)).border(1.dp, Color(0xFF2C2C2E))
                        ) {
                            auraTypes.forEach { type ->
                                val isSelected = type == auraEffectType
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = type,
                                            color = if (isSelected) LuxuryColors.AccentGold else LuxuryColors.CreamyWhite,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        scope.launch { settingsRepository.setAuraEffectType(type) }
                                        expandedAuraMenu = false
                                    },
                                    modifier = if (isSelected) Modifier.background(LuxuryColors.AccentGold.copy(alpha = 0.15f)) else Modifier
                                )
                            }
                        }
                    }
                }
            }

            // Volume Counter Screen Off switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VOLUME KEYS COUNTER (SCREEN OFF / BACKGROUND)",
                    style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 13.sp)
                )
                Switch(
                    checked = volumeCounterScreenOffEnabled,
                    onCheckedChange = { scope.launch { settingsRepository.setVolumeCounterScreenOffEnabled(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = LuxuryColors.AccentGold)
                )
            }

            // Energy Aura switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ANIMATED ENERGY AURA EFFECT",
                    style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 13.sp)
                )
                Switch(
                    checked = energyAuraEnabled,
                    onCheckedChange = { scope.launch { settingsRepository.setEnergyAuraEnabled(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = LuxuryColors.AccentGold)
                )
            }

            // Mesh Gradient switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ANIMATED MESH GRADIENT BACKDROP",
                    style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 13.sp)
                )
                Switch(
                    checked = meshGradientEnabled,
                    onCheckedChange = { scope.launch { settingsRepository.setMeshGradientEnabled(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = LuxuryColors.AccentGold)
                )
            }

            // Gradient Text colors switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GRADIENT GOLD DIGITS",
                    style = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 13.sp)
                )
                Switch(
                    checked = gradientEnabled,
                    onCheckedChange = { scope.launch { settingsRepository.setGradientEnabled(it) } },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Display Theme Mode Setting
            Text(
                text = "ILLUMINATION STYLE MODE",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
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

            // Haptic Intensity Settings
            Text(
                text = "HAPTIC FEEDBACK INTENSITY",
                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, letterSpacing = 2.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
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

            // Custom Color HSV / RGB Slider Picker
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

@Composable
fun CountdownDurationPicker(
    totalSeconds: Int,
    onDurationChanged: (Int) -> Unit
) {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeUnitBox("HOURS", hours, max = 23) { newHours ->
            val validHours = newHours.coerceIn(0, 23)
            val newTotal = validHours * 3600 + minutes * 60 + seconds
            if (newTotal > 0) onDurationChanged(newTotal)
        }
        Text(":", color = LuxuryColors.WarmGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        TimeUnitBox("MINS", minutes, max = 59) { newMins ->
            val validMins = newMins.coerceIn(0, 59)
            val newTotal = hours * 3600 + validMins * 60 + seconds
            if (newTotal > 0) onDurationChanged(newTotal)
        }
        Text(":", color = LuxuryColors.WarmGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        TimeUnitBox("SECS", seconds, max = 59) { newSecs ->
            val validSecs = newSecs.coerceIn(0, 59)
            val newTotal = hours * 3600 + minutes * 60 + validSecs
            if (newTotal > 0) onDurationChanged(newTotal)
        }
    }
}

@Composable
fun TimeUnitBox(
    label: String,
    value: Int,
    max: Int,
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = LuxuryColors.WarmGray, fontSize = 9.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(1.dp, LuxuryColors.WarmGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .clickable { onValueChange(if (value > 0) value - 1 else max) }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("-", color = LuxuryColors.AccentGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = String.format("%02d", value),
                color = LuxuryColors.CreamyWhite,
                fontSize = 14.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            Box(
                modifier = Modifier
                    .clickable { onValueChange(if (value < max) value + 1 else 0) }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("+", color = LuxuryColors.AccentGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
