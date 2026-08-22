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
    onFloatClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val stylePreset by settingsRepository.stylePreset.collectAsState(initial = "Glass Premium")
    val colorPreset by settingsRepository.colorPreset.collectAsState(initial = "Gold")
    val hapticIntensity by settingsRepository.hapticIntensity.collectAsState(initial = "Medium")
    val themeMode by settingsRepository.themeMode.collectAsState(initial = "Midnight")
    val mainDisplayScale by settingsRepository.mainDisplayScale.collectAsState(initial = 1.0f)
    val shapePreset by settingsRepository.shapePreset.collectAsState(initial = "rounded")
    val floatingPadding by settingsRepository.floatingPadding.collectAsState(initial = 6.0f)
    val floatingOpacity by settingsRepository.floatingOpacity.collectAsState(initial = 0.85f)
    val volumeCounterScreenOffEnabled by settingsRepository.volumeCounterScreenOffEnabled.collectAsState(initial = false)
    val layoutOrientation by settingsRepository.layoutOrientation.collectAsState(initial = "horizontal")

    val categories = remember {
        listOf(
            "APPEARANCE", "STOPWATCH", "COUNTDOWN", "COUNTER",
            "INTERVAL", "SOUNDS & HAPTICS", "FLOATING WIDGETS", "ADVANCED"
        )
    }

    val shapes = remember { listOf("rounded", "capsule", "circle", "sharp", "glass") }
    val themeModes = remember { listOf("Midnight Dark", "Warm Paper Light", "Obsidian Dark", "Pure White Light") }
    val presets = remember { listOf("Glass Premium", "Obsidian", "Titanium", "Ultra Minimal") }
    val intensities = remember { listOf("Off", "Light", "Medium", "Strong") }
    val colorPresets = remember { listOf("Gold", "Galaxy Blue", "Titanium", "Emerald", "Sapphire", "Violet", "Rose", "Ice", "Amber", "Pure White", "Custom") }

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
                    .clickable { onFloatClick() }
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
                    .clickable { onFloatClick() }
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
                                    Text(text = "ILLUMINATION MODE", color = currentGrayColor, fontSize = 9.sp, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OptionGridCompact(
                                        options = themeModes,
                                        selectedOption = themeMode,
                                        accentColor = accentColor,
                                        onOptionSelected = { mode -> scope.launch { settingsRepository.setThemeMode(mode) } }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(text = "STYLE PRESET", color = currentGrayColor, fontSize = 9.sp, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OptionGridCompact(
                                        options = presets,
                                        selectedOption = stylePreset,
                                        accentColor = accentColor,
                                        onOptionSelected = { preset -> scope.launch { settingsRepository.setStylePreset(preset) } }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(text = "COLOR ACCENT", color = currentGrayColor, fontSize = 9.sp, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OptionGridCompact(
                                        options = colorPresets,
                                        selectedOption = colorPreset,
                                        accentColor = accentColor,
                                        onOptionSelected = { color -> scope.launch { settingsRepository.setColorPreset(color) } }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    DragAdjustField(
                                        label = "MAIN DISPLAY SIZE",
                                        value = mainDisplayScale,
                                        minValue = 0.7f,
                                        maxValue = 1.3f,
                                        pixelsPerUnit = 180f,
                                        accentColor = accentColor,
                                        valueFormatter = { String.format("%.2fx", it) },
                                        onValueChange = { scope.launch { settingsRepository.setMainDisplayScale(it) } }
                                    )
                                }
                                "STOPWATCH" -> WidgetCategorySettingsCompact(index = 0, widgetTitle = "STOPWATCH", settingsRepository = settingsRepository, accentColor = accentColor, currentTextColor = currentTextColor, scope = scope)
                                "COUNTDOWN" -> WidgetCategorySettingsCompact(index = 1, widgetTitle = "COUNTDOWN", settingsRepository = settingsRepository, accentColor = accentColor, currentTextColor = currentTextColor, scope = scope)
                                "COUNTER" -> WidgetCategorySettingsCompact(index = 2, widgetTitle = "COUNTER", settingsRepository = settingsRepository, accentColor = accentColor, currentTextColor = currentTextColor, scope = scope)
                                "INTERVAL" -> {
                                    val intervalName by settingsRepository.intervalName.collectAsState(initial = "HIT")
                                    val workMs by settingsRepository.intervalWorkMs.collectAsState(initial = 40000L)
                                    val restMs by settingsRepository.intervalRestMs.collectAsState(initial = 20000L)
                                    val rounds by settingsRepository.intervalRounds.collectAsState(initial = 8)

                                    var nameInput by remember(intervalName) { mutableStateOf(intervalName) }
                                    var workSecs by remember(workMs) { mutableIntStateOf((workMs / 1000).toInt()) }
                                    var restSecs by remember(restMs) { mutableIntStateOf((restMs / 1000).toInt()) }
                                    var roundsVal by remember(rounds) { mutableIntStateOf(rounds) }

                                    OutlinedTextField(
                                        value = nameInput,
                                        onValueChange = { nameInput = it },
                                        label = { Text("Interval Name", color = currentGrayColor, fontSize = 9.sp) },
                                        textStyle = TextStyle(color = currentTextColor, fontSize = 11.sp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    DragAdjustField(
                                        label = "WORK DURATION",
                                        value = workSecs.toFloat(),
                                        minValue = 1f,
                                        maxValue = 18000f,
                                        pixelsPerUnit = 4f,
                                        accentColor = accentColor,
                                        valueFormatter = { String.format("%02d:%02d", it.toInt() / 3600, (it.toInt() % 3600) / 60) },
                                        onValueChange = { workSecs = it.toInt().coerceIn(1, 18000) }
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    DragAdjustField(
                                        label = "REST DURATION",
                                        value = restSecs.toFloat(),
                                        minValue = 1f,
                                        maxValue = 3600f,
                                        pixelsPerUnit = 4f,
                                        accentColor = accentColor,
                                        valueFormatter = { String.format("%02d:%02d", it.toInt() / 3600, (it.toInt() % 3600) / 60) },
                                        onValueChange = { restSecs = it.toInt().coerceIn(1, 3600) }
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("ROUNDS: $roundsVal", color = currentTextColor, fontSize = 10.sp)
                                        Row {
                                            Text("-", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { if (roundsVal > 1) roundsVal -= 1 }.padding(6.dp))
                                            Text("+", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { roundsVal += 1 }.padding(6.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                settingsRepository.setIntervalConfig(
                                                    name = nameInput.ifBlank { "HIT" },
                                                    workMs = workSecs * 1000L,
                                                    restMs = restSecs * 1000L,
                                                    rounds = roundsVal
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("SAVE INTERVAL", color = LuxuryColors.WarmBlack, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    WidgetCategorySettingsCompact(index = 3, widgetTitle = "INTERVAL", settingsRepository = settingsRepository, accentColor = accentColor, currentTextColor = currentTextColor, scope = scope)
                                }
                                "FLOATING WIDGETS" -> {
                                    Text(text = "SHAPE PRESET", color = currentGrayColor, fontSize = 9.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OptionGridCompact(
                                        options = shapes,
                                        selectedOption = shapePreset,
                                        accentColor = accentColor,
                                        onOptionSelected = { shape -> scope.launch { settingsRepository.setShapePreset(shape) } }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    DragAdjustField(
                                        label = "PADDING",
                                        value = floatingPadding,
                                        minValue = 0f,
                                        maxValue = 32f,
                                        pixelsPerUnit = 8f,
                                        accentColor = accentColor,
                                        valueFormatter = { "${it.toInt()}dp" },
                                        onValueChange = { scope.launch { settingsRepository.setFloatingPadding(it) } }
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    DragAdjustField(
                                        label = "OPACITY",
                                        value = floatingOpacity,
                                        minValue = 0f,
                                        maxValue = 1f,
                                        pixelsPerUnit = 180f,
                                        accentColor = accentColor,
                                        valueFormatter = { "${(it * 100).toInt()}%" },
                                        onValueChange = { scope.launch { settingsRepository.setFloatingOpacity(it) } }
                                    )
                                }
                                "SOUNDS & HAPTICS" -> {
                                    Text(text = "HAPTIC FEEDBACK INTENSITY", color = currentGrayColor, fontSize = 9.sp)
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

@Composable
private fun WidgetCategorySettingsCompact(
    index: Int,
    widgetTitle: String,
    settingsRepository: SettingsRepository,
    accentColor: Color,
    currentTextColor: Color,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val isWidgetActive by settingsRepository.isWidgetActive(index).collectAsState(initial = index == 0)
    val wWidth by settingsRepository.getWidgetWidth(index).collectAsState(initial = 170.0f)
    val wHeight by settingsRepository.getWidgetHeight(index).collectAsState(initial = 56.0f)
    val saveDimensions by settingsRepository.getWidgetSaveDimensions(index).collectAsState(initial = true)
    val fontSizeScale by settingsRepository.getWidgetFontSizeScale(index).collectAsState(initial = 1.0f)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "ENABLE $widgetTitle OVERLAY", color = currentTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        SlimLuxuryToggle(
            checked = isWidgetActive,
            onCheckedChange = { scope.launch { settingsRepository.setWidgetActive(index, it) } },
            accentColor = accentColor
        )
    }

    if (isWidgetActive) {
        Spacer(modifier = Modifier.height(6.dp))

        DragAdjustField(
            label = "WIDTH",
            value = wWidth,
            minValue = 1f,
            maxValue = 320f,
            pixelsPerUnit = 1.5f,
            accentColor = accentColor,
            valueFormatter = { "${it.toInt()}dp" },
            onValueChange = { scope.launch { settingsRepository.setWidgetWidth(index, it) } }
        )

        Spacer(modifier = Modifier.height(4.dp))

        DragAdjustField(
            label = "HEIGHT",
            value = wHeight,
            minValue = 1f,
            maxValue = 120f,
            pixelsPerUnit = 2.5f,
            accentColor = accentColor,
            valueFormatter = { "${it.toInt()}dp" },
            onValueChange = { scope.launch { settingsRepository.setWidgetHeight(index, it) } }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "SAVE FLOATING DIMENSIONS", color = currentTextColor, fontSize = 9.sp)
            SlimLuxuryToggle(
                checked = saveDimensions,
                onCheckedChange = { scope.launch { settingsRepository.setWidgetSaveDimensions(index, it) } },
                accentColor = accentColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        DragAdjustField(
            label = "FONT SIZE SCALE",
            value = fontSizeScale,
            minValue = 0.5f,
            maxValue = 1.5f,
            pixelsPerUnit = 180f,
            accentColor = accentColor,
            valueFormatter = { String.format("%.2f", it) },
            onValueChange = { scope.launch { settingsRepository.setWidgetFontSizeScale(index, it) } }
        )
    }
}
