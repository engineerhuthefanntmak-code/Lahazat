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
    val name: String,
    val stages: List<IntervalStage>,
    val repetitions: Int
)

class IntervalEngine {
    private val _state = MutableStateFlow(IntervalState.IDLE)
    val state: StateFlow<IntervalState> = _state.asStateFlow()

    private val _activeTemplate = MutableStateFlow<IntervalTemplate?>(null)
    val activeTemplate: StateFlow<IntervalTemplate?> = _activeTemplate.asStateFlow()

    private val _currentStageIndex = MutableStateFlow(0)
    val currentStageIndex: StateFlow<Int> = _currentStageIndex.asStateFlow()

    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    private val _stageRemainingMs = MutableStateFlow(0L)
    val stageRemainingMs: StateFlow<Long> = _stageRemainingMs.asStateFlow()

    private var job: Job? = null
    private var lastTimeMs = 0L

    companion object {
        val BUILT_IN_TEMPLATES = listOf(
            IntervalTemplate(
                id = "hiit",
                name = "HIIT",
                stages = listOf(
                    IntervalStage("s1", "WORK", 40000L, 0, IntervalStageType.WORK),
                    IntervalStage("s2", "REST", 20000L, 1, IntervalStageType.REST)
                ),
                repetitions = 8
            ),
            IntervalTemplate(
                id = "tabata",
                name = "TABATA",
                stages = listOf(
                    IntervalStage("s1", "WORK", 20000L, 0, IntervalStageType.WORK),
                    IntervalStage("s2", "REST", 10000L, 1, IntervalStageType.REST)
                ),
                repetitions = 8
            ),
            IntervalTemplate(
                id = "pomodoro",
                name = "POMODORO",
                stages = listOf(
                    IntervalStage("s1", "WORK", 1500000L, 0, IntervalStageType.WORK),
                    IntervalStage("s2", "REST", 300000L, 1, IntervalStageType.REST)
                ),
                repetitions = 4
            ),
            IntervalTemplate(
                id = "study",
                name = "STUDY",
                stages = listOf(
                    IntervalStage("s1", "WORK", 3000000L, 0, IntervalStageType.WORK),
                    IntervalStage("s2", "REST", 600000L, 1, IntervalStageType.REST)
                ),
                repetitions = 4
            ),
            IntervalTemplate(
                id = "workout",
                name = "WORKOUT",
                stages = listOf(
                    IntervalStage("s1", "WORK", 45000L, 0, IntervalStageType.WORK),
                    IntervalStage("s2", "REST", 15000L, 1, IntervalStageType.REST)
                ),
                repetitions = 12
            )
        )
    }

    init {
        loadTemplate(BUILT_IN_TEMPLATES[0])
    }

    fun loadTemplate(template: IntervalTemplate) {
        pause()
        _activeTemplate.value = template
        _currentStageIndex.value = 0
        _currentRound.value = 1
        val firstStageDuration = template.stages.firstOrNull()?.durationMs ?: 0L
        _stageRemainingMs.value = firstStageDuration
        _state.value = IntervalState.IDLE
    }

    fun start(scope: CoroutineScope) {
        val template = _activeTemplate.value ?: return
        if (template.stages.isEmpty()) return

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
                    val currentStages = _activeTemplate.value?.stages ?: emptyList()
                    val nextIdx = _currentStageIndex.value + 1
                    if (nextIdx < currentStages.size) {
                        _currentStageIndex.value = nextIdx
                        _stageRemainingMs.value = currentStages[nextIdx].durationMs
                    } else {
                        // Round finished
                        val nextRound = _currentRound.value + 1
                        val maxRounds = _activeTemplate.value?.repetitions ?: 1
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
        val firstStageDuration = _activeTemplate.value?.stages?.firstOrNull()?.durationMs ?: 0L
        _stageRemainingMs.value = firstStageDuration
        _state.value = IntervalState.IDLE
    }

    fun stop() {
        pause()
        _state.value = IntervalState.CANCELLED
    }

    fun getCurrentStage(): IntervalStage? {
        val stages = _activeTemplate.value?.stages ?: return null
        val idx = _currentStageIndex.value
        return if (idx in stages.indices) stages[idx] else null
    }

    fun getNextStage(): IntervalStage? {
        val stages = _activeTemplate.value?.stages ?: return null
        val idx = _currentStageIndex.value + 1
        if (idx in stages.indices) return stages[idx]
        // Next round's first stage
        if (_currentRound.value < (_activeTemplate.value?.repetitions ?: 1)) {
            return stages.firstOrNull()
        }
        return null
    }
}
