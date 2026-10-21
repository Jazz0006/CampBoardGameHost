package com.codex.campboardgamehost.clocktower.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AbilityFunctioningSemanticsTest {
    @Test
    fun `healthy real role interacts and functions`() {
        val subject = subject(actual = "Monk")

        assertTrue((AbilityFunctioningSemantics.stateFor(subject, "Monk") != null))
        assertTrue((AbilityFunctioningSemantics.stateFor(subject, "Monk") == AbilityFunctioningState.FUNCTIONING))
        assertEquals(AbilityFunctioningState.FUNCTIONING, AbilityFunctioningSemantics.stateFor(subject, "Monk"))
    }

    @Test
    fun `poisoned real role interacts but does not function`() {
        val subject = subject(actual = "Monk", poisoned = true)

        assertTrue((AbilityFunctioningSemantics.stateFor(subject, "Monk") != null))
        assertFalse((AbilityFunctioningSemantics.stateFor(subject, "Monk") == AbilityFunctioningState.FUNCTIONING))
        assertEquals(AbilityFunctioningState.POISONED, AbilityFunctioningSemantics.stateFor(subject, "Monk"))
    }

    @Test
    fun `Drunk interacts as shown role but does not function`() {
        val subject = subject(actual = "Drunk", shown = "Monk")

        assertEquals("Monk", AbilityFunctioningSemantics.perceivedRole(subject))
        assertTrue((AbilityFunctioningSemantics.stateFor(subject, "Monk") != null))
        assertFalse((AbilityFunctioningSemantics.stateFor(subject, "Monk") == AbilityFunctioningState.FUNCTIONING))
        assertEquals(AbilityFunctioningState.DRUNK, AbilityFunctioningSemantics.stateFor(subject, "Monk"))
    }

    @Test
    fun `conditional death interactions follow perceived role with impairment reliability`() {
        listOf("Ravenkeeper", "Sage").forEach { role ->
            assertEquals(AbilityFunctioningState.FUNCTIONING, AbilityFunctioningSemantics.stateFor(subject(role), role))
            assertEquals(AbilityFunctioningState.POISONED, AbilityFunctioningSemantics.stateFor(subject(role, poisoned = true), role))
            assertEquals(AbilityFunctioningState.DRUNK, AbilityFunctioningSemantics.stateFor(subject("Drunk", role), role))
            assertFalse((AbilityFunctioningSemantics.stateFor(subject("Chef"), role) != null))
        }
    }

    @Test
    fun `established death trigger keeps poison state after death`() {
        val subject = subject(
            actual = "Ravenkeeper",
            poisoned = true,
            alive = false,
        )

        assertEquals(null, AbilityFunctioningSemantics.stateFor(subject, "Ravenkeeper"))
        assertEquals(
            AbilityFunctioningState.POISONED,
            AbilityFunctioningSemantics.stateForEstablishedInteraction(subject, "Ravenkeeper"),
        )
    }

    @Test
    fun `mechanical roles require healthy actual ownership`() {
        listOf("Monk", "Soldier", "Slayer", "Virgin", "Mayor").forEach { role ->
            assertTrue((AbilityFunctioningSemantics.stateFor(subject(role), role) == AbilityFunctioningState.FUNCTIONING))
            assertFalse((AbilityFunctioningSemantics.stateFor(subject(role, poisoned = true), role) == AbilityFunctioningState.FUNCTIONING))
            assertFalse((AbilityFunctioningSemantics.stateFor(subject("Drunk", role), role) == AbilityFunctioningState.FUNCTIONING))
        }
    }

    @Test
    fun `representative information role preserves interaction and reliability contract`() {
        val role = "Empath"
        assertEquals(AbilityFunctioningState.FUNCTIONING, AbilityFunctioningSemantics.stateFor(subject(role), role))
        assertEquals(AbilityFunctioningState.POISONED, AbilityFunctioningSemantics.stateFor(subject(role, poisoned = true), role))
        assertEquals(AbilityFunctioningState.DRUNK, AbilityFunctioningSemantics.stateFor(subject("Drunk", role), role))
    }

    @Test
    fun `representative mechanical roles preserve interaction but require functioning state`() {
        listOf("Soldier", "Virgin", "Mayor").forEach { role ->
            assertTrue((AbilityFunctioningSemantics.stateFor(subject(role), role) == AbilityFunctioningState.FUNCTIONING))

            val poisoned = subject(role, poisoned = true)
            assertTrue((AbilityFunctioningSemantics.stateFor(poisoned, role) != null))
            assertFalse((AbilityFunctioningSemantics.stateFor(poisoned, role) == AbilityFunctioningState.FUNCTIONING))

            val drunkShown = subject("Drunk", shown = role)
            assertTrue((AbilityFunctioningSemantics.stateFor(drunkShown, role) != null))
            assertFalse((AbilityFunctioningSemantics.stateFor(drunkShown, role) == AbilityFunctioningState.FUNCTIONING))
        }
    }

    private fun subject(
        actual: String,
        shown: String = actual,
        poisoned: Boolean = false,
        alive: Boolean = true,
    ) = AbilitySubject(
        actualRole = actual,
        shownRole = shown,
        isPoisoned = poisoned,
        isAlive = alive,
    )
}
