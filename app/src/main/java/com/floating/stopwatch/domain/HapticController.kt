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

    fun trigger(intensity: String, event: String) {
        if (intensity == "Off" || vibrator == null || !vibrator.hasVibrator()) return

        val amplitude = when (intensity) {
            "Light" -> 40
            "Medium" -> 110
            "Strong" -> 255
            else -> 110
        }

        val duration = when (event) {
            "Start" -> 40L
            "Stop" -> 60L
            "Lap" -> 25L
            "Reset" -> 80L
            else -> 30L
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
}
