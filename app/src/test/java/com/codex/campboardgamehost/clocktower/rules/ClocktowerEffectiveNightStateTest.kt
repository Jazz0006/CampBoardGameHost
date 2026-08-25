package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerEffectiveNightStateTest {
    private val poisoner = ClocktowerInteractionId("other_night:role:Poisoner")
    private val monk = ClocktowerInteractionId("other_night:role:Monk")
    private val imp = ClocktowerInteractionId("other_night:role:Imp")
    private val empath = ClocktowerInteractionId("other_night:role:Empath")
    private val plan = listOf(poisoner, monk, imp, empath)
    private val impDeath = ResolvedNightMechanicalEvent.MechanicalDeath(1, imp)

    @Test fun `base living player is alive before any same-night death`() {
        assertTrue(project(emptyList(), monk).isMechanicallyAlive(2))
    }

    @Test fun `resolved death becomes mechanically effective at its cursor`() {
        assertTrue(project(listOf(impDeath), monk).isMechanicallyAlive(1))
        assertFalse(project(listOf(impDeath), imp).isMechanicallyAlive(1))
    }

    @Test fun `later death cannot rewrite earlier state`() {
        assertTrue(project(listOf(impDeath), monk).isMechanicallyAlive(1))
        assertFalse(project(listOf(impDeath), empath).isMechanicallyAlive(1))
    }

    @Test fun `no resolved death leaves target alive`() {
        assertTrue(project(emptyList(), empath).isMechanicallyAlive(1))
    }

    @Test fun `public dead remains dead`() {
        assertFalse(project(listOf(impDeath), empath, base = setOf(1, 2, 3)).isMechanicallyAlive(4))
    }

    @Test fun `projection is deterministic`() {
        val first = project(listOf(impDeath), empath)
        val second = project(listOf(impDeath), empath)
        assertEquals(first, second)
    }

    @Test fun `input collections remain unchanged`() {
        val base = linkedSetOf(1, 2, 3, 4)
        val events = listOf(impDeath)
        project(events, empath, base)
        assertEquals(setOf(1, 2, 3, 4), base)
        assertEquals(listOf(impDeath), events)
        assertEquals(plan, plan.toList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unknown cursor fails closed`() {
        project(emptyList(), ClocktowerInteractionId("other_night:role:Unknown"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `event with unknown interaction fails closed`() {
        project(listOf(ResolvedNightMechanicalEvent.MechanicalDeath(1, ClocktowerInteractionId("unknown"))), empath)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duplicate canonical interaction identity rejected`() {
        ClocktowerEffectiveNightStateProjector.projectAt(setOf(1), listOf(poisoner, poisoner), emptyList(), poisoner)
    }

    private fun project(
        events: List<ResolvedNightMechanicalEvent>,
        cursor: ClocktowerInteractionId,
        base: Set<Int> = setOf(1, 2, 3, 4),
    ) = ClocktowerEffectiveNightStateProjector.projectAt(base, plan, events, cursor)
}
