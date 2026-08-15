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

class CountdownEngine {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null
    private val lock = Any()

    private val _initialDurationMs = MutableStateFlow(300000L) // Default 5 mins
    val initialDurationMs: StateFlow<Long> = _initialDurationMs.asStateFlow()

    private val _remainingTimeMs = MutableStateFlow(300000L)
    val remainingTimeMs: StateFlow<Long> = _remainingTimeMs.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var baseTime = 0L

    fun setDuration(durationMs: Long) = synchronized(lock) {
        if (_isRunning.value) return
        val clamped = durationMs.coerceAtLeast(1000L)
        _initialDurationMs.value = clamped
        _remainingTimeMs.value = clamped
    }

    fun adjustDuration(deltaMs: Long) = synchronized(lock) {
        if (_isRunning.value) return
        val current = _initialDurationMs.value
        val newDuration = (current + deltaMs).coerceAtLeast(1000L)
        _initialDurationMs.value = newDuration
        _remainingTimeMs.value = newDuration
    }

    fun start() = synchronized(lock) {
        if (_isRunning.value || _remainingTimeMs.value <= 0L) return
        baseTime = SystemClock.elapsedRealtime()
        _isRunning.value = true
        startTicker()
    }

    fun pause() = synchronized(lock) {
        if (!_isRunning.value) return
        val now = SystemClock.elapsedRealtime()
        val delta = now - baseTime
        val current = (_remainingTimeMs.value - delta).coerceAtLeast(0L)
        _remainingTimeMs.value = current
        _isRunning.value = false
        stopTicker()
    }

    fun reset() = synchronized(lock) {
        pause()
        _remainingTimeMs.value = _initialDurationMs.value
    }

    private fun startTicker() {
        job?.cancel()
        job = scope.launch {
            var lastBase = SystemClock.elapsedRealtime()
            while (_isRunning.value) {
                delay(10)
                synchronized(lock) {
                    if (_isRunning.value) {
                        val now = SystemClock.elapsedRealtime()
                        val delta = now - lastBase
                        lastBase = now
                        val current = _remainingTimeMs.value - delta
                        if (current <= 0L) {
                            _remainingTimeMs.value = 0L
                            _isRunning.value = false
                            stopTicker()
                        } else {
                            _remainingTimeMs.value = current
                        }
                    }
                }
            }
        }
    }

    private fun stopTicker() {
        job?.cancel()
        job = null
    }
}
