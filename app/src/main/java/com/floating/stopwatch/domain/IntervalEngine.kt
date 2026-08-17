package com.floating.stopwatch.domain

import android.os.SystemClock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class IntervalState {
    IDLE, RUNNING, PAUSED, COMPLETED, CANCELLED
}

enum class IntervalStageType { WORK, REST }

data class IntervalStage(
    val id: String,
    val name: String,
    val durationMs: Long,
    val order: Int,
    val type: IntervalStageType = IntervalStageType.WORK
)

data class IntervalTemplate(
    val id: String,
    val name: String, // Default = "HIT"
    val workDurationMs: Long = 40000L,
    val restDurationMs: Long = 20000L,
    val repetitions: Int = 8
) {
    val stages: List<IntervalStage>
        get() = listOf(
            IntervalStage("s1", "WORK", workDurationMs, 0, IntervalStageType.WORK),
            IntervalStage("s2", "REST", restDurationMs, 1, IntervalStageType.REST)
        )
}

class IntervalEngine {
    private val _state = MutableStateFlow(IntervalState.IDLE)
    val state: StateFlow<IntervalState> = _state.asStateFlow()

    private val _activeTemplate = MutableStateFlow(
        IntervalTemplate(
            id = "default_hit",
            name = "HIT",
            workDurationMs = 40000L,
            restDurationMs = 20000L,
            repetitions = 8
        )
    )
    val activeTemplate: StateFlow<IntervalTemplate> = _activeTemplate.asStateFlow()

    private val _currentStageIndex = MutableStateFlow(0)
    val currentStageIndex: StateFlow<Int> = _currentStageIndex.asStateFlow()

    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    private val _stageRemainingMs = MutableStateFlow(40000L)
    val stageRemainingMs: StateFlow<Long> = _stageRemainingMs.asStateFlow()

    private var job: Job? = null
    private var lastTimeMs = 0L

    fun loadTemplate(template: IntervalTemplate) {
        val isActiveRun = _state.value == IntervalState.RUNNING || _state.value == IntervalState.PAUSED
        _activeTemplate.value = template

        // If running or paused, update template configuration for subsequent rounds/stages without interrupting active stage timer
        if (isActiveRun) {
            return
        }

        pause()
        _currentStageIndex.value = 0
        _currentRound.value = 1
        _stageRemainingMs.value = template.workDurationMs
        _state.value = IntervalState.IDLE
    }

    fun renameTemplate(newName: String) {
        val current = _activeTemplate.value
        _activeTemplate.value = current.copy(name = newName)
    }

    fun updateDurations(workMs: Long, restMs: Long, reps: Int) {
        val current = _activeTemplate.value
        val updated = current.copy(workDurationMs = workMs, restDurationMs = restMs, repetitions = reps)
        _activeTemplate.value = updated
        if (_state.value == IntervalState.IDLE) {
            _stageRemainingMs.value = if (_currentStageIndex.value == 0) workMs else restMs
        }
    }

    fun start(scope: CoroutineScope) {
        val template = _activeTemplate.value
        val stages = template.stages
        if (stages.isEmpty()) return

        if (_state.value == IntervalState.COMPLETED || _state.value == IntervalState.CANCELLED) {
            reset()
        }

        _state.value = IntervalState.RUNNING
        lastTimeMs = SystemClock.elapsedRealtime()

        job?.cancel()
        job = scope.launch(Dispatchers.Main) {
            while (isActive && _state.value == IntervalState.RUNNING) {
                delay(50L)
                val now = SystemClock.elapsedRealtime()
                val delta = now - lastTimeMs
                lastTimeMs = now

                var rem = _stageRemainingMs.value - delta
                if (rem <= 0) {
                    val currentStages = _activeTemplate.value.stages
                    val nextIdx = _currentStageIndex.value + 1
                    if (nextIdx < currentStages.size) {
                        _currentStageIndex.value = nextIdx
                        _stageRemainingMs.value = currentStages[nextIdx].durationMs
                    } else {
                        // Round finished
                        val nextRound = _currentRound.value + 1
                        val maxRounds = _activeTemplate.value.repetitions
                        if (nextRound <= maxRounds) {
                            _currentRound.value = nextRound
                            _currentStageIndex.value = 0
                            _stageRemainingMs.value = currentStages.firstOrNull()?.durationMs ?: 0L
                        } else {
                            // Completed
                            _stageRemainingMs.value = 0L
                            _state.value = IntervalState.COMPLETED
                            break
                        }
                    }
                } else {
                    _stageRemainingMs.value = rem
                }
            }
        }
    }

    fun pause() {
        if (_state.value == IntervalState.RUNNING) {
            _state.value = IntervalState.PAUSED
        }
        job?.cancel()
        job = null
    }

    fun reset() {
        pause()
        _currentStageIndex.value = 0
        _currentRound.value = 1
        _stageRemainingMs.value = _activeTemplate.value.workDurationMs
        _state.value = IntervalState.IDLE
    }

    fun stop() {
        pause()
        _state.value = IntervalState.CANCELLED
    }

    fun getCurrentStage(): IntervalStage? {
        val stages = _activeTemplate.value.stages
        val idx = _currentStageIndex.value
        return if (idx in stages.indices) stages[idx] else null
    }

    fun getNextStage(): IntervalStage? {
        val stages = _activeTemplate.value.stages
        val idx = _currentStageIndex.value + 1
        if (idx in stages.indices) return stages[idx]
        if (_currentRound.value < _activeTemplate.value.repetitions) {
            return stages.firstOrNull()
        }
        return null
    }
}
