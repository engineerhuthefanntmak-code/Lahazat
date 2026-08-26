package com.floating.stopwatch.presentation.quiz

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
fun QuizScreen() {
    val quizTypes = listOf(
        "اختبار الأصول",
        "اختبار الفروق",
        "اختبار الفرش",
        "اختبار السور",
        "اختبار شامل",
        "اختبار عشوائي"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlRayyashColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "اختبر إتقانك",
            color = AlRayyashColors.TextPrimary,
            fontFamily = SulsFontFamily,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "اختبارات نوعية لتقييم الحفظ والضبط والتفرقة الدقيقة",
            color = AlRayyashColors.TextSecondary,
            fontFamily = SulsFontFamily,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            quizTypes.forEach { quizTitle ->
                QuizCard(title = quizTitle)
            }
        }
    }
}

@Composable
private fun QuizCard(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AlRayyashColors.SurfaceDeep)
            .border(1.dp, AlRayyashColors.SurfaceBorder, RoundedCornerShape(10.dp))
            .clickable { /* Quiz launcher */ }
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
                    color = AlRayyashColors.TextPrimary,
                    fontFamily = SulsFontFamily,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ابدأ الاختبار الان",
                    color = AlRayyashColors.GoldAccent,
                    fontFamily = SulsFontFamily,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "‹",
                color = AlRayyashColors.GoldAccent,
                fontSize = 20.sp
            )
        }
    }
}
