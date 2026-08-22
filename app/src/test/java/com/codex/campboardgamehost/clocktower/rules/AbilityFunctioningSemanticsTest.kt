package com.codex.campboardgamehost.clocktower.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AbilityFunctioningSemanticsTest {
    @Test
    fun `healthy real role interacts and functions`() {
        val subject = subject(actual = "Monk")

        assertTrue(AbilityFunctioningSemantics.interactsAs(subject, "Monk"))
        assertTrue(AbilityFunctioningSemantics.functionsAs(subject, "Monk"))
        assertEquals(AbilityFunctioningState.FUNCTIONING, AbilityFunctioningSemantics.stateFor(subject, "Monk"))
    }

    @Test
    fun `poisoned real role interacts but does not function`() {
        val subject = subject(actual = "Monk", poisoned = true)

        assertTrue(AbilityFunctioningSemantics.interactsAs(subject, "Monk"))
        assertFalse(AbilityFunctioningSemantics.functionsAs(subject, "Monk"))
        assertEquals(AbilityFunctioningState.POISONED, AbilityFunctioningSemantics.stateFor(subject, "Monk"))
    }

    @Test
    fun `Drunk interacts as shown role but does not function`() {
        val subject = subject(actual = "Drunk", shown = "Monk")

        assertEquals("Monk", AbilityFunctioningSemantics.perceivedRole(subject))
        assertTrue(AbilityFunctioningSemantics.interactsAs(subject, "Monk"))
        assertFalse(AbilityFunctioningSemantics.functionsAs(subject, "Monk"))
        assertEquals(AbilityFunctioningState.DRUNK, AbilityFunctioningSemantics.stateFor(subject, "Monk"))
    }

    @Test
    fun `conditional death interactions follow perceived role with impairment reliability`() {
        listOf("Ravenkeeper", "Sage").forEach { role ->
            assertEquals(AbilityFunctioningState.FUNCTIONING, AbilityFunctioningSemantics.stateFor(subject(role), role))
            assertEquals(AbilityFunctioningState.POISONED, AbilityFunctioningSemantics.stateFor(subject(role, poisoned = true), role))
            assertEquals(AbilityFunctioningState.DRUNK, AbilityFunctioningSemantics.stateFor(subject("Drunk", role), role))
            assertFalse(AbilityFunctioningSemantics.interactsAs(subject("Chef"), role))
        }
    }

    @Test
    fun `mechanical roles require healthy actual ownership`() {
        listOf("Monk", "Soldier", "Slayer", "Virgin", "Mayor").forEach { role ->
            assertTrue(AbilityFunctioningSemantics.functionsAs(subject(role), role))
            assertFalse(AbilityFunctioningSemantics.functionsAs(subject(role, poisoned = true), role))
            assertFalse(AbilityFunctioningSemantics.functionsAs(subject("Drunk", role), role))
        }
    }

    @Test
    fun `committed Monk selection requires a functioning Monk to protect`() {
        assertTrue(
            AbilityFunctioningSemantics.selectedMechanicalEffectApplies(
                subject("Monk"), "Monk", selectionMatches = true,
            ),
        )
        assertFalse(
            AbilityFunctioningSemantics.selectedMechanicalEffectApplies(
                subject("Monk", poisoned = true), "Monk", selectionMatches = true,
            ),
        )
        assertFalse(
            AbilityFunctioningSemantics.selectedMechanicalEffectApplies(
                subject("Drunk", "Monk"), "Monk", selectionMatches = true,
            ),
        )
    }

    @Test
    fun `representative information role preserves interaction and reliability contract`() {
        val role = "Empath"
        assertEquals(AbilityFunctioningState.FUNCTIONING, AbilityFunctioningSemantics.stateFor(subject(role), role))
        assertEquals(AbilityFunctioningState.POISONED, AbilityFunctioningSemantics.stateFor(subject(role, poisoned = true), role))
        assertEquals(AbilityFunctioningState.DRUNK, AbilityFunctioningSemantics.stateFor(subject("Drunk", role), role))
    }

    private fun subject(actual: String, shown: String = actual, poisoned: Boolean = false) =
        AbilitySubject(actualRole = actual, shownRole = shown, isPoisoned = poisoned, isAlive = true)
}
