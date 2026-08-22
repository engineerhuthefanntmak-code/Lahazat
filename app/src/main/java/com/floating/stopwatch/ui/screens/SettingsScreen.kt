package com.floating.stopwatch.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.data.SettingsRepository
import com.floating.stopwatch.ui.components.DragAdjustField
import com.floating.stopwatch.ui.theme.LuxuryColors
import java.util.Locale
import kotlinx.coroutines.launch

sealed class SettingsPresentationState {
    object Closed : SettingsPresentationState()
    object Menu : SettingsPresentationState()
    data class Panel(val category: String) : SettingsPresentationState()
}

@Composable
fun SlimLuxuryToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 16f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "SlimToggleOffset"
    )

    Box(
        modifier = modifier
            .width(36.dp)
            .height(18.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (checked) accentColor.copy(alpha = 0.25f) else Color(0xFF1E1E1E))
            .border(
                width = 1.dp,
                color = if (checked) accentColor else LuxuryColors.WarmGray.copy(alpha = 0.4f),
                shape = RoundedCornerShape(9.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset.dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(if (checked) accentColor else LuxuryColors.WarmGray)
        )
    }
}

@Composable
fun SettingsOverlay(
    settingsRepository: SettingsRepository,
    accentColor: Color,
    currentGrayColor: Color,
    currentTextColor: Color,
    presentationState: SettingsPresentationState,
    onStateChange: (SettingsPresentationState) -> Unit,
    onFloatClick: () -> Unit,
    onFloatLongClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val colorPreset by settingsRepository.colorPreset.collectAsState(initial = "Champagne Gold")
    val hapticIntensity by settingsRepository.hapticIntensity.collectAsState(initial = "Medium")
    val mainDisplayScale by settingsRepository.mainDisplayScale.collectAsState(initial = 1.0f)
    val volumeCounterScreenOffEnabled by settingsRepository.volumeCounterScreenOffEnabled.collectAsState(initial = false)
    val layoutOrientation by settingsRepository.layoutOrientation.collectAsState(initial = "horizontal")

    val backgroundAtmosphere by settingsRepository.backgroundAtmosphere.collectAsState(initial = "Pure Black")
    val atmospheres = remember {
        listOf(
            "Pure Black", "MIDNIGHT SILK", "CELESTIAL VEIL", "LIQUID SHADOW", "GOLDEN DUST",
            "MOONLIT MIST", "OBSIDIAN FLOW", "DEEP OCEAN", "VELVET NIGHT", "AURORA VEIL",
            "COSMIC DUST", "ABSTRACT GALLERY", "SILENT ARCHITECTURE", "AFTER RAIN", "ECLIPSE",
            "NOCTURNAL GARDEN", "INK & LIGHT", "CHAMPAGNE DUSK", "PRIVATE SKY", "SILENT GALAXY", "MONOLITHIC LIGHT"
        )
    }

    val masterSoundEnabled by settingsRepository.masterSoundEnabled.collectAsState(initial = true)
    val selectedSoundType by settingsRepository.selectedSoundType.collectAsState(initial = "Soft Click")

    val categories = remember {
        listOf("APPEARANCE", "SOUNDS & HAPTICS", "ADVANCED")
    }

    val intensities = remember { listOf("Off", "Light", "Medium", "Strong") }
    val soundTypes = remember {
        listOf(
            "Premium Click", "Mechanical Click", "Soft Button Tap", "Fingertip Tap",
            "Wood Knock", "Glass Tap", "Metal Tap", "Paper Tap",
            "Light Rattle", "Soft Rattle", "Small Object Shake", "Tiny Bell",
            "Water Drop", "Soft Chime", "Finger Snap", "Gentle Clap",
            "Cat Meow", "Pigeon Coo", "Small Bird Chirp", "Nature Chirp"
        )
    }
    val colorPresets = remember {
        listOf(
            "Champagne", "Antique Gold", "Brushed Gold", "Rose Gold", "Pale Gold",
            "Platinum", "Titanium", "Pearl", "Ivory", "Porcelain",
            "Sand", "Taupe", "Bronze", "Copper", "Dark Copper",
            "Mocha", "Espresso", "Deep Olive", "Sage", "Emerald",
            "Forest", "Slate", "Steel Blue", "Midnight Blue", "Royal Navy",
            "Deep Burgundy", "Wine", "Plum", "Graphite", "Charcoal"
        )
    }

    val scaleStopwatch by settingsRepository.scaleStopwatch.collectAsState(initial = 1.0f)
    val scaleCountdown by settingsRepository.scaleCountdown.collectAsState(initial = 1.0f)
    val scaleCounter by settingsRepository.scaleCounter.collectAsState(initial = 1.0f)
    val scaleInterval by settingsRepository.scaleInterval.collectAsState(initial = 1.0f)
    val scaleLegacy by settingsRepository.scaleLegacy.collectAsState(initial = 1.0f)

    if (presentationState is SettingsPresentationState.Closed) {
        // Closed state: SETTINGS and FLOAT visible in top end
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "SETTINGS",
                style = TextStyle(
                    color = currentGrayColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier
                    .clickable { onStateChange(SettingsPresentationState.Menu) }
                    .padding(8.dp)
            )

            Text(
                text = "FLOAT ↗",
                style = TextStyle(
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onFloatClick() },
                            onLongPress = { onFloatLongClick() }
                        )
                    }
                    .padding(8.dp)
            )
        }
        return
    }

    // Intercept Back button
    BackHandler {
        when (presentationState) {
            is SettingsPresentationState.Panel -> onStateChange(SettingsPresentationState.Menu)
            is SettingsPresentationState.Menu -> onStateChange(SettingsPresentationState.Closed)
            else -> {}
        }
    }

    // Full screen invisible touch blocker for dismiss on tap-outside
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(presentationState) {
                detectTapGestures(
                    onTap = {
                        onStateChange(SettingsPresentationState.Closed)
                    }
                )
            }
    ) {
        // Top End Column: FLOAT + MENU / PANEL anchored near top end area
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 24.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { /* consume click inside menu/panel */ })
                }
        ) {
            // FLOAT stays visible at top end
            Text(
                text = "FLOAT ↗",
                style = TextStyle(
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onFloatClick() },
                            onLongPress = { onFloatLongClick() }
                        )
                    }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            when (presentationState) {
                is SettingsPresentationState.Menu -> {
                    // Category List rendered with exact FLOAT visual language
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0A0A0A)) // Completely solid opaque
                            .border(BorderStroke(1.dp, Color(0xFF222222)), RoundedCornerShape(10.dp))
                            .padding(vertical = 4.dp)
                    ) {
                        categories.forEach { category ->
                            Text(
                                text = category,
                                style = TextStyle(
                                    color = currentGrayColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 2.sp
                                ),
                                modifier = Modifier
                                    .clickable { onStateChange(SettingsPresentationState.Panel(category)) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                is SettingsPresentationState.Panel -> {
                    val categoryName = presentationState.category

                    // Compact, 100% OPAQUE panel surface anchored near upper area (never centered, never fullscreen)
                    Surface(
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0A0A0A), // 100% Solid Opaque - no timer digits or stellar bg visible through panel
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.7f)), // Refined Luxury Minimal outline around selected category
                        shadowElevation = 16.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Header Row: Thin Refined Outline around category + Back / Close
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .border(BorderStroke(1.dp, accentColor), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = categoryName,
                                        style = TextStyle(
                                            color = accentColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        )
                                    )
                                }

                                Text(
                                    text = "‹ BACK",
                                    style = TextStyle(
                                        color = currentGrayColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.5.sp
                                    ),
                                    modifier = Modifier
                                        .clickable { onStateChange(SettingsPresentationState.Menu) }
                                        .padding(6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Category Controls
                            when (categoryName) {
                                "APPEARANCE" -> {
                                    Text(text = "COLOR ACCENT", color = currentGrayColor, fontSize = 9.sp, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OptionGridCompact(
                                        options = colorPresets,
                                        selectedOption = colorPreset,
                                        accentColor = accentColor,
                                        onOptionSelected = { color -> scope.launch { settingsRepository.setColorPreset(color) } }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(text = "BACKGROUND ATMOSPHERE", color = currentGrayColor, fontSize = 9.sp, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OptionGridCompact(
                                        options = atmospheres,
                                        selectedOption = backgroundAtmosphere,
                                        accentColor = accentColor,
                                        onOptionSelected = { atmosphere -> scope.launch { settingsRepository.setBackgroundAtmosphere(atmosphere) } }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    DragAdjustField(
                                        label = "MAIN DISPLAY SIZE",
                                        value = mainDisplayScale,
                                        minValue = 0.7f,
                                        maxValue = 1.3f,
                                        pixelsPerUnit = 180f,
                                        accentColor = accentColor,
                                        valueFormatter = { String.format(Locale.US, "%.2fx", it) },
                                        onValueChange = { scope.launch { settingsRepository.setMainDisplayScale(it) } }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(text = "INDEPENDENT DIGIT SIZE SCALES", color = currentGrayColor, fontSize = 9.sp, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    DragAdjustField(
                                        label = "STOPWATCH",
                                        value = scaleStopwatch,
                                        minValue = 0.60f,
                                        maxValue = 2.00f,
                                        pixelsPerUnit = 120f,
                                        accentColor = accentColor,
                                        valueFormatter = { "${(it * 100).toInt()}%" },
                                        onValueChange = { scope.launch { settingsRepository.setScaleStopwatch(it) } }
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    DragAdjustField(
                                        label = "COUNTDOWN",
                                        value = scaleCountdown,
                                        minValue = 0.60f,
                                        maxValue = 2.00f,
                                        pixelsPerUnit = 120f,
                                        accentColor = accentColor,
                                        valueFormatter = { "${(it * 100).toInt()}%" },
                                        onValueChange = { scope.launch { settingsRepository.setScaleCountdown(it) } }
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    DragAdjustField(
                                        label = "COUNTER",
                                        value = scaleCounter,
                                        minValue = 0.60f,
                                        maxValue = 2.00f,
                                        pixelsPerUnit = 120f,
                                        accentColor = accentColor,
                                        valueFormatter = { "${(it * 100).toInt()}%" },
                                        onValueChange = { scope.launch { settingsRepository.setScaleCounter(it) } }
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    DragAdjustField(
                                        label = "INTERVAL",
                                        value = scaleInterval,
                                        minValue = 0.60f,
                                        maxValue = 2.00f,
                                        pixelsPerUnit = 120f,
                                        accentColor = accentColor,
                                        valueFormatter = { "${(it * 100).toInt()}%" },
                                        onValueChange = { scope.launch { settingsRepository.setScaleInterval(it) } }
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    DragAdjustField(
                                        label = "LEGACY",
                                        value = scaleLegacy,
                                        minValue = 0.60f,
                                        maxValue = 2.00f,
                                        pixelsPerUnit = 120f,
                                        accentColor = accentColor,
                                        valueFormatter = { "${(it * 100).toInt()}%" },
                                        onValueChange = { scope.launch { settingsRepository.setScaleLegacy(it) } }
                                    )
                                }
                                "SOUNDS & HAPTICS" -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "MASTER SOUND", color = currentTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        SlimLuxuryToggle(
                                            checked = masterSoundEnabled,
                                            onCheckedChange = { scope.launch { settingsRepository.setMasterSoundEnabled(it) } },
                                            accentColor = accentColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(text = "NATURAL SOUND PRESET", color = currentGrayColor, fontSize = 9.sp, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OptionGridCompact(
                                        options = soundTypes,
                                        selectedOption = selectedSoundType,
                                        accentColor = accentColor,
                                        onOptionSelected = { sound ->
                                            scope.launch {
                                                settingsRepository.setSelectedSoundType(sound)
                                                com.floating.stopwatch.domain.CompletionSoundPlayer.playSound(sound, masterSoundEnabled)
                                            }
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(text = "HAPTIC FEEDBACK INTENSITY", color = currentGrayColor, fontSize = 9.sp, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OptionGridCompact(
                                        options = intensities,
                                        selectedOption = hapticIntensity,
                                        accentColor = accentColor,
                                        onOptionSelected = { intensity -> scope.launch { settingsRepository.setHapticIntensity(intensity) } }
                                    )
                                }
                                "ADVANCED" -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "VOLUME KEYS COUNTER (SCREEN OFF)", color = currentTextColor, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                        SlimLuxuryToggle(
                                            checked = volumeCounterScreenOffEnabled,
                                            onCheckedChange = { scope.launch { settingsRepository.setVolumeCounterScreenOffEnabled(it) } },
                                            accentColor = accentColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "VERTICAL DISPLAY ORIENTATION", color = currentTextColor, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                        SlimLuxuryToggle(
                                            checked = layoutOrientation == "vertical",
                                            onCheckedChange = { scope.launch { settingsRepository.setLayoutOrientation(if (it) "vertical" else "horizontal") } },
                                            accentColor = accentColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun FloatingModeConfigDialog(
    modeIndex: Int,
    modeName: String,
    settingsRepository: SettingsRepository,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val wWidth by settingsRepository.getWidgetWidth(modeIndex).collectAsState(initial = 170.0f)
    val wHeight by settingsRepository.getWidgetHeight(modeIndex).collectAsState(initial = 56.0f)
    val fontSizeScale by settingsRepository.getWidgetFontSizeScale(modeIndex).collectAsState(initial = 1.0f)
    val saveDimensions by settingsRepository.getWidgetSaveDimensions(modeIndex).collectAsState(initial = true)

    val floatingOpacity by settingsRepository.floatingOpacity.collectAsState(initial = 0.85f)
    val floatingPadding by settingsRepository.floatingPadding.collectAsState(initial = 6.0f)
    val shapePreset by settingsRepository.shapePreset.collectAsState(initial = "rounded")

    val shapes = remember { listOf("rounded", "capsule", "circle", "sharp", "glass") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) }
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0A0A0A),
            border = BorderStroke(1.dp, accentColor),
            shadowElevation = 16.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 48.dp, end = 24.dp)
                .widthIn(max = 320.dp)
                .wrapContentHeight()
                .pointerInput(Unit) { detectTapGestures { /* consume click inside */ } }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "${modeName.uppercase()} FLOATING CONFIG",
                    style = TextStyle(color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                DragAdjustField(
                    label = "WIDTH",
                    value = wWidth,
                    minValue = 1f,
                    maxValue = 320f,
                    pixelsPerUnit = 1.5f,
                    accentColor = accentColor,
                    valueFormatter = { "${it.toInt()}dp" },
                    onValueChange = { scope.launch { settingsRepository.setWidgetWidth(modeIndex, it) } }
                )

                Spacer(modifier = Modifier.height(6.dp))

                DragAdjustField(
                    label = "HEIGHT",
                    value = wHeight,
                    minValue = 1f,
                    maxValue = 120f,
                    pixelsPerUnit = 2.5f,
                    accentColor = accentColor,
                    valueFormatter = { "${it.toInt()}dp" },
                    onValueChange = { scope.launch { settingsRepository.setWidgetHeight(modeIndex, it) } }
                )

                Spacer(modifier = Modifier.height(6.dp))

                DragAdjustField(
                    label = "FONT SIZE SCALE",
                    value = fontSizeScale,
                    minValue = 0.5f,
                    maxValue = 2.0f,
                    pixelsPerUnit = 120f,
                    accentColor = accentColor,
                    valueFormatter = { String.format(Locale.US, "%.2fx", it) },
                    onValueChange = { scope.launch { settingsRepository.setWidgetFontSizeScale(modeIndex, it) } }
                )

                Spacer(modifier = Modifier.height(6.dp))

                DragAdjustField(
                    label = "OPACITY",
                    value = floatingOpacity,
                    minValue = 0.10f,
                    maxValue = 1.00f,
                    pixelsPerUnit = 180f,
                    accentColor = accentColor,
                    valueFormatter = { "${(it * 100).toInt()}%" },
                    onValueChange = { scope.launch { settingsRepository.setFloatingOpacity(it) } }
                )

                Spacer(modifier = Modifier.height(6.dp))

                DragAdjustField(
                    label = "INTERNAL PADDING",
                    value = floatingPadding,
                    minValue = 0f,
                    maxValue = 32f,
                    pixelsPerUnit = 8f,
                    accentColor = accentColor,
                    valueFormatter = { "${it.toInt()}dp" },
                    onValueChange = { scope.launch { settingsRepository.setFloatingPadding(it) } }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "SHAPE PRESET", color = LuxuryColors.WarmGray, fontSize = 9.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OptionGridCompact(
                    options = shapes,
                    selectedOption = shapePreset,
                    accentColor = accentColor,
                    onOptionSelected = { shape -> scope.launch { settingsRepository.setShapePreset(shape) } }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "SAVE DIMENSIONS", color = LuxuryColors.CreamyWhite, fontSize = 10.sp)
                    SlimLuxuryToggle(
                        checked = saveDimensions,
                        onCheckedChange = { scope.launch { settingsRepository.setWidgetSaveDimensions(modeIndex, it) } },
                        accentColor = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "DONE",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionGridCompact(
    options: List<String>,
    selectedOption: String,
    accentColor: Color,
    onOptionSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.chunked(2).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rowOptions.forEach { option ->
                    val isSelected = selectedOption == option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) accentColor.copy(alpha = 0.18f) else Color(0xFF141414))
                            .border(1.dp, if (isSelected) accentColor else Color(0xFF222222), RoundedCornerShape(6.dp))
                            .clickable { onOptionSelected(option) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.uppercase(),
                            color = if (isSelected) LuxuryColors.CreamyWhite else LuxuryColors.WarmGray,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                if (rowOptions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
