package com.floating.stopwatch.domain

import android.os.SystemClock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LegacyEngine {
    private val _legacies = MutableStateFlow<List<Legacy>>(emptyList())
    val legacies: StateFlow<List<Legacy>> = _legacies.asStateFlow()

    private val _selectedLegacyId = MutableStateFlow<String?>(null)
    val selectedLegacyId: StateFlow<String?> = _selectedLegacyId.asStateFlow()

    private val _activeLegacy = MutableStateFlow<Legacy?>(null)
    val activeLegacy: StateFlow<Legacy?> = _activeLegacy.asStateFlow()

    private val _state = MutableStateFlow(LegacyState.IDLE)
    val state: StateFlow<LegacyState> = _state.asStateFlow()

    var onLegacyDataChanged: ((List<Legacy>, String?) -> Unit)? = null

    private var job: Job? = null
    private var lastTimeMs = 0L

    fun loadLegacies(legacies: List<Legacy>, selectedId: String?) {
        _legacies.value = legacies
        val targetId = selectedId ?: legacies.firstOrNull()?.id
        _selectedLegacyId.value = targetId
        _activeLegacy.value = legacies.find { it.id == targetId }
        if (_activeLegacy.value == null && legacies.isNotEmpty()) {
            _selectedLegacyId.value = legacies.first().id
            _activeLegacy.value = legacies.first()
        }
    }

    fun selectLegacy(id: String) {
        if (_state.value == LegacyState.RUNNING) {
            pause()
        }
        _selectedLegacyId.value = id
        _activeLegacy.value = _legacies.value.find { it.id == id }
        _state.value = LegacyState.IDLE
        notifyChange()
    }

    fun createLegacy(
        name: String,
        targetDurationMs: Long,
        totalDays: Int,
        dailyTargetMs: Long,
        targetDateMs: Long
    ): Legacy {
        val now = System.currentTimeMillis()
        val newLegacy = Legacy(
            id = "legacy_${now}_${(1000..9999).random()}",
            name = name,
            targetDurationMs = targetDurationMs,
            totalDays = totalDays,
            dailyTargetMs = dailyTargetMs,
            startDateMs = now,
            targetDateMs = targetDateMs
        )
        val updatedList = _legacies.value + newLegacy
        _legacies.value = updatedList
        _selectedLegacyId.value = newLegacy.id
        _activeLegacy.value = newLegacy
        _state.value = LegacyState.IDLE
        notifyChange()
        return newLegacy
    }

    fun deleteLegacy(id: String) {
        if (_selectedLegacyId.value == id && _state.value == LegacyState.RUNNING) {
            pause()
        }
        val updatedList = _legacies.value.filter { it.id != id }
        _legacies.value = updatedList
        if (_selectedLegacyId.value == id) {
            val newSelected = updatedList.firstOrNull()
            _selectedLegacyId.value = newSelected?.id
            _activeLegacy.value = newSelected
        }
        _state.value = LegacyState.IDLE
        notifyChange()
    }

    fun start(scope: CoroutineScope) {
        val current = _activeLegacy.value ?: return
        if (current.isCompleted) return

        _state.value = LegacyState.RUNNING
        lastTimeMs = SystemClock.elapsedRealtime()

        job?.cancel()
        job = scope.launch(Dispatchers.Main) {
            while (isActive && _state.value == LegacyState.RUNNING) {
                delay(100L)
                val nowRealtime = SystemClock.elapsedRealtime()
                val delta = nowRealtime - lastTimeMs
                lastTimeMs = nowRealtime

                if (delta > 0) {
                    addElapsedTime(delta)
                }
            }
        }
    }

    private fun addElapsedTime(deltaMs: Long) {
        val current = _activeLegacy.value ?: return
        val newAcc = current.accumulatedTimeMs + deltaMs
        val isCompletedNow = newAcc >= current.targetDurationMs

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val currentTodayMs = current.dailyProgressMap[todayStr] ?: 0L
        val updatedMap = current.dailyProgressMap.toMutableMap()
        updatedMap[todayStr] = currentTodayMs + deltaMs

        val updatedLegacy = current.copy(
            accumulatedTimeMs = newAcc,
            isCompleted = isCompletedNow,
            dailyProgressMap = updatedMap
        )

        updateLegacyInList(updatedLegacy)

        if (isCompletedNow) {
            _state.value = LegacyState.COMPLETED
            job?.cancel()
            job = null
        }
    }

    fun pause() {
        if (_state.value == LegacyState.RUNNING) {
            _state.value = LegacyState.PAUSED
        }
        job?.cancel()
        job = null
        notifyChange()
    }

    fun resume(scope: CoroutineScope) {
        start(scope)
    }

    fun addManualTime(hours: Int, minutes: Int) {
        val current = _activeLegacy.value ?: return
        val extraMs = (hours * 3600L + minutes * 60L) * 1000L
        if (extraMs <= 0) return

        val newAcc = current.accumulatedTimeMs + extraMs
        val newManual = current.manualTimeMs + extraMs
        val isCompletedNow = newAcc >= current.targetDurationMs

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val currentTodayMs = current.dailyProgressMap[todayStr] ?: 0L
        val updatedMap = current.dailyProgressMap.toMutableMap()
        updatedMap[todayStr] = currentTodayMs + extraMs

        val updatedLegacy = current.copy(
            accumulatedTimeMs = newAcc,
            manualTimeMs = newManual,
            isCompleted = isCompletedNow,
            dailyProgressMap = updatedMap
        )

        updateLegacyInList(updatedLegacy)
        notifyChange()
    }

    fun postpone(extraDays: Int) {
        val current = _activeLegacy.value ?: return
        if (extraDays <= 0) return

        val extraMs = extraDays * 24 * 3600 * 1000L
        val updatedLegacy = current.copy(
            targetDateMs = current.targetDateMs + extraMs,
            postponedDays = current.postponedDays + extraDays
        )

        updateLegacyInList(updatedLegacy)
        notifyChange()
    }

    private fun updateLegacyInList(updated: Legacy) {
        _activeLegacy.value = updated
        _legacies.value = _legacies.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    private fun notifyChange() {
        onLegacyDataChanged?.invoke(_legacies.value, _selectedLegacyId.value)
    }
}
