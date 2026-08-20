package com.floating.stopwatch.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var activeDialogCategory by remember { mutableStateOf<String?>(null) }

    val stylePreset by settingsRepository.stylePreset.collectAsState(initial = "Glass Premium")
    val colorPreset by settingsRepository.colorPreset.collectAsState(initial = "Gold")
    val customColorHex by settingsRepository.customColorHex.collectAsState(initial = "#C9A66B")
    val hapticIntensity by settingsRepository.hapticIntensity.collectAsState(initial = "Medium")
    val themeMode by settingsRepository.themeMode.collectAsState(initial = "Midnight")
    val mainDisplayScale by settingsRepository.mainDisplayScale.collectAsState(initial = 1.0f)

    var customR by remember { mutableFloatStateOf(201f) }
    var customG by remember { mutableFloatStateOf(166f) }
    var customB by remember { mutableFloatStateOf(107f) }

    fun updateCustomColor() {
        val hex = String.format("#%02X%02X%02X", customR.toInt(), customG.toInt(), customB.toInt())
        scope.launch { settingsRepository.setCustomColorHex(hex) }
    }

    val shapes = listOf("rounded", "capsule", "circle", "sharp", "glass")
    val themeModes = listOf("Midnight Dark", "Warm Paper Light", "Obsidian Dark", "Pure White Light")
    val presets = listOf("Glass Premium", "Obsidian", "Titanium", "Ultra Minimal")
    val intensities = listOf("Off", "Light", "Medium", "Strong")
    val colorPresets = listOf("Gold", "Galaxy Blue", "Titanium", "Emerald", "Sapphire", "Violet", "Rose", "Ice", "Amber", "Pure White", "Custom")

    val categories = listOf(
        "Appearance", "Stopwatch", "Countdown", "Counter", "Interval",
        "Floating Widgets", "Sounds & Haptics", "Advanced"
    )

    fun dismissSettings() {
        scope.launch {
            sheetState.hide()
            onBack()
        }
    }

    val activeAccentColor = if (colorPreset == "Custom") {
        try { Color(android.graphics.Color.parseColor(customColorHex)) } catch (e: Exception) { LuxuryColors.AccentGold }
    } else {
        LuxuryColors.fromName(colorPreset)
    }

    ModalBottomSheet(
        onDismissRequest = { dismissSettings() },
        sheetState = sheetState,
        containerColor = LuxuryColors.WarmBlack,
        contentColor = LuxuryColors.CreamyWhite,
        scrimColor = Color.Black.copy(alpha = 0.62f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(38.dp)
                    .height(3.dp)
                    .background(LuxuryColors.WarmGray.copy(alpha = 0.55f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SETTINGS",
                        color = LuxuryColors.CreamyWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SETTINGS CATEGORIES",
                        color = LuxuryColors.WarmGray,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                }
                Text(
                    text = "CLOSE",
                    color = LuxuryColors.WarmGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .clickable { dismissSettings() }
                        .padding(10.dp)
                )
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val columns = if (maxWidth >= 560.dp) 3 else 2
                categories.chunked(columns).forEach { rowCategories ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCategories.forEach { categoryName ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 72.dp)
                                    .clickable { activeDialogCategory = categoryName }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(activeAccentColor.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = categoryName.uppercase(),
                                            color = LuxuryColors.CreamyWhite,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.8.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(text = "▸", color = activeAccentColor.copy(alpha = 0.8f), fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        repeat(columns - rowCategories.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Category Popup Dialogs
    activeDialogCategory?.let { category ->
        val activeAccentColor = if (colorPreset == "Custom") {
            try { Color(android.graphics.Color.parseColor(customColorHex)) } catch (e: Exception) { LuxuryColors.AccentGold }
        } else {
            LuxuryColors.fromName(colorPreset)
        }
        Dialog(onDismissRequest = { activeDialogCategory = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2C2C2E)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.uppercase(),
                            style = TextStyle(color = activeAccentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        )
                        Text(
                            text = "✕",
                            color = LuxuryColors.WarmGray,
                            fontSize = 16.sp,
                            modifier = Modifier.clickable { activeDialogCategory = null }.padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (category) {
                        "Appearance" -> {
                            Text(text = "ILLUMINATION MODE", color = LuxuryColors.WarmGray, fontSize = 10.sp, letterSpacing = 1.sp)
                            themeModes.forEach { mode ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { scope.launch { settingsRepository.setThemeMode(mode) } }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = themeMode == mode, onClick = { scope.launch { settingsRepository.setThemeMode(mode) } }, colors = RadioButtonDefaults.colors(selectedColor = activeAccentColor))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = mode, color = LuxuryColors.CreamyWhite, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(text = "STYLE PRESET", color = LuxuryColors.WarmGray, fontSize = 10.sp, letterSpacing = 1.sp)
                            presets.forEach { preset ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { scope.launch { settingsRepository.setStylePreset(preset) } }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = stylePreset == preset, onClick = { scope.launch { settingsRepository.setStylePreset(preset) } }, colors = RadioButtonDefaults.colors(selectedColor = activeAccentColor))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = preset, color = LuxuryColors.CreamyWhite, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(text = "COLOR ACCENT PRESET", color = LuxuryColors.WarmGray, fontSize = 10.sp, letterSpacing = 1.sp)
                            colorPresets.forEach { color ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { scope.launch { settingsRepository.setColorPreset(color) } }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = colorPreset == color, onClick = { scope.launch { settingsRepository.setColorPreset(color) } }, colors = RadioButtonDefaults.colors(selectedColor = activeAccentColor))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = color, color = LuxuryColors.CreamyWhite, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "MAIN DISPLAY SIZE: ${String.format("%.2fx", mainDisplayScale)}",
                                color = LuxuryColors.WarmGray,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Slider(
                                value = mainDisplayScale,
                                onValueChange = { scope.launch { settingsRepository.setMainDisplayScale(it) } },
                                valueRange = 0.7f..1.3f,
                                steps = 11,
                                colors = SliderDefaults.colors(
                                    thumbColor = activeAccentColor,
                                    activeTrackColor = activeAccentColor
                                )
                            )
                        }
                        "Stopwatch" -> WidgetCategorySettings(index = 0, widgetTitle = "STOPWATCH", settingsRepository = settingsRepository, scope = scope, colorPresets = colorPresets)
                        "Countdown" -> WidgetCategorySettings(index = 1, widgetTitle = "COUNTDOWN", settingsRepository = settingsRepository, scope = scope, colorPresets = colorPresets)
                        "Counter" -> WidgetCategorySettings(index = 2, widgetTitle = "COUNTER", settingsRepository = settingsRepository, scope = scope, colorPresets = colorPresets)
                        "Interval" -> {
                            val intervalName by settingsRepository.intervalName.collectAsState(initial = "HIT")
                            val workMs by settingsRepository.intervalWorkMs.collectAsState(initial = 40000L)
                            val restMs by settingsRepository.intervalRestMs.collectAsState(initial = 20000L)
                            val rounds by settingsRepository.intervalRounds.collectAsState(initial = 8)

                            var nameInput by remember(intervalName) { mutableStateOf(intervalName) }
                            var workSecs by remember(workMs) { mutableIntStateOf((workMs / 1000).toInt()) }
                            var restSecs by remember(restMs) { mutableIntStateOf((restMs / 1000).toInt()) }
                            var roundsVal by remember(rounds) { mutableIntStateOf(rounds) }

                            Text(text = "INTERVAL CONFIGURATION", color = LuxuryColors.WarmGray, fontSize = 10.sp, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Interval Name", color = LuxuryColors.WarmGray, fontSize = 10.sp) },
                                textStyle = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 12.sp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            IntervalDurationDragField(
                                label = "WORK DURATION",
                                totalSeconds = workSecs,
                                minSeconds = 1,
                                maxSeconds = 18000,
                                accentColor = activeAccentColor,
                                onValueChange = { workSecs = it }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            IntervalDurationDragField(
                                label = "REST DURATION",
                                totalSeconds = restSecs,
                                minSeconds = 1,
                                maxSeconds = 3600,
                                accentColor = activeAccentColor,
                                onValueChange = { restSecs = it }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ROUNDS: $roundsVal", color = LuxuryColors.CreamyWhite, fontSize = 11.sp)
                                Row {
                                    Box(modifier = Modifier.clickable { if (roundsVal > 1) roundsVal -= 1 }.padding(8.dp)) {
                                        Text("-", color = activeAccentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(modifier = Modifier.clickable { roundsVal += 1 }.padding(8.dp)) {
                                        Text("+", color = activeAccentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

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
                                colors = ButtonDefaults.buttonColors(containerColor = activeAccentColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("SAVE CONFIGURATION", color = LuxuryColors.WarmBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            WidgetCategorySettings(index = 3, widgetTitle = "INTERVAL", settingsRepository = settingsRepository, scope = scope, colorPresets = colorPresets)
                        }
                        "Floating Widgets" -> {
                            val shapePreset by settingsRepository.shapePreset.collectAsState(initial = "rounded")
                            val floatingPadding by settingsRepository.floatingPadding.collectAsState(initial = 6.0f)
                            val floatingOpacity by settingsRepository.floatingOpacity.collectAsState(initial = 0.85f)

                            Text(text = "SHAPE PRESET", color = LuxuryColors.WarmGray, fontSize = 10.sp)
                            shapes.forEach { shape ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { scope.launch { settingsRepository.setShapePreset(shape) } }.padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = shapePreset == shape, onClick = { scope.launch { settingsRepository.setShapePreset(shape) } }, colors = RadioButtonDefaults.colors(selectedColor = activeAccentColor))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = shape.uppercase(), color = LuxuryColors.CreamyWhite, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(text = "PADDING: ${floatingPadding.toInt()}dp", color = LuxuryColors.WarmGray, fontSize = 10.sp)
                            Slider(value = floatingPadding, onValueChange = { scope.launch { settingsRepository.setFloatingPadding(it) } }, valueRange = 0.0f..32.0f, colors = SliderDefaults.colors(thumbColor = activeAccentColor))

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = "OPACITY: ${(floatingOpacity * 100).toInt()}%", color = LuxuryColors.WarmGray, fontSize = 10.sp)
                            Slider(value = floatingOpacity, onValueChange = { scope.launch { settingsRepository.setFloatingOpacity(it) } }, valueRange = 0.0f..1.0f, colors = SliderDefaults.colors(thumbColor = activeAccentColor))
                        }
                        "Sounds & Haptics" -> {
                            Text(text = "HAPTIC FEEDBACK INTENSITY", color = LuxuryColors.WarmGray, fontSize = 10.sp)
                            intensities.forEach { intensity ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { scope.launch { settingsRepository.setHapticIntensity(intensity) } }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = hapticIntensity == intensity, onClick = { scope.launch { settingsRepository.setHapticIntensity(intensity) } }, colors = RadioButtonDefaults.colors(selectedColor = activeAccentColor))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = intensity, color = LuxuryColors.CreamyWhite, fontSize = 12.sp)
                                }
                            }
                        }
                        "Advanced" -> {
                            val volumeCounterScreenOffEnabled by settingsRepository.volumeCounterScreenOffEnabled.collectAsState(initial = false)
                            val layoutOrientation by settingsRepository.layoutOrientation.collectAsState(initial = "horizontal")

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "VOLUME KEYS COUNTER (SCREEN OFF)", color = LuxuryColors.CreamyWhite, fontSize = 11.sp)
                                Switch(checked = volumeCounterScreenOffEnabled, onCheckedChange = { scope.launch { settingsRepository.setVolumeCounterScreenOffEnabled(it) } }, colors = SwitchDefaults.colors(checkedThumbColor = activeAccentColor))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "VERTICAL DISPLAY ORIENTATION", color = LuxuryColors.CreamyWhite, fontSize = 11.sp)
                                Switch(checked = layoutOrientation == "vertical", onCheckedChange = { scope.launch { settingsRepository.setLayoutOrientation(if (it) "vertical" else "horizontal") } }, colors = SwitchDefaults.colors(checkedThumbColor = activeAccentColor))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntervalDurationDragField(
    label: String,
    totalSeconds: Int,
    minSeconds: Int,
    maxSeconds: Int,
    accentColor: Color,
    onValueChange: (Int) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    val currentTotalSeconds by rememberUpdatedState(totalSeconds)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val density = androidx.compose.ui.platform.LocalDensity.current
    val pixelsPerSecond = with(density) { 4.dp.toPx() }
    val dragHighlight by animateColorAsState(
        targetValue = if (isDragging) accentColor.copy(alpha = 0.12f) else Color.Transparent,
        label = "DurationDragHighlight"
    )
    val valueScale by animateFloatAsState(
        targetValue = if (isDragging) 1.02f else 1.0f,
        label = "DurationDragScale"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (isDragging) 0.55f else 0.22f)),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(minSeconds, maxSeconds) {
                var pendingDistance = 0f
                var draggedSeconds = currentTotalSeconds
                detectDragGestures(
                    onDragStart = {
                        draggedSeconds = currentTotalSeconds
                        isDragging = true
                    },
                    onDragEnd = {
                        pendingDistance = 0f
                        isDragging = false
                    },
                    onDragCancel = {
                        pendingDistance = 0f
                        isDragging = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        pendingDistance -= dragAmount.y
                        val steps = (pendingDistance / pixelsPerSecond).toInt()
                        if (steps != 0) {
                            draggedSeconds = (draggedSeconds + steps).coerceIn(minSeconds, maxSeconds)
                            currentOnValueChange(draggedSeconds)
                            pendingDistance -= steps * pixelsPerSecond
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(dragHighlight)
                .padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = LuxuryColors.WarmGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = String.format("%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60),
                color = if (isDragging) accentColor else LuxuryColors.CreamyWhite,
                fontSize = 26.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                modifier = Modifier.scale(valueScale)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "DRAG UP/DOWN TO ADJUST",
                color = LuxuryColors.WarmGray.copy(alpha = 0.72f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
fun WidgetCategorySettings(
    index: Int,
    widgetTitle: String,
    settingsRepository: SettingsRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    colorPresets: List<String>
) {
    val isWidgetActive by settingsRepository.isWidgetActive(index).collectAsState(initial = index == 0)
    val wWidth by settingsRepository.getWidgetWidth(index).collectAsState(initial = 170.0f)
    val wHeight by settingsRepository.getWidgetHeight(index).collectAsState(initial = 56.0f)
    val saveDimensions by settingsRepository.getWidgetSaveDimensions(index).collectAsState(initial = true)
    val fontSizeScale by settingsRepository.getWidgetFontSizeScale(index).collectAsState(initial = 1.0f)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "ENABLE $widgetTitle OVERLAY", color = LuxuryColors.CreamyWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Switch(checked = isWidgetActive, onCheckedChange = { scope.launch { settingsRepository.setWidgetActive(index, it) } }, colors = SwitchDefaults.colors(checkedThumbColor = LuxuryColors.AccentGold))
    }

    if (isWidgetActive) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "WIDTH: ${wWidth.toInt()}dp", color = LuxuryColors.WarmGray, fontSize = 10.sp)
        Slider(value = wWidth, onValueChange = { scope.launch { settingsRepository.setWidgetWidth(index, it) } }, valueRange = 1.0f..320.0f, colors = SliderDefaults.colors(thumbColor = LuxuryColors.AccentGold))

        Text(text = "HEIGHT: ${wHeight.toInt()}dp", color = LuxuryColors.WarmGray, fontSize = 10.sp)
        Slider(value = wHeight, onValueChange = { scope.launch { settingsRepository.setWidgetHeight(index, it) } }, valueRange = 1.0f..120.0f, colors = SliderDefaults.colors(thumbColor = LuxuryColors.AccentGold))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "SAVE FLOATING DIMENSIONS", color = LuxuryColors.CreamyWhite, fontSize = 10.sp)
            Switch(checked = saveDimensions, onCheckedChange = { scope.launch { settingsRepository.setWidgetSaveDimensions(index, it) } }, colors = SwitchDefaults.colors(checkedThumbColor = LuxuryColors.AccentGold))
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(text = "FONT SIZE SCALE: ${String.format("%.2f", fontSizeScale)}", color = LuxuryColors.WarmGray, fontSize = 10.sp)
        Slider(value = fontSizeScale, onValueChange = { scope.launch { settingsRepository.setWidgetFontSizeScale(index, it) } }, valueRange = 0.5f..1.5f, colors = SliderDefaults.colors(thumbColor = LuxuryColors.AccentGold))

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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
            modifier = Modifier.border(1.dp, LuxuryColors.WarmGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Box(modifier = Modifier.clickable { onValueChange(if (value > 0) value - 1 else max) }.padding(horizontal = 6.dp, vertical = 2.dp)) {
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
            Box(modifier = Modifier.clickable { onValueChange(if (value < max) value + 1 else 0) }.padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("+", color = LuxuryColors.AccentGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
