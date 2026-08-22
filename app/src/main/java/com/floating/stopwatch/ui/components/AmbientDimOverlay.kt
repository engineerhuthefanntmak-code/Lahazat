package com.floating.stopwatch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun AmbientDimOverlay(
    onExitDoubleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onExitDoubleTap()
                    }
                )
            }
    )
}
