package com.floating.stopwatch.domain

import android.media.AudioManager
import android.media.ToneGenerator

object CompletionSoundPlayer {
    private var toneGenerator: ToneGenerator? = null

    fun playCompletionClick() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
