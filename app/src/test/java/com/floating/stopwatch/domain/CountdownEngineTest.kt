package com.floating.stopwatch.domain

import org.junit.Assert.*
import org.junit.Test

class CountdownEngineTest {

    @Test
    fun testCountdownEngineDurationAndSync() {
        val engine = CountdownEngine()
        assertEquals(300000L, engine.initialDurationMs.value)
        assertEquals(300000L, engine.remainingTimeMs.value)
        assertFalse(engine.isRunning.value)

        engine.setDuration(60000L)
        assertEquals(60000L, engine.initialDurationMs.value)
        assertEquals(60000L, engine.remainingTimeMs.value)

        engine.adjustDuration(30000L)
        assertEquals(90000L, engine.initialDurationMs.value)
        assertEquals(90000L, engine.remainingTimeMs.value)

        engine.reset()
        assertEquals(90000L, engine.remainingTimeMs.value)
        assertFalse(engine.isRunning.value)
    }
}
