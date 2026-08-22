package com.floating.stopwatch.domain

import android.media.AudioManager
import android.media.ToneGenerator

object CompletionSoundPlayer {
    private var toneGenerator: ToneGenerator? = null

    private fun getToneGen(): ToneGenerator? {
        if (toneGenerator == null) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return toneGenerator
    }

    fun playSound(soundType: String, soundEnabled: Boolean = true) {
        if (!soundEnabled) return
        try {
            val gen = getToneGen() ?: return
            when (soundType) {
                "Soft Click" -> gen.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
                "Gentle Tap" -> gen.startTone(ToneGenerator.TONE_PROP_PROMPT, 30)
                "Light Tick" -> gen.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 25)
                "Soft Knock" -> gen.startTone(ToneGenerator.TONE_SUP_PIP, 45)
                "Button Click" -> gen.startTone(ToneGenerator.TONE_PROP_ACK, 35)
                "Subtle Pop" -> gen.startTone(ToneGenerator.TONE_DTMF_1, 30)
                "Finger Snap" -> gen.startTone(ToneGenerator.TONE_CDMA_ANSWER, 25)
                "Bell" -> gen.startTone(ToneGenerator.TONE_SUP_CALL_WAITING, 75)
                "Soft Chime" -> gen.startTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE, 65)
                "Wooden Tap" -> gen.startTone(ToneGenerator.TONE_PROP_NACK, 35)
                "Paper Tap" -> gen.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 20)
                else -> gen.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playCompletionClick(soundEnabled: Boolean = true) {
        playSound("Soft Click", soundEnabled)
    }
}
