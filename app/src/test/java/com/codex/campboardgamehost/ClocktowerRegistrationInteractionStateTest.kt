package com.codex.campboardgamehost

import org.junit.Assert.*
import org.junit.Test

class ClocktowerRegistrationInteractionStateTest {
    @Test fun `Virgin ruling distinguishes untouched explicit false and separate nominations`() {
        val state = ClocktowerRegistrationInteractionState()
        assertNull(state.explicitChoice(null))
        assertNull(state.explicitChoice("day1"))
        assertFalse(state.spyHasExplicitChoice("day1"))
        state.chooseSpy("day1", false)
        assertEquals(false, state.explicitChoice("day1"))
        assertTrue(state.spyHasExplicitChoice("day1"))
        assertFalse(state.spyIsGood("day1"))
        state.chooseSpy("day1", true)
        assertTrue(state.spyIsGood("day1"))
        assertNull(state.explicitChoice("day2"))
    }
}
