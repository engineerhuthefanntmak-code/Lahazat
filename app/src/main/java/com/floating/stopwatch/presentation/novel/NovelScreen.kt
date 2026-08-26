package com.floating.stopwatch.presentation.novel

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
fun NovelScreen() {
    val novelTopics = listOf(
        "التعريف بالرواية",
        "البسملة",
        "المد",
        "الهمز",
        "الإدغام",
        "الإمالة",
        "ياءات الإضافة",
        "الياءات الزوائد",
        "السكت",
        "فرش الحروف"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlRayyashColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "رواية الإمام شعبة",
            color = AlRayyashColors.TextPrimary,
            fontFamily = SulsFontFamily,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "الأصول والقواعد الكلية لراوي الكوفة الإمام شعبة بن عياش عن عاصم",
            color = AlRayyashColors.TextSecondary,
            fontFamily = SulsFontFamily,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            novelTopics.forEach { topic ->
                NovelTopicRow(title = topic)
            }
        }
    }
}

@Composable
private fun NovelTopicRow(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AlRayyashColors.SurfaceDeep)
            .border(1.dp, AlRayyashColors.SurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { /* Detail view navigation when content is added */ }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(AlRayyashColors.GoldAccent, RoundedCornerShape(2.dp))
                        .padding(horizontal = 2.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = title,
                    color = AlRayyashColors.TextPrimary,
                    fontFamily = SulsFontFamily,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(start = 12.dp)
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
