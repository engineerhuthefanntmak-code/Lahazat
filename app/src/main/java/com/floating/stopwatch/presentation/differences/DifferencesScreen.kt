package com.floating.stopwatch.presentation.differences

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.designsystem.theme.AlRayyashColors
import com.floating.stopwatch.designsystem.theme.SulsFontFamily

@Composable
fun DifferencesScreen() {
    val filters = listOf(
        "جميع الفروق",
        "الأصول",
        "الفرش",
        "الهمز",
        "المد",
        "الإدغام",
        "الإمالة",
        "الياءات"
    )
    var selectedFilter by remember { mutableStateOf("جميع الفروق") }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlRayyashColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "الفروق بين حفص وشعبة",
                color = AlRayyashColors.TextPrimary,
                fontFamily = SulsFontFamily,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "مقارنة دقيقة وشاملة بين الراويين عن الإمام عاصم بن أبي النجود",
                color = AlRayyashColors.TextSecondary,
                fontFamily = SulsFontFamily,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AlRayyashColors.SurfaceDeep)
                    .border(1.dp, AlRayyashColors.SurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "ابحث عن كلمة أو موضع" else searchQuery,
                    color = if (searchQuery.isEmpty()) AlRayyashColors.TextMuted else AlRayyashColors.TextPrimary,
                    fontFamily = SulsFontFamily,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Filters Horizontal Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp)
        ) {
            items(filters) { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) AlRayyashColors.GoldSurface else AlRayyashColors.SurfaceDeep)
                        .border(
                            1.dp,
                            if (isSelected) AlRayyashColors.GoldAccent else AlRayyashColors.SurfaceBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) AlRayyashColors.GoldAccent else AlRayyashColors.TextSecondary,
                        fontFamily = SulsFontFamily,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Empty state placeholder awaiting verified scholarly data
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AlRayyashColors.SurfaceDeep)
                .border(1.dp, AlRayyashColors.SurfaceBorder, RoundedCornerShape(8.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "سيتم تحميل جدول الفروق الدقيق",
                    color = AlRayyashColors.GoldAccent,
                    fontFamily = SulsFontFamily,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "المحتوى العلمي المعتمد قيد الإعداد والإدخال الدقيق",
                    color = AlRayyashColors.TextSecondary,
                    fontFamily = SulsFontFamily,
                    fontSize = 13.sp
                )
            }
        }
    }
}
