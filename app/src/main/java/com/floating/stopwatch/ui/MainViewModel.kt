package com.floating.stopwatch.ui

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.floating.stopwatch.domain.Lap
import com.floating.stopwatch.domain.StopwatchEngine
import com.floating.stopwatch.domain.StopwatchState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class AppMode {
    Stopwatch, Countdown, Counter
}

class MainViewModel(
    val engine: StopwatchEngine
) : ViewModel() {

    val currentMode = MutableStateFlow(AppMode.Stopwatch)

    val state: StateFlow<StopwatchState> = engine.state
    val elapsedTimeMs: StateFlow<Long> = engine.elapsedTimeMs
    val laps: StateFlow<List<Lap>> = engine.laps

    // Countdown State
    val countdownInitialMs = MutableStateFlow(300000L) // Default 5 mins
    val countdownRemainingMs = MutableStateFlow(300000L)
    val isCountdownRunning = MutableStateFlow(false)
    private var countdownJob: Job? = null

    // Counter State
    val counterValue = MutableStateFlow(0L)

    fun cycleMode() {
        val nextMode = when (currentMode.value) {
            AppMode.Stopwatch -> AppMode.Countdown
            AppMode.Countdown -> AppMode.Counter
            AppMode.Counter -> AppMode.Stopwatch
        }
        currentMode.value = nextMode
    }

    // Countdown Time Adjustment via Drag
    fun adjustCountdownHours(deltaHours: Int) {
        if (isCountdownRunning.value) return
        val currentMs = countdownInitialMs.value
        val hours = currentMs / 3600000L
        val rest = currentMs % 3600000L
        val newHours = (hours + deltaHours).coerceIn(0L, 99L)
        val newTotal = newHours * 3600000L + rest
        countdownInitialMs.value = newTotal
        countdownRemainingMs.value = newTotal
    }

    fun adjustCountdownMinutes(deltaMinutes: Int) {
        if (isCountdownRunning.value) return
        val currentMs = countdownInitialMs.value
        val hours = currentMs / 3600000L
        val minutes = (currentMs % 3600000L) / 60000L
        val seconds = currentMs % 60000L
        val newMinutes = (minutes + deltaMinutes).coerceIn(0L, 59L)
        val newTotal = hours * 3600000L + newMinutes * 60000L + seconds
        countdownInitialMs.value = newTotal
        countdownRemainingMs.value = newTotal
    }

    fun adjustCountdownSeconds(deltaSeconds: Int) {
        if (isCountdownRunning.value) return
        val currentMs = countdownInitialMs.value
        val hours = currentMs / 3600000L
        val minutes = (currentMs % 3600000L) / 60000L
        val seconds = (currentMs % 60000L) / 1000L
        val newSeconds = (seconds + deltaSeconds).coerceIn(0L, 59L)
        val newTotal = hours * 3600000L + minutes * 60000L + newSeconds * 1000L
        countdownInitialMs.value = newTotal
        countdownRemainingMs.value = newTotal
    }

    fun startCountdown() {
        if (countdownRemainingMs.value <= 0L) return
        isCountdownRunning.value = true
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch(Dispatchers.Default) {
            var lastBase = SystemClock.elapsedRealtime()
            while (isCountdownRunning.value) {
                delay(10)
                val now = SystemClock.elapsedRealtime()
                val delta = now - lastBase
                lastBase = now
                val current = countdownRemainingMs.value - delta
                if (current <= 0L) {
                    countdownRemainingMs.value = 0L
                    isCountdownRunning.value = false
                    break
                } else {
                    countdownRemainingMs.value = current
                }
            }
        }
    }

    fun pauseCountdown() {
        isCountdownRunning.value = false
        countdownJob?.cancel()
    }

    fun resetCountdown() {
        pauseCountdown()
        countdownRemainingMs.value = countdownInitialMs.value
    }

    fun incrementCounter() {
        counterValue.value += 1
    }

    fun decrementCounter() {
        if (counterValue.value > 0) {
            counterValue.value -= 1
        }
    }

    fun resetCounter() {
        counterValue.value = 0
    }

    fun start() {
        engine.start()
    }

    fun pause() {
        engine.pause()
    }

    fun reset() {
        engine.reset()
    }

    fun lap() {
        engine.lap()
    }
}
