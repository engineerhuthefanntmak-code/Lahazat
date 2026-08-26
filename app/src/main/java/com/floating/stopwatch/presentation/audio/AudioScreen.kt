package com.floating.stopwatch.presentation.audio

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.stopwatch.core.utils.toArabicNumerals
import com.floating.stopwatch.designsystem.theme.AlRayyashColors
import com.floating.stopwatch.designsystem.theme.SulsFontFamily

@Composable
fun AudioScreen() {
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlRayyashColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "الاستماع",
            color = AlRayyashColors.TextPrimary,
            fontFamily = SulsFontFamily,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "استمع للرواية بأداء صوتي فاخر ومتقن",
            color = AlRayyashColors.TextSecondary,
            fontFamily = SulsFontFamily,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Currently selected audio position card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AlRayyashColors.SurfaceDeep)
                .border(1.dp, AlRayyashColors.SurfaceBorder, RoundedCornerShape(12.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "سورة الفاتحة",
                    color = AlRayyashColors.GoldAccent,
                    fontFamily = SulsFontFamily,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "الآية: ١".toArabicNumerals(),
                    color = AlRayyashColors.TextPrimary,
                    fontFamily = SulsFontFamily,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "الموضع: بداية السورة",
                    color = AlRayyashColors.TextSecondary,
                    fontFamily = SulsFontFamily,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Audio Player Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AudioControlItem(title = "تكرار") { }
                    AudioControlItem(title = "السابق") { }

                    // Main Play / Pause Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(AlRayyashColors.GoldSurface)
                            .border(1.dp, AlRayyashColors.GoldAccent, RoundedCornerShape(30.dp))
                            .clickable { isPlaying = !isPlaying }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                            color = AlRayyashColors.GoldAccent,
                            fontFamily = SulsFontFamily,
                            fontSize = 16.sp
                        )
                    }

                    AudioControlItem(title = "التالي") { }
                }
            }
        }
    }
}

@Composable
private fun AudioControlItem(
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = AlRayyashColors.TextSecondary,
            fontFamily = SulsFontFamily,
            fontSize = 14.sp
        )
    }
}
