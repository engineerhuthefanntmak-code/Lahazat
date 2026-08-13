package com.floating.stopwatch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.layout.FoldingFeature
import com.floating.stopwatch.ui.theme.LuxuryColors

@Composable
fun AdaptiveLayout(
    foldingFeature: FoldingFeature?,
    isUnfoldedFlat: Boolean,
    mainContent: @Composable () -> Unit,
    sideContent: @Composable () -> Unit
) {
    if (foldingFeature != null) {
        val isTabletop = foldingFeature.state == FoldingFeature.State.HALF_OPENED &&
                foldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL

        if (isTabletop) {
            // Keep primary content on the top half screen panel
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    mainContent()
                }
                Spacer(
                    modifier = Modifier
                        .height(2.dp)
                        .fillMaxWidth()
                        .background(LuxuryColors.WarmGray)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    sideContent()
                }
            }
        } else {
            // Two panel side-by-side split screen
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                ) {
                    mainContent()
                }
                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(LuxuryColors.WarmGray)
                )
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight()
                ) {
                    sideContent()
                }
            }
        }
    } else {
        if (isUnfoldedFlat) {
            // Unfolded flat large landscape/tablet display posture layout
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                ) {
                    mainContent()
                }
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight()
                ) {
                    sideContent()
                }
            }
        } else {
            // Traditional Portrait posture layout
            Box(modifier = Modifier.fillMaxSize()) {
                mainContent()
            }
        }
    }
}
