package com.floating.stopwatch.domain

import kotlinx.coroutines.flow.MutableStateFlow

data class TimelineEvent(
    val timestampMs: Long,
    val label: String
)

class SessionEngine {
    val isRunning = MutableStateFlow(false)
    val sessionName = MutableStateFlow("WORKOUT")
    val totalDurationMs = MutableStateFlow(0L)
    val timeline = MutableStateFlow<List<TimelineEvent>>(emptyList())

    fun addEvent(label: String) {
        val newEvent = TimelineEvent(System.currentTimeMillis(), label)
        timeline.value = timeline.value + newEvent
    }

    fun start() {
        isRunning.value = true
        addEvent("SESSION START")
    }

    fun pause() {
        isRunning.value = false
        addEvent("SESSION PAUSE")
    }

    fun complete() {
        isRunning.value = false
        addEvent("SESSION COMPLETE")
    }

    fun reset() {
        isRunning.value = false
        totalDurationMs.value = 0L
        timeline.value = emptyList()
    }
}
