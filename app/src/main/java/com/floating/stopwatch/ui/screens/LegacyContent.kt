package com.floating.stopwatch.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.window.Dialog
import com.floating.stopwatch.domain.*
import com.floating.stopwatch.ui.components.DragAdjustField
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.floating.stopwatch.ui.components.TimeDisplay
import com.floating.stopwatch.ui.theme.LuxuryColors

@Composable
fun LegacyContent(
    legacyEngine: LegacyEngine,
    accentColor: Color,
    currentTextColor: Color,
    currentGrayColor: Color,
    mainSize: Float,
    secondaryAlpha: Float,
    scalePulse: Float,
    resetAutoHideTimer: () -> Unit
) {
    val legacies by legacyEngine.legacies.collectAsState()
    val activeLegacy by legacyEngine.activeLegacy.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showManualTimeDialog by remember { mutableStateOf(false) }
    var showPostponeDialog by remember { mutableStateOf(false) }
    var showSelectorDialog by remember { mutableStateOf(false) }

    if (legacies.isEmpty()) {
        // Native Legacy Empty State
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "الأهداف والإنجازات",
                style = TextStyle(
                    color = accentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "لم يتم إنشاء أهداف بعد",
                style = TextStyle(
                    color = currentGrayColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        resetAutoHideTimer()
                        showCreateDialog = true
                    },
                color = accentColor,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "+ إنشاء هدف جديد",
                    style = TextStyle(
                        color = LuxuryColors.WarmBlack,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    } else if (activeLegacy != null) {
        val legacy = activeLegacy!!
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Selected Legacy Name & Switcher / Add
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = legacy.name.uppercase(),
                    style = TextStyle(
                        color = accentColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                )
                if (legacies.size > 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "▾",
                        style = TextStyle(
                            color = currentGrayColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable {
                                resetAutoHideTimer()
                                showSelectorDialog = true
                            }
                            .padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "+ جديد",
                    style = TextStyle(
                        color = currentGrayColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier
                        .clickable {
                            resetAutoHideTimer()
                            showCreateDialog = true
                        }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Time Display: Completed Time
            TimeDisplay(
                elapsedTimeMs = legacy.completedTimeMs,
                showCentiseconds = false,
                baseStyle = TextStyle(color = currentTextColor, fontSize = 48.sp),
                scaleFactor = mainSize,
                accentColor = accentColor,
                modifier = Modifier
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Status Badge: ON PACE / AHEAD / BEHIND
            val status = legacy.getStatus()
            val statusColor = when (status) {
                LegacyStatus.AHEAD -> Color(0xFF4AC98F)
                LegacyStatus.ON_PACE -> accentColor
                LegacyStatus.BEHIND -> Color(0xFFC94A4A)
            }
            val statusText = when (status) {
                LegacyStatus.AHEAD -> "متقدم"
                LegacyStatus.ON_PACE -> "ضمن المخطط"
                LegacyStatus.BEHIND -> "متأخر"
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(statusColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusText,
                    style = TextStyle(
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${String.format(Locale.US, "%.1f", legacy.progressPercentage)}%",
                    style = TextStyle(
                        color = currentTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = com.floating.stopwatch.ui.theme.DiwaniFontFamily
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed Progress Metrics Panel
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Row 1: Total Target & Remaining Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "الهدف الكلي",
                            value = formatMsToHoursMinutes(legacy.targetDurationMs),
                            grayColor = currentGrayColor,
                            textColor = currentTextColor
                        )
                        MetricItem(
                            label = "المتبقي",
                            value = formatMsToHoursMinutes(legacy.remainingTimeMs),
                            grayColor = currentGrayColor,
                            textColor = currentTextColor
                        )
                        MetricItem(
                            label = "الأيام المتبقية",
                            value = "${legacy.getRemainingDays()} / ${legacy.totalTargetDays}ي",
                            grayColor = currentGrayColor,
                            textColor = currentTextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Today's Target & Today's Completed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "هدف اليوم",
                            value = formatMsToHoursMinutes(legacy.dailyTargetMs),
                            grayColor = currentGrayColor,
                            textColor = currentTextColor
                        )
                        MetricItem(
                            label = "المُنجز اليوم",
                            value = formatMsToHoursMinutes(legacy.getTodayCompletedMs()),
                            grayColor = currentGrayColor,
                            textColor = accentColor
                        )
                        MetricItem(
                            label = "المُضاف يدوياً",
                            value = formatMsToHoursMinutes(legacy.manualTimeMs),
                            grayColor = currentGrayColor,
                            textColor = currentTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Manual Time & Postpone
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[+ إضافة وقت يدوياً]",
                    style = TextStyle(
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier
                        .clickable {
                            resetAutoHideTimer()
                            showManualTimeDialog = true
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "[تمديد الموعد]",
                    style = TextStyle(
                        color = currentGrayColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier
                        .clickable {
                            resetAutoHideTimer()
                            showPostponeDialog = true
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    // Dialogs
    if (showCreateDialog) {
        CreateLegacyDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, totalHours, days ->
                val targetDurationMs = totalHours * 3600 * 1000L
                val dailyTargetMs = if (days > 0) targetDurationMs / days else targetDurationMs
                val now = System.currentTimeMillis()
                val targetDateMs = now + (days * 24 * 3600 * 1000L)
                legacyEngine.createLegacy(
                    name = name,
                    targetDurationMs = targetDurationMs,
                    totalDays = days,
                    dailyTargetMs = dailyTargetMs,
                    targetDateMs = targetDateMs
                )
                showCreateDialog = false
            }
        )
    }

    if (showManualTimeDialog) {
        ManualTimeDialog(
            onDismiss = { showManualTimeDialog = false },
            onAdd = { hours, minutes ->
                legacyEngine.addManualTime(hours, minutes)
                showManualTimeDialog = false
            }
        )
    }

    if (showPostponeDialog) {
        PostponeDialog(
            onDismiss = { showPostponeDialog = false },
            onPostpone = { days ->
                legacyEngine.postpone(days)
                showPostponeDialog = false
            }
        )
    }

    if (showSelectorDialog && legacies.size > 1) {
        LegacySelectorDialog(
            legacies = legacies,
            selectedId = activeLegacy?.id ?: "",
            onDismiss = { showSelectorDialog = false },
            onSelect = { id ->
                legacyEngine.selectLegacy(id)
                showSelectorDialog = false
            },
            onDelete = { id ->
                legacyEngine.deleteLegacy(id)
                if (legacies.size <= 1) showSelectorDialog = false
            }
        )
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    grayColor: Color,
    textColor: Color
) {
    Column {
        Text(
            text = label,
            style = TextStyle(color = grayColor, fontSize = 9.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = TextStyle(color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = com.floating.stopwatch.ui.theme.DiwaniFontFamily)
        )
    }
}

fun formatMsToHoursMinutes(ms: Long): String {
    val totalSecs = ms / 1000
    val hrs = totalSecs / 3600
    val mins = (totalSecs % 3600) / 60
    return String.format(Locale.US, "%02dh %02dm", hrs, mins)
}

@Composable
fun CreateLegacyDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, totalHours: Int, days: Int) -> Unit
) {
    var name by remember { mutableStateOf("My Goal") }
    var totalHours by remember { mutableIntStateOf(100) }
    var days by remember { mutableIntStateOf(30) }

    Dialog(onDismissRequest = onDismiss) {
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
                    text = "إنشاء هدف جديد",
                    style = TextStyle(color = LuxuryColors.AccentGold, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الهدف", color = LuxuryColors.WarmGray, fontSize = 10.sp) },
                    textStyle = TextStyle(color = LuxuryColors.CreamyWhite, fontSize = 12.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                DragAdjustField(
                    label = "المدة المستهدفة (ساعات)",
                    value = totalHours.toFloat(),
                    minValue = 1f,
                    maxValue = 10000f,
                    pixelsPerUnit = 2f,
                    accentColor = LuxuryColors.AccentGold,
                    valueFormatter = { "${it.toInt()} ساعة" },
                    onValueChange = { totalHours = it.toInt().coerceIn(1, 10000) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                DragAdjustField(
                    label = "عدد الأيام",
                    value = days.toFloat(),
                    minValue = 1f,
                    maxValue = 365f,
                    pixelsPerUnit = 4f,
                    accentColor = LuxuryColors.AccentGold,
                    valueFormatter = { "${it.toInt()} يوم" },
                    onValueChange = { days = it.toInt().coerceIn(1, 365) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                val dailyMins = (totalHours * 60) / days.coerceAtLeast(1)
                Text(
                    text = "الهدف اليومي: ${dailyMins / 60}س ${dailyMins % 60}د / يوم",
                    style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp)
                )

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
                            onCreate(name.ifBlank { "هدف جديد" }, totalHours, days)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryColors.AccentGold)
                    ) {
                        Text("إنشاء", color = LuxuryColors.WarmBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ManualTimeDialog(
    onDismiss: () -> Unit,
    onAdd: (hours: Int, minutes: Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(1) }
    var minutes by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
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
                    text = "إضافة وقت يدوياً",
                    style = TextStyle(color = LuxuryColors.AccentGold, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                DragAdjustField(
                    label = "ساعات",
                    value = hours.toFloat(),
                    minValue = 0f,
                    maxValue = 100f,
                    pixelsPerUnit = 4f,
                    accentColor = LuxuryColors.AccentGold,
                    valueFormatter = { "${it.toInt()} س" },
                    onValueChange = { hours = it.toInt().coerceIn(0, 100) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                DragAdjustField(
                    label = "دقائق",
                    value = minutes.toFloat(),
                    minValue = 0f,
                    maxValue = 59f,
                    pixelsPerUnit = 4f,
                    accentColor = LuxuryColors.AccentGold,
                    valueFormatter = { "${it.toInt()} د" },
                    onValueChange = { minutes = it.toInt().coerceIn(0, 59) }
                )

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
                        onClick = { onAdd(hours, minutes) },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryColors.AccentGold)
                    ) {
                        Text("إضافة", color = LuxuryColors.WarmBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PostponeDialog(
    onDismiss: () -> Unit,
    onPostpone: (days: Int) -> Unit
) {
    var postponeDays by remember { mutableIntStateOf(7) }

    Dialog(onDismissRequest = onDismiss) {
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
                    text = "تمديد موعد الهدف",
                    style = TextStyle(color = LuxuryColors.AccentGold, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                DragAdjustField(
                    label = "أيام إضافية",
                    value = postponeDays.toFloat(),
                    minValue = 1f,
                    maxValue = 90f,
                    pixelsPerUnit = 4f,
                    accentColor = LuxuryColors.AccentGold,
                    valueFormatter = { "+${it.toInt()} يوم" },
                    onValueChange = { postponeDays = it.toInt().coerceIn(1, 90) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "سيتم الاحتفاظ بجميع التقدم المحرز.",
                    style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 11.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp)
                )

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
                        onClick = { onPostpone(postponeDays) },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryColors.AccentGold)
                    ) {
                        Text("تمديد", color = LuxuryColors.WarmBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LegacySelectorDialog(
    legacies: List<Legacy>,
    selectedId: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                    text = "اختر الهدف",
                    style = TextStyle(color = LuxuryColors.AccentGold, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                legacies.forEach { item ->
                    val isSelected = item.id == selectedId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) LuxuryColors.AccentGold.copy(alpha = 0.12f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelect(item.id) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.name,
                                style = TextStyle(
                                    color = if (isSelected) LuxuryColors.AccentGold else LuxuryColors.CreamyWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", item.progressPercentage)}% مكتمل",
                                style = TextStyle(color = LuxuryColors.WarmGray, fontSize = 10.sp)
                            )
                        }
                        if (legacies.size > 1) {
                            var isPressingItemDelete by remember { mutableStateOf(false) }
                            val scope = rememberCoroutineScope()
                            Text(
                                text = "اضغط مطولاً للحذف",
                                style = TextStyle(
                                    color = if (isPressingItemDelete) Color(0xFFE53935) else Color(0xFFC94A4A),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .pointerInput(item.id) {
                                        detectTapGestures(
                                            onPress = {
                                                isPressingItemDelete = true
                                                var triggered = false
                                                val job = scope.launch {
                                                    kotlinx.coroutines.delay(500L)
                                                    triggered = true
                                                    onDelete(item.id)
                                                }
                                                tryAwaitRelease()
                                                isPressingItemDelete = false
                                                if (!triggered) job.cancel()
                                            }
                                        )
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "إغلاق",
                        color = LuxuryColors.WarmGray,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
