package com.floating.stopwatch.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IntervalEngineTest {

    private lateinit var engine: IntervalEngine

    @Before
    fun setUp() {
        engine = IntervalEngine()
    }

    @Test
    fun testInitialState() {
        assertEquals(IntervalState.IDLE, engine.state.value)
        assertNotNull(engine.activeTemplate.value)
        assertEquals("HIT", engine.activeTemplate.value.name)
        assertEquals(40000L, engine.activeTemplate.value.workDurationMs)
        assertEquals(20000L, engine.activeTemplate.value.restDurationMs)
        assertEquals(8, engine.activeTemplate.value.repetitions)
        assertEquals(1, engine.currentRound.value)
        assertEquals(0, engine.currentStageIndex.value)
        assertEquals(40000L, engine.stageRemainingMs.value)
    }

    @Test
    fun testRenameTemplate() {
        engine.renameTemplate("BOXING")
        assertEquals("BOXING", engine.activeTemplate.value.name)
        assertEquals(40000L, engine.activeTemplate.value.workDurationMs)
    }

    @Test
    fun testUpdateDurations() {
        engine.updateDurations(45000L, 15000L, 12)
        assertEquals(45000L, engine.activeTemplate.value.workDurationMs)
        assertEquals(15000L, engine.activeTemplate.value.restDurationMs)
        assertEquals(12, engine.activeTemplate.value.repetitions)
        assertEquals(45000L, engine.stageRemainingMs.value)
    }

    @Test
    fun testPauseAndReset() {
        engine.updateDurations(30000L, 10000L, 5)
        assertEquals(30000L, engine.stageRemainingMs.value)

        engine.pause()
        assertEquals(IntervalState.IDLE, engine.state.value)

        engine.reset()
        assertEquals(IntervalState.IDLE, engine.state.value)
        assertEquals(1, engine.currentRound.value)
        assertEquals(0, engine.currentStageIndex.value)
        assertEquals(30000L, engine.stageRemainingMs.value)
    }
}
