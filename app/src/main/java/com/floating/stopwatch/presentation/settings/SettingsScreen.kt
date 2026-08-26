package com.floating.stopwatch.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.R
import com.floating.stopwatch.core.utils.toArabicNumerals
import com.floating.stopwatch.designsystem.theme.AlRayyashColors
import com.floating.stopwatch.designsystem.theme.SulsFontFamily

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTheme by remember { mutableStateOf("داكن") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlRayyashColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        // Top Row Back
        Row(
            modifier = Modifier
                .clickable { onNavigateBack() }
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "›",
                color = AlRayyashColors.GoldAccent,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "رجوع",
                color = AlRayyashColors.GoldAccent,
                fontFamily = SulsFontFamily,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 6.dp)
            )
        }

        Text(
            text = "الإعدادات",
            color = AlRayyashColors.TextPrimary,
            fontFamily = SulsFontFamily,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // المظهر
        SettingsSectionTitle("المظهر")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("داكن", "فاتح", "تلقائي").forEach { mode ->
                val isSelected = mode == selectedTheme
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AlRayyashColors.GoldSurface else AlRayyashColors.SurfaceDeep)
                        .border(
                            1.dp,
                            if (isSelected) AlRayyashColors.GoldAccent else AlRayyashColors.SurfaceBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTheme = mode }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode,
                        color = if (isSelected) AlRayyashColors.GoldAccent else AlRayyashColors.TextSecondary,
                        fontFamily = SulsFontFamily,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // الخط
        SettingsSectionTitle("الخط")
        SettingsInfoRow(title = "حجم النص", value = "متوسط")

        Spacer(modifier = Modifier.height(24.dp))

        // الصوت
        SettingsSectionTitle("الصوت")
        SettingsInfoRow(title = "سرعة القراءة", value = "١.٠×".toArabicNumerals())
        Spacer(modifier = Modifier.height(8.dp))
        SettingsInfoRow(title = "التكرار", value = "مفعل")

        Spacer(modifier = Modifier.height(24.dp))

        // الإشعارات
        SettingsSectionTitle("الإشعارات")
        SettingsInfoRow(title = "تذكير التعلّم", value = "يوميًا")
        Spacer(modifier = Modifier.height(8.dp))
        SettingsInfoRow(title = "تذكير المراجعة", value = "مفعل")

        Spacer(modifier = Modifier.height(24.dp))

        // التطبيق
        SettingsSectionTitle("التطبيق")
        SettingsInfoRow(title = "المصادر", value = "المعايير المعتمدة")
        Spacer(modifier = Modifier.height(8.dp))
        SettingsInfoRow(title = "حول الريّاش", value = stringResource(R.string.app_subtitle))
        Spacer(modifier = Modifier.height(8.dp))
        SettingsInfoRow(title = "الإصدار", value = "١.٠.٠".toArabicNumerals())

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = AlRayyashColors.GoldAccent,
        fontFamily = SulsFontFamily,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun SettingsInfoRow(title: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AlRayyashColors.SurfaceDeep)
            .border(1.dp, AlRayyashColors.SurfaceBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = AlRayyashColors.TextPrimary,
                fontFamily = SulsFontFamily,
                fontSize = 15.sp
            )
            Text(
                text = value,
                color = AlRayyashColors.TextSecondary,
                fontFamily = SulsFontFamily,
                fontSize = 14.sp
            )
        }
    }
}
