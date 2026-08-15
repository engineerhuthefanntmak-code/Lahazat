package com.floating.stopwatch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.floating.stopwatch.domain.Lap
import com.floating.stopwatch.domain.StopwatchEngine
import com.floating.stopwatch.domain.StopwatchState
import kotlinx.coroutines.flow.StateFlow

enum class AppMode {
    Stopwatch, Countdown, Counter
}

class MainViewModel(
    val engine: StopwatchEngine
) : ViewModel() {

    val currentMode = kotlinx.coroutines.flow.MutableStateFlow(AppMode.Stopwatch)

    val state: StateFlow<StopwatchState> = engine.state
    val elapsedTimeMs: StateFlow<Long> = engine.elapsedTimeMs
    val laps: StateFlow<List<Lap>> = engine.laps

    val countdownRemainingMs = kotlinx.coroutines.flow.MutableStateFlow(300000L) // 5 mins
    val counterValue = kotlinx.coroutines.flow.MutableStateFlow(0L)

    fun cycleMode() {
        val nextMode = when (currentMode.value) {
            AppMode.Stopwatch -> AppMode.Countdown
            AppMode.Countdown -> AppMode.Counter
            AppMode.Counter -> AppMode.Stopwatch
        }
        currentMode.value = nextMode
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
