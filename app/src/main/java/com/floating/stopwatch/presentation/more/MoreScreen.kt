package com.floating.stopwatch.presentation.more

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
fun MoreScreen(
    onNavigateToSettings: () -> Unit
) {
    val items = listOf(
        "الكتاب" to { },
        "المحفوظات" to { },
        "المراجعة اليومية" to { },
        "التقدم" to { },
        "البحث" to { },
        "الإعدادات" to onNavigateToSettings,
        "حول الريّاش" to { }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlRayyashColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "المزيد",
            color = AlRayyashColors.TextPrimary,
            fontFamily = SulsFontFamily,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "الأدوات والإعدادات والمراجع العلمية المساندة",
            color = AlRayyashColors.TextSecondary,
            fontFamily = SulsFontFamily,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { (title, onClick) ->
                MoreMenuItem(title = title, onClick = onClick)
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AlRayyashColors.SurfaceDeep)
            .border(1.dp, AlRayyashColors.SurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp)
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
                fontSize = 16.sp
            )

            Text(
                text = "‹",
                color = AlRayyashColors.GoldAccent,
                fontSize = 20.sp
            )
        }
    }
}
