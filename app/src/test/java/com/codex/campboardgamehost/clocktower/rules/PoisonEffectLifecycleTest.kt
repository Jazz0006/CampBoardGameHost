package com.codex.campboardgamehost.clocktower.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PoisonEffectLifecycleTest {
    @Test
    fun `poison is inactive before source action`() {
        assertNull(PoisonEffectLifecycle.effectiveTarget("Empath", false, true))
    }

    @Test
    fun `functioning source activates confirmed target`() {
        assertEquals("Empath", PoisonEffectLifecycle.effectiveTarget("Empath", true, true))
    }

    @Test
    fun `source death ends active poison`() {
        assertNull(PoisonEffectLifecycle.effectiveTarget("Empath", true, false))
    }

    @Test
    fun `source ability loss ends active poison`() {
        assertNull(PoisonEffectLifecycle.effectiveTarget("Empath", true, false))
    }

    @Test
    fun `poison remains active through the following day`() {
        assertEquals("Empath", PoisonEffectLifecycle.afterNight("Empath", poisonerAlive = true))
    }

    @Test
    fun `poison ends if the poisoner dies`() {
        assertNull(PoisonEffectLifecycle.afterNight("Empath", poisonerAlive = false))
    }

    @Test
    fun `previous poison expires when the next night begins`() {
        assertNull(PoisonEffectLifecycle.atStartOfNextNight())
    }
}
