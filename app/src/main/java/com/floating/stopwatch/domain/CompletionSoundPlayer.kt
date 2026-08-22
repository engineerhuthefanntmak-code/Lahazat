package com.floating.stopwatch.domain

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin

object CompletionSoundPlayer {

    private const val SAMPLE_RATE = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playSound(soundType: String, soundEnabled: Boolean = true) {
        if (!soundEnabled) return
        scope.launch {
            try {
                val pcmSamples = generatePcm(soundType)
                if (pcmSamples.isNotEmpty()) {
                    playPcmBuffer(pcmSamples)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playCompletionClick(soundEnabled: Boolean = true) {
        playSound("Premium Click", soundEnabled)
    }

    private fun generatePcm(soundType: String): ShortArray {
        return when (soundType) {
            "Premium Click" -> synthesizeClick(frequency = 1800f, durationMs = 25, decay = 80f)
            "Mechanical Click" -> synthesizeClick(frequency = 1200f, durationMs = 35, decay = 50f)
            "Soft Button Tap" -> synthesizeClick(frequency = 800f, durationMs = 30, decay = 40f)
            "Fingertip Tap" -> synthesizeClick(frequency = 450f, durationMs = 35, decay = 35f)
            "Wood Knock" -> synthesizeResonantTone(baseFreq = 220f, durationMs = 60, decay = 30f)
            "Glass Tap" -> synthesizeResonantTone(baseFreq = 2800f, durationMs = 50, decay = 45f)
            "Metal Tap" -> synthesizeResonantTone(baseFreq = 3400f, durationMs = 65, decay = 25f)
            "Paper Tap" -> synthesizeNoiseBurst(durationMs = 20, decay = 120f)
            "Light Rattle", "Soft Rattle" -> synthesizeRattle(durationMs = 45)
            "Small Object Shake" -> synthesizeRattle(durationMs = 60)
            "Tiny Bell" -> synthesizeChime(baseFreq = 1760f, durationMs = 120)
            "Water Drop" -> synthesizeWaterDrop(durationMs = 80)
            "Soft Chime" -> synthesizeChime(baseFreq = 1046.5f, durationMs = 140)
            "Finger Snap" -> synthesizeSnap(durationMs = 35)
            "Gentle Clap" -> synthesizeSnap(durationMs = 50)
            "Cat Meow" -> synthesizeGlissando(startFreq = 550f, endFreq = 850f, durationMs = 160)
            "Pigeon Coo" -> synthesizeGlissando(startFreq = 320f, endFreq = 260f, durationMs = 180)
            "Small Bird Chirp" -> synthesizeChirp(startFreq = 2400f, midFreq = 3600f, endFreq = 2800f, durationMs = 80)
            "Nature Chirp" -> synthesizeChirp(startFreq = 1800f, midFreq = 3200f, endFreq = 2200f, durationMs = 90)
            else -> synthesizeClick(frequency = 1600f, durationMs = 25, decay = 70f)
        }
    }

    private fun synthesizeClick(frequency: Float, durationMs: Int, decay: Float): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i / SAMPLE_RATE.toFloat()
            val envelope = exp(-t * decay)
            val sample = sin(2.0 * Math.PI * frequency * t) * envelope
            buffer[i] = (sample * 24000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun synthesizeResonantTone(baseFreq: Float, durationMs: Int, decay: Float): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i / SAMPLE_RATE.toFloat()
            val envelope = exp(-t * decay)
            val harmonic1 = sin(2.0 * Math.PI * baseFreq * t)
            val harmonic2 = sin(2.0 * Math.PI * (baseFreq * 2.1f) * t) * 0.4
            val sample = (harmonic1 + harmonic2) * envelope
            buffer[i] = (sample * 20000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun synthesizeChime(baseFreq: Float, durationMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i / SAMPLE_RATE.toFloat()
            val envelope = exp(-t * 18f)
            val h1 = sin(2.0 * Math.PI * baseFreq * t)
            val h2 = sin(2.0 * Math.PI * (baseFreq * 2f) * t) * 0.5
            val h3 = sin(2.0 * Math.PI * (baseFreq * 3f) * t) * 0.25
            val sample = (h1 + h2 + h3) * envelope
            buffer[i] = (sample * 18000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun synthesizeWaterDrop(durationMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i / numSamples.toFloat()
            val freq = 600f + progress * 900f
            val t = i / SAMPLE_RATE.toFloat()
            val envelope = exp(-t * 30f)
            val sample = sin(2.0 * Math.PI * freq * t) * envelope
            buffer[i] = (sample * 22000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun synthesizeChirp(startFreq: Float, midFreq: Float, endFreq: Float, durationMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val p = i / numSamples.toFloat()
            val freq = if (p < 0.5f) {
                startFreq + (midFreq - startFreq) * (p * 2f)
            } else {
                midFreq + (endFreq - midFreq) * ((p - 0.5f) * 2f)
            }
            val t = i / SAMPLE_RATE.toFloat()
            val envelope = exp(-t * 25f)
            val sample = sin(2.0 * Math.PI * freq * t) * envelope
            buffer[i] = (sample * 20000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun synthesizeGlissando(startFreq: Float, endFreq: Float, durationMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val p = i / numSamples.toFloat()
            val freq = startFreq + (endFreq - startFreq) * p
            val t = i / SAMPLE_RATE.toFloat()
            val envelope = sin(p * Math.PI) // Smooth soft curve
            val sample = sin(2.0 * Math.PI * freq * t) * envelope
            buffer[i] = (sample * 16000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun synthesizeNoiseBurst(durationMs: Int, decay: Float): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        val rand = java.util.Random()
        for (i in 0 until numSamples) {
            val t = i / SAMPLE_RATE.toFloat()
            val envelope = exp(-t * decay)
            val noise = (rand.nextFloat() * 2f - 1f)
            val sample = noise * envelope
            buffer[i] = (sample * 15000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun synthesizeRattle(durationMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        val rand = java.util.Random()
        for (i in 0 until numSamples) {
            val t = i / SAMPLE_RATE.toFloat()
            val envelope = exp(-t * 40f)
            val rattleFreq = 1800f + sin(t * 120f) * 400f
            val sample = (sin(2.0 * Math.PI * rattleFreq * t) + rand.nextFloat() * 0.3) * envelope
            buffer[i] = (sample * 16000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun synthesizeSnap(durationMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        val rand = java.util.Random()
        for (i in 0 until numSamples) {
            val t = i / SAMPLE_RATE.toFloat()
            val envelope = exp(-t * 90f)
            val tone = sin(2.0 * Math.PI * 2200.0 * t)
            val noise = rand.nextFloat() * 2f - 1f
            val sample = (tone * 0.6 + noise * 0.4) * envelope
            buffer[i] = (sample * 22000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun playPcmBuffer(samples: ShortArray) {
        val bufferSize = samples.size * 2
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(samples, 0, samples.size)
        audioTrack.play()
    }
}
