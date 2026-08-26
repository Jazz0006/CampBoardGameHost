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
    private val impDeath = ResolvedNightMechanicalEvent.MechanicalDeath(
        1,
        ClocktowerEffectiveNightCursor(imp, ClocktowerInteractionBoundary.AFTER),
    )

    @Test fun `base living player is alive before any same-night death`() {
        assertTrue(project(emptyList(), monk).isMechanicallyAlive(2))
    }

    @Test fun `death from interaction does not affect before same interaction`() {
        assertTrue(project(listOf(impDeath), imp, ClocktowerInteractionBoundary.BEFORE).isMechanicallyAlive(1))
    }

    @Test fun `death is effective after same interaction`() {
        assertFalse(project(listOf(impDeath), imp, ClocktowerInteractionBoundary.AFTER).isMechanicallyAlive(1))
    }

    @Test fun `death is visible before later interaction`() {
        assertFalse(project(listOf(impDeath), empath, ClocktowerInteractionBoundary.BEFORE).isMechanicallyAlive(1))
    }

    @Test fun `later event does not rewrite earlier after snapshot`() {
        val laterDeath = ResolvedNightMechanicalEvent.MechanicalDeath(
            1,
            ClocktowerEffectiveNightCursor(empath, ClocktowerInteractionBoundary.AFTER),
        )
        assertTrue(project(listOf(laterDeath), monk, ClocktowerInteractionBoundary.AFTER).isMechanicallyAlive(1))
    }

    @Test fun `self death does not suppress actor before its own action`() {
        assertTrue(project(listOf(impDeath), imp, ClocktowerInteractionBoundary.BEFORE).isMechanicallyAlive(1))
        assertFalse(project(listOf(impDeath), imp, ClocktowerInteractionBoundary.AFTER).isMechanicallyAlive(1))
    }

    @Test fun `no resolved death leaves target alive`() {
        assertTrue(project(emptyList(), empath).isMechanicallyAlive(1))
    }

    @Test fun `public dead remains dead`() {
        assertFalse(project(listOf(impDeath), empath, base = setOf(1, 2, 3)).isMechanicallyAlive(4))
    }

    @Test fun `projection is deterministic`() {
        val first = project(listOf(impDeath), empath, ClocktowerInteractionBoundary.BEFORE)
        val second = project(listOf(impDeath), empath, ClocktowerInteractionBoundary.BEFORE)
        assertEquals(first, second)
    }

    @Test fun `chronology orders before before after at the same interaction`() {
        assertTrue(
            ClocktowerEffectiveNightChronology.rankOf(plan, ClocktowerEffectiveNightCursor(imp, ClocktowerInteractionBoundary.BEFORE)) <
                ClocktowerEffectiveNightChronology.rankOf(plan, ClocktowerEffectiveNightCursor(imp, ClocktowerInteractionBoundary.AFTER)),
        )
    }

    @Test fun `chronology orders after before before the next interaction`() {
        assertTrue(
            ClocktowerEffectiveNightChronology.rankOf(plan, ClocktowerEffectiveNightCursor(imp, ClocktowerInteractionBoundary.AFTER)) <
                ClocktowerEffectiveNightChronology.rankOf(plan, ClocktowerEffectiveNightCursor(empath, ClocktowerInteractionBoundary.BEFORE)),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `chronology rejects unknown interaction`() {
        ClocktowerEffectiveNightChronology.rankOf(
            plan,
            ClocktowerEffectiveNightCursor(ClocktowerInteractionId("unknown"), ClocktowerInteractionBoundary.BEFORE),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `chronology rejects duplicate interaction ids`() {
        ClocktowerEffectiveNightChronology.rankOf(
            listOf(poisoner, poisoner),
            ClocktowerEffectiveNightCursor(poisoner, ClocktowerInteractionBoundary.BEFORE),
        )
    }

    @Test fun `input collections remain unchanged`() {
        val base = linkedSetOf(1, 2, 3, 4)
        val events = listOf(impDeath)
        project(events, empath, ClocktowerInteractionBoundary.AFTER, base)
        assertEquals(setOf(1, 2, 3, 4), base)
        assertEquals(listOf(impDeath), events)
        assertEquals(plan, plan.toList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unknown cursor fails closed`() {
        project(
            emptyList(),
            ClocktowerInteractionId("other_night:role:Unknown"),
            ClocktowerInteractionBoundary.BEFORE,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `event with unknown interaction fails closed`() {
        project(
            listOf(
                ResolvedNightMechanicalEvent.MechanicalDeath(
                    1,
                    ClocktowerEffectiveNightCursor(
                        ClocktowerInteractionId("unknown"),
                        ClocktowerInteractionBoundary.AFTER,
                    ),
                ),
            ),
            empath,
            ClocktowerInteractionBoundary.AFTER,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duplicate canonical interaction identity rejected`() {
        ClocktowerEffectiveNightStateProjector.projectAt(
            setOf(1),
            listOf(poisoner, poisoner),
            emptyList(),
            ClocktowerEffectiveNightCursor(poisoner, ClocktowerInteractionBoundary.BEFORE),
        )
    }

    private fun project(
        events: List<ResolvedNightMechanicalEvent>,
        cursor: ClocktowerInteractionId,
        boundary: ClocktowerInteractionBoundary = ClocktowerInteractionBoundary.AFTER,
        base: Set<Int> = setOf(1, 2, 3, 4),
    ) = ClocktowerEffectiveNightStateProjector.projectAt(
        base,
        plan,
        events,
        ClocktowerEffectiveNightCursor(cursor, boundary),
    )
}
