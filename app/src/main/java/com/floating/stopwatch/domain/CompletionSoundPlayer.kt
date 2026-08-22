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
            val gen = getToneGen()
            when (soundType) {
                "Premium Click", "Soft Click" -> gen?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
                "Mechanical Click" -> gen?.startTone(ToneGenerator.TONE_PROP_ACK, 40)
                "Soft Button Tap", "Gentle Tap" -> gen?.startTone(ToneGenerator.TONE_PROP_PROMPT, 25)
                "Fingertip Tap" -> gen?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 20)
                "Wood Knock", "Wooden Tap" -> gen?.startTone(ToneGenerator.TONE_PROP_NACK, 35)
                "Glass Tap" -> gen?.startTone(ToneGenerator.TONE_DTMF_1, 25)
                "Metal Tap" -> gen?.startTone(ToneGenerator.TONE_DTMF_2, 30)
                "Paper Tap" -> gen?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 15)
                "Light Rattle" -> gen?.startTone(ToneGenerator.TONE_SUP_PIP, 30)
                "Soft Rattle" -> gen?.startTone(ToneGenerator.TONE_SUP_PIP, 45)
                "Small Object Shake" -> gen?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 35)
                "Tiny Bell", "Bell" -> gen?.startTone(ToneGenerator.TONE_SUP_CALL_WAITING, 70)
                "Water Drop" -> gen?.startTone(ToneGenerator.TONE_DTMF_A, 25)
                "Soft Chime" -> gen?.startTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE, 60)
                "Finger Snap" -> gen?.startTone(ToneGenerator.TONE_CDMA_ANSWER, 25)
                "Gentle Clap" -> gen?.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 35)
                "Cat Meow" -> gen?.startTone(ToneGenerator.TONE_DTMF_3, 80)
                "Pigeon Coo" -> gen?.startTone(ToneGenerator.TONE_DTMF_4, 90)
                "Small Bird Chirp" -> gen?.startTone(ToneGenerator.TONE_DTMF_6, 40)
                "Nature Chirp" -> gen?.startTone(ToneGenerator.TONE_DTMF_7, 50)
                else -> gen?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playCompletionClick(soundEnabled: Boolean = true) {
        playSound("Soft Click", soundEnabled)
    }
}
