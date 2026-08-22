package com.floating.stopwatch.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LegacyEngineTest {

    private lateinit var engine: LegacyEngine

    @Before
    fun setUp() {
        engine = LegacyEngine()
    }

    @Test
    fun testInitialState() {
        assertTrue(engine.legacies.value.isEmpty())
        assertNull(engine.selectedLegacyId.value)
        assertNull(engine.activeLegacy.value)
        assertEquals(LegacyState.IDLE, engine.state.value)
    }

    @Test
    fun testCreateLegacy() {
        val now = System.currentTimeMillis()
        val legacy = engine.createLegacy(
            name = "Read Books",
            targetDurationMs = 100 * 3600 * 1000L,
            totalDays = 30,
            dailyTargetMs = (100 * 3600 * 1000L) / 30,
            targetDateMs = now + (30L * 24 * 3600 * 1000L)
        )

        assertEquals(1, engine.legacies.value.size)
        assertEquals(legacy.id, engine.selectedLegacyId.value)
        assertNotNull(engine.activeLegacy.value)
        assertEquals("Read Books", engine.activeLegacy.value?.name)
        assertEquals(0L, legacy.completedTimeMs)
        assertEquals(100 * 3600 * 1000L, legacy.remainingTimeMs)
        assertEquals(0f, legacy.progressPercentage, 0.01f)
    }

    @Test
    fun testAddManualTime() {
        engine.createLegacy(
            name = "Workout",
            targetDurationMs = 10 * 3600 * 1000L,
            totalDays = 10,
            dailyTargetMs = 3600 * 1000L,
            targetDateMs = System.currentTimeMillis() + (10L * 24 * 3600 * 1000L)
        )

        engine.addManualTime(2, 30) // 2 hours 30 mins = 2.5 hours = 25% progress

        val active = engine.activeLegacy.value
        assertNotNull(active)
        assertEquals(9000000L, active?.completedTimeMs) // 2.5 * 3600 * 1000 = 9,000,000 ms
        assertEquals(9000000L, active?.manualTimeMs)
        assertEquals(25.0f, active?.progressPercentage ?: 0f, 0.01f)
    }

    @Test
    fun testPostpone() {
        val now = System.currentTimeMillis()
        val legacy = engine.createLegacy(
            name = "Learn Arabic",
            targetDurationMs = 50 * 3600 * 1000L,
            totalDays = 20,
            dailyTargetMs = (50 * 3600 * 1000L) / 20,
            targetDateMs = now + (20L * 24 * 3600 * 1000L)
        )

        engine.postpone(5) // Postpone by 5 days

        val active = engine.activeLegacy.value
        assertNotNull(active)
        assertEquals(5, active?.postponedDays)
        assertEquals(25, active?.totalTargetDays)
        assertEquals(legacy.targetDateMs + (5L * 24 * 3600 * 1000L), active?.targetDateMs)
    }

    @Test
    fun testStatusCalculation() {
        val now = System.currentTimeMillis()
        val legacy = Legacy(
            id = "test_1",
            name = "Project",
            targetDurationMs = 10 * 3600 * 1000L,
            totalDays = 10,
            dailyTargetMs = 3600 * 1000L,
            startDateMs = now,
            targetDateMs = now + (10L * 24 * 3600 * 1000L),
            accumulatedTimeMs = 3600 * 1000L // 1 hour completed on day 1
        )

        // Day 1: expected 1 hour, completed 1 hour -> ON_PACE
        assertEquals(LegacyStatus.ON_PACE, legacy.getStatus(now))

        // Ahead status
        val aheadLegacy = legacy.copy(accumulatedTimeMs = 3 * 3600 * 1000L) // 3 hours on day 1
        assertEquals(LegacyStatus.AHEAD, aheadLegacy.getStatus(now))

        // Behind status
        val behindLegacy = legacy.copy(accumulatedTimeMs = 1000L) // Almost 0 on day 1
        assertEquals(LegacyStatus.BEHIND, behindLegacy.getStatus(now))
    }

    @Test
    fun testPauseAndStop() {
        engine.createLegacy(
            name = "Meditation",
            targetDurationMs = 5 * 3600 * 1000L,
            totalDays = 5,
            dailyTargetMs = 3600 * 1000L,
            targetDateMs = System.currentTimeMillis() + (5L * 24 * 3600 * 1000L)
        )

        engine.pause()
        assertEquals(LegacyState.IDLE, engine.state.value)

        engine.stop()
        assertEquals(LegacyState.IDLE, engine.state.value)
    }

    @Test
    fun testDeleteLegacy() {
        val l1 = engine.createLegacy("Legacy 1", 10000L, 1, 10000L, System.currentTimeMillis() + 100000)
        val l2 = engine.createLegacy("Legacy 2", 20000L, 2, 10000L, System.currentTimeMillis() + 200000)

        assertEquals(2, engine.legacies.value.size)
        assertEquals(l2.id, engine.selectedLegacyId.value)

        engine.deleteLegacy(l2.id)

        assertEquals(1, engine.legacies.value.size)
        assertEquals(l1.id, engine.selectedLegacyId.value)
        assertEquals(l1.id, engine.activeLegacy.value?.id)
    }
}
