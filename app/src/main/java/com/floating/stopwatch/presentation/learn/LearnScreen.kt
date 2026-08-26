package com.floating.stopwatch.presentation.learn

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.designsystem.theme.AlRayyashColors
import com.floating.stopwatch.designsystem.theme.SulsFontFamily

@Composable
fun LearnScreen() {
    val stages = listOf(
        "المرحلة الأولى — مدخل إلى الرواية",
        "المرحلة الثانية — أصول الرواية",
        "المرحلة الثالثة — القواعد التطبيقية",
        "المرحلة الرابعة — فرش الحروف",
        "المرحلة الخامسة — التطبيق على السور",
        "المرحلة السادسة — المراجعة الشاملة"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlRayyashColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "رحلتك إلى إتقان رواية شعبة",
            color = AlRayyashColors.TextPrimary,
            fontFamily = SulsFontFamily,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "منهج علمي متدرج مصمم لتحقيق الإتقان والضبط التام",
            color = AlRayyashColors.TextSecondary,
            fontFamily = SulsFontFamily,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            stages.forEachIndexed { index, stageTitle ->
                LearnStageCard(
                    stageNumber = index + 1,
                    title = stageTitle,
                    isUnlocked = index == 0
                )
            }
        }
    }
}

@Composable
private fun LearnStageCard(
    stageNumber: Int,
    title: String,
    isUnlocked: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isUnlocked) AlRayyashColors.SurfaceDeep else AlRayyashColors.BackgroundDeep)
            .border(
                1.dp,
                if (isUnlocked) AlRayyashColors.GoldAccent.copy(alpha = 0.5f) else AlRayyashColors.SurfaceBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable(enabled = isUnlocked) { }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = if (isUnlocked) AlRayyashColors.TextPrimary else AlRayyashColors.TextMuted,
                    fontFamily = SulsFontFamily,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isUnlocked) "جارٍ التعلّم — اضغط للمتابعة" else "مغلق حتى إتمام المرحلة السابقة",
                    color = if (isUnlocked) AlRayyashColors.GoldAccent else AlRayyashColors.TextMuted,
                    fontFamily = SulsFontFamily,
                    fontSize = 13.sp
                )
            }

            Text(
                text = if (isUnlocked) "‹" else "•",
                color = if (isUnlocked) AlRayyashColors.GoldAccent else AlRayyashColors.TextMuted,
                fontSize = 20.sp
            )
        }
    }
}
