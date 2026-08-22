package com.floating.stopwatch.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LegacyStatus {
    ON_PACE, AHEAD, BEHIND
}

enum class LegacyState {
    IDLE, RUNNING, PAUSED, COMPLETED
}

data class Legacy(
    val id: String,
    val name: String,
    val targetDurationMs: Long,
    val totalDays: Int,
    val dailyTargetMs: Long,
    val startDateMs: Long,
    val targetDateMs: Long,
    val accumulatedTimeMs: Long = 0L,
    val manualTimeMs: Long = 0L,
    val postponedDays: Int = 0,
    val isCompleted: Boolean = false,
    val dailyProgressMap: Map<String, Long> = emptyMap()
) {
    val completedTimeMs: Long
        get() = accumulatedTimeMs

    val remainingTimeMs: Long
        get() = (targetDurationMs - completedTimeMs).coerceAtLeast(0L)

    val progressPercentage: Float
        get() = if (targetDurationMs > 0) {
            ((completedTimeMs.toDouble() / targetDurationMs.toDouble()) * 100).coerceIn(0.0, 100.0).toFloat()
        } else 0f

    val totalTargetDays: Int
        get() = totalDays + postponedDays

    fun getRemainingDays(nowMs: Long = System.currentTimeMillis()): Int {
        val diffMs = targetDateMs - nowMs
        return if (diffMs > 0) {
            (diffMs / (1000 * 60 * 60 * 24)).toInt() + 1
        } else 0
    }

    fun getTodayCompletedMs(nowMs: Long = System.currentTimeMillis()): Long {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(nowMs))
        return dailyProgressMap[dateStr] ?: 0L
    }

    fun getStatus(nowMs: Long = System.currentTimeMillis()): LegacyStatus {
        if (completedTimeMs >= targetDurationMs) {
            return LegacyStatus.AHEAD
        }
        val elapsedDaysSinceStart = ((nowMs - startDateMs).coerceAtLeast(0L) / (1000 * 60 * 60 * 24)).toInt() + 1
        val daysConstrained = elapsedDaysSinceStart.coerceAtMost(totalTargetDays)
        val expectedTimeMs = daysConstrained * dailyTargetMs

        // Margin of 15 minutes to be considered ON_PACE
        val marginMs = 15 * 60 * 1000L
        return when {
            completedTimeMs > expectedTimeMs + marginMs -> LegacyStatus.AHEAD
            completedTimeMs < expectedTimeMs - marginMs -> LegacyStatus.BEHIND
            else -> LegacyStatus.ON_PACE
        }
    }
}
