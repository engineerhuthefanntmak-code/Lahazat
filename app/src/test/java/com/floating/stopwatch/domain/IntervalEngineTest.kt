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
        assertEquals("HIIT", engine.activeTemplate.value?.name)
        assertEquals(1, engine.currentRound.value)
        assertEquals(0, engine.currentStageIndex.value)
        assertEquals(40000L, engine.stageRemainingMs.value)
    }

    @Test
    fun testLoadCustomTemplate() {
        val customTemplate = IntervalTemplate(
            id = "custom_test",
            name = "TEST TEMPLATE",
            stages = listOf(
                IntervalStage("st1", "WORK", 10000L, 0, IntervalStageType.WORK),
                IntervalStage("st2", "REST", 5000L, 1, IntervalStageType.REST)
            ),
            repetitions = 3
        )
        engine.loadTemplate(customTemplate)

        assertEquals(IntervalState.IDLE, engine.state.value)
        assertEquals("TEST TEMPLATE", engine.activeTemplate.value?.name)
        assertEquals(10000L, engine.stageRemainingMs.value)
    }

    @Test
    fun testPauseAndReset() {
        val template = IntervalEngine.BUILT_IN_TEMPLATES[1] // Tabata
        engine.loadTemplate(template)
        assertEquals(20000L, engine.stageRemainingMs.value)

        engine.pause()
        assertEquals(IntervalState.IDLE, engine.state.value)

        engine.reset()
        assertEquals(IntervalState.IDLE, engine.state.value)
        assertEquals(1, engine.currentRound.value)
        assertEquals(0, engine.currentStageIndex.value)
        assertEquals(20000L, engine.stageRemainingMs.value)
    }
}
