package com.floating.stopwatch.domain

import android.os.SystemClock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

enum class IntervalStageType { WORK, REST }

data class IntervalTemplate(
    val name: String,
    val workDurationMs: Long,
    val restDurationMs: Long,
    val totalRounds: Int
)

class IntervalEngine {
    val isRunning = MutableStateFlow(false)
    val currentRound = MutableStateFlow(1)
    val totalRounds = MutableStateFlow(8)
    val currentStage = MutableStateFlow(IntervalStageType.WORK)
    val stageRemainingMs = MutableStateFlow(40000L)
    val currentWorkDurationMs = MutableStateFlow(40000L)
    val currentRestDurationMs = MutableStateFlow(20000L)

    private var job: Job? = null
    private var lastTimeMs = 0L

    companion object {
        val TEMPLATES = listOf(
            IntervalTemplate("HIIT", 40000L, 20000L, 8),
            IntervalTemplate("TABATA", 20000L, 10000L, 8),
            IntervalTemplate("POMODORO", 1500000L, 300000L, 4),
            IntervalTemplate("STUDY", 3000000L, 600000L, 1),
            IntervalTemplate("WORKOUT", 45000L, 15000L, 12)
        )
    }

    fun applyTemplate(template: IntervalTemplate) {
        pause()
        currentWorkDurationMs.value = template.workDurationMs
        currentRestDurationMs.value = template.restDurationMs
        totalRounds.value = template.totalRounds
        reset()
    }

    fun start(scope: CoroutineScope) {
        if (isRunning.value) return
        isRunning.value = true
        lastTimeMs = SystemClock.elapsedRealtime()
        job = scope.launch(Dispatchers.Main) {
            while (isActive && isRunning.value) {
                delay(50L)
                val now = SystemClock.elapsedRealtime()
                val delta = now - lastTimeMs
                lastTimeMs = now

                var rem = stageRemainingMs.value - delta
                if (rem <= 0) {
                    if (currentStage.value == IntervalStageType.WORK) {
                        currentStage.value = IntervalStageType.REST
                        rem = currentRestDurationMs.value
                    } else {
                        if (currentRound.value >= totalRounds.value) {
                            rem = 0L
                            isRunning.value = false
                            break
                        } else {
                            currentRound.value += 1
                            currentStage.value = IntervalStageType.WORK
                            rem = currentWorkDurationMs.value
                        }
                    }
                }
                stageRemainingMs.value = rem
            }
        }
    }

    fun pause() {
        isRunning.value = false
        job?.cancel()
        job = null
    }

    fun reset() {
        pause()
        currentRound.value = 1
        currentStage.value = IntervalStageType.WORK
        stageRemainingMs.value = currentWorkDurationMs.value
    }

    fun nextStage() {
        if (currentStage.value == IntervalStageType.WORK) {
            currentStage.value = IntervalStageType.REST
            stageRemainingMs.value = currentRestDurationMs.value
        } else {
            if (currentRound.value < totalRounds.value) {
                currentRound.value += 1
                currentStage.value = IntervalStageType.WORK
                stageRemainingMs.value = currentWorkDurationMs.value
            } else {
                reset()
            }
        }
    }

    fun previousStage() {
        if (currentStage.value == IntervalStageType.REST) {
            currentStage.value = IntervalStageType.WORK
            stageRemainingMs.value = currentWorkDurationMs.value
        } else if (currentRound.value > 1) {
            currentRound.value -= 1
            currentStage.value = IntervalStageType.REST
            stageRemainingMs.value = currentRestDurationMs.value
        } else {
            reset()
        }
    }
}
