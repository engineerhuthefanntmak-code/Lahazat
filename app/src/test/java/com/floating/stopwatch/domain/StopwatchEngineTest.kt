package com.floating.stopwatch.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StopwatchEngineTest {

    @Test
    fun testInitialState() {
        val engine = StopwatchEngine()
        assertEquals(StopwatchState.Ready, engine.state.value)
        assertEquals(0L, engine.elapsedTimeMs.value)
        assertTrue(engine.laps.value.isEmpty())
    }

    @Test
    fun testStateTransitions() {
        val engine = StopwatchEngine()

        // Start state transition
        engine.start()
        assertEquals(StopwatchState.Running, engine.state.value)

        // Reset shouldn't work while running (requires pause)
        engine.reset()
        assertEquals(StopwatchState.Running, engine.state.value)

        // Pause state transition
        engine.pause()
        assertEquals(StopwatchState.Paused, engine.state.value)

        // Reset should work now
        engine.reset()
        assertEquals(StopwatchState.Ready, engine.state.value)
        assertEquals(0L, engine.elapsedTimeMs.value)
        assertTrue(engine.laps.value.isEmpty())
    }

    @Test
    fun testLapsCalculations() {
        val engine = StopwatchEngine()
        engine.start()

        // Add artificial delay can be simulated or we can restore state directly to test lap calculations safely
        engine.restoreState(5000L, StopwatchState.Running, emptyList())
        engine.lap()

        assertEquals(1, engine.laps.value.size)
        val firstLap = engine.laps.value.first()
        assertEquals(5000L, firstLap.cumulativeTimeMs)
        assertEquals(5000L, firstLap.lapTimeMs)
        assertEquals(0L, firstLap.diffFromPreviousMs)

        // Second lap
        engine.restoreState(8000L, StopwatchState.Running, engine.laps.value)
        engine.lap()

        assertEquals(2, engine.laps.value.size)
        val secondLap = engine.laps.value.last()
        assertEquals(8000L, secondLap.cumulativeTimeMs)
        assertEquals(3000L, secondLap.lapTimeMs)
        assertEquals(-2000L, secondLap.diffFromPreviousMs) // 3000L - 5000L = -2000L
    }
}
