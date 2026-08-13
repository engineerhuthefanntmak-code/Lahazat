package com.floating.stopwatch.domain

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticController(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Sound soundless design: triggering custom soundless tones via ToneGenerator or basic haptic clicks
    fun trigger(intensity: String, event: String) {
        if (intensity == "Off" || vibrator == null || !vibrator.hasVibrator()) return

        val amplitude = when (intensity) {
            "Light" -> 40
            "Medium" -> 110
            "Strong" -> 255
            else -> 110
        }

        val duration = when (event) {
            "Start" -> 45L
            "Stop" -> 70L
            "Lap" -> 30L
            "Reset" -> 90L
            else -> 35L
        }

        // Trigger premium custom vibration patterns for distinct premium tactile weights
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (event) {
                "Start" -> {
                    // Double short tap pattern
                    val wave = VibrationEffect.createWaveform(longArrayOf(0, 15, 30, 25), intArrayOf(0, amplitude, 0, amplitude), -1)
                    vibrator.vibrate(wave)
                }
                "Stop" -> {
                    // Single heavy tap
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                }
                "Lap" -> {
                    // Ultra light crisp tap
                    vibrator.vibrate(VibrationEffect.createOneShot(15, (amplitude * 0.7f).toInt().coerceIn(1, 255)))
                }
                "Reset" -> {
                    // Long fade out vibration pattern
                    val wave = VibrationEffect.createWaveform(longArrayOf(0, 40, 20, 20, 10, 10), intArrayOf(0, amplitude, 0, (amplitude * 0.5f).toInt(), 0, (amplitude * 0.2f).toInt()), -1)
                    vibrator.vibrate(wave)
                }
                else -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
}
