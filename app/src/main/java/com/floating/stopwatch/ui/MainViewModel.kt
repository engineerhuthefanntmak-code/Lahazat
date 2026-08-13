package com.floating.stopwatch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.floating.stopwatch.domain.Lap
import com.floating.stopwatch.domain.StopwatchEngine
import com.floating.stopwatch.domain.StopwatchState
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    val engine: StopwatchEngine
) : ViewModel() {

    val state: StateFlow<StopwatchState> = engine.state
    val elapsedTimeMs: StateFlow<Long> = engine.elapsedTimeMs
    val laps: StateFlow<List<Lap>> = engine.laps

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
