package com.floating.stopwatch.presentation.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun HomeScreen(
    onNavigateToNovel: () -> Unit,
    onNavigateToDifferences: () -> Unit,
    onNavigateToLearn: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToAudio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlRayyashColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                color = AlRayyashColors.TextPrimary,
                fontFamily = SulsFontFamily,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.app_subtitle),
                color = AlRayyashColors.GoldAccent,
                fontFamily = SulsFontFamily,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Journey Card (بطاقة الرحلة)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AlRayyashColors.SurfaceDeep)
                .border(1.dp, AlRayyashColors.SurfaceBorder, RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "رحلتك في رواية شعبة",
                    color = AlRayyashColors.GoldAccent,
                    fontFamily = SulsFontFamily,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "أكمل من حيث توقفت",
                    color = AlRayyashColors.TextPrimary,
                    fontFamily = SulsFontFamily,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "آخر درس",
                            color = AlRayyashColors.TextSecondary,
                            fontFamily = SulsFontFamily,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "مدخل إلى الرواية",
                            color = AlRayyashColors.TextPrimary,
                            fontFamily = SulsFontFamily,
                            fontSize = 15.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AlRayyashColors.GoldSurface)
                            .border(1.dp, AlRayyashColors.GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { onNavigateToLearn() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "متابعة التعلّم",
                            color = AlRayyashColors.GoldAccent,
                            fontFamily = SulsFontFamily,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Indicator
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "التقدم العام",
                            color = AlRayyashColors.TextMuted,
                            fontFamily = SulsFontFamily,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "١٥٪".toArabicNumerals(),
                            color = AlRayyashColors.GoldAccent,
                            fontFamily = SulsFontFamily,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(AlRayyashColors.SurfaceBorder, RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.15f)
                                .height(3.dp)
                                .background(AlRayyashColors.GoldAccent, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Access Units (٤ وحدات رئيسية)
        Text(
            text = "الوحدات الرئيسية",
            color = AlRayyashColors.TextSecondary,
            fontFamily = SulsFontFamily,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            HomeUnitCard(
                title = "الرواية",
                subtitle = "أصول وفرش رواية شعبة",
                onClick = onNavigateToNovel
            )

            HomeUnitCard(
                title = "الفروق",
                subtitle = "حفص وشعبة",
                onClick = onNavigateToDifferences
            )

            HomeUnitCard(
                title = "التعلّم",
                subtitle = "منهج الإتقان",
                onClick = onNavigateToLearn
            )

            HomeUnitCard(
                title = "الاختبار",
                subtitle = "اختبر إتقانك",
                onClick = onNavigateToQuiz
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Audio Access Unit (الاستماع)
        HomeUnitCard(
            title = "الاستماع",
            subtitle = "الاستماع الصوتي المتقن",
            onClick = onNavigateToAudio,
            isAccent = true
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun HomeUnitCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isAccent: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isAccent) AlRayyashColors.GoldSurface else AlRayyashColors.SurfaceDeep)
            .border(
                1.dp,
                if (isAccent) AlRayyashColors.GoldAccent.copy(alpha = 0.4f) else AlRayyashColors.SurfaceBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = if (isAccent) AlRayyashColors.GoldAccent else AlRayyashColors.TextPrimary,
                    fontFamily = SulsFontFamily,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = AlRayyashColors.TextSecondary,
                    fontFamily = SulsFontFamily,
                    fontSize = 14.sp
                )
            }

            Text(
                text = "‹",
                color = AlRayyashColors.GoldAccent,
                fontSize = 22.sp
            )
        }
    }
}
