package com.floating.stopwatch.domain

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class StopwatchEngine {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    // Use atomic reference or synchronized blocks for multi-thread safety
    private val lock = Any()

    private val _state = MutableStateFlow(StopwatchState.Ready)
    val state: StateFlow<StopwatchState> = _state.asStateFlow()

    private val _elapsedTimeMs = MutableStateFlow(0L)
    val elapsedTimeMs: StateFlow<Long> = _elapsedTimeMs.asStateFlow()

    private val _laps = MutableStateFlow<List<Lap>>(emptyList())
    val laps: StateFlow<List<Lap>> = _laps.asStateFlow()

    private var baseTime = 0L
    private var accumulatedTime = 0L

    fun start() = synchronized(lock) {
        if (_state.value == StopwatchState.Running) return

        baseTime = SystemClock.elapsedRealtime()
        _state.value = StopwatchState.Running
        startTicker()
    }

    fun pause() = synchronized(lock) {
        if (_state.value != StopwatchState.Running) return

        val now = SystemClock.elapsedRealtime()
        accumulatedTime += now - baseTime
        _elapsedTimeMs.value = accumulatedTime
        _state.value = StopwatchState.Paused
        stopTicker()
    }

    fun reset() = synchronized(lock) {
        // Resetting while Running should require pausing first or be disallowed unless paused.
        // Rule: "Reset... (should require Pause first, or confirm behavior)."
        // Let's enforce that Reset only works when in Paused state.
        if (_state.value != StopwatchState.Paused) return

        _state.value = StopwatchState.Ready
        accumulatedTime = 0L
        baseTime = 0L
        _elapsedTimeMs.value = 0L
        _laps.value = emptyList()
    }

    fun lap() = synchronized(lock) {
        if (_state.value != StopwatchState.Running) return

        val currentTotal = currentElapsed()
        val lapList = _laps.value
        val lapIndex = lapList.size + 1

        val cumulativeTimeMs = currentTotal
        val prevCumulativeTimeMs = if (lapList.isNotEmpty()) {
            lapList.last().cumulativeTimeMs
        } else {
            0L
        }
        val lapTimeMs = cumulativeTimeMs - prevCumulativeTimeMs
        val diffFromPreviousMs = if (lapList.isNotEmpty()) {
            lapTimeMs - lapList.last().lapTimeMs
        } else {
            0L // First lap delta from previous is 0, or itself depending on preferences. Let's do difference between this lap and previous lap time.
        }

        val newLap = Lap(
            lapIndex = lapIndex,
            lapTimeMs = lapTimeMs,
            cumulativeTimeMs = cumulativeTimeMs,
            diffFromPreviousMs = diffFromPreviousMs
        )
        _laps.value = lapList + newLap
    }

    private fun currentElapsed(): Long {
        if (_state.value == StopwatchState.Running) {
            return accumulatedTime + (SystemClock.elapsedRealtime() - baseTime)
        }
        return accumulatedTime
    }

    private fun startTicker() {
        job?.cancel()
        job = scope.launch {
            while (true) {
                synchronized(lock) {
                    if (_state.value == StopwatchState.Running) {
                        _elapsedTimeMs.value = currentElapsed()
                    }
                }
                // tick approximately every centisecond (10ms)
                delay(10)
            }
        }
    }

    private fun stopTicker() {
        job?.cancel()
        job = null
    }

    // Load state on process death survival
    fun restoreState(savedElapsed: Long, savedState: StopwatchState, savedLaps: List<Lap>) = synchronized(lock) {
        accumulatedTime = savedElapsed
        _elapsedTimeMs.value = savedElapsed
        _state.value = savedState
        _laps.value = savedLaps
        if (savedState == StopwatchState.Running) {
            baseTime = SystemClock.elapsedRealtime()
            startTicker()
        } else {
            stopTicker()
        }
    }
}
