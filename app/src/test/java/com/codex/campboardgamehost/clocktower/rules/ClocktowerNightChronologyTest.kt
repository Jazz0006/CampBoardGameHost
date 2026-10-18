package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import org.junit.Assert.assertTrue
import org.junit.Test

/** Ordering retained for the optional-source cursor, independent of any mechanical shadow state. */
class ClocktowerNightChronologyTest {
    private val poisoner = ClocktowerInteractionId("other_night:role:Poisoner")
    private val imp = ClocktowerInteractionId("other_night:role:Imp")
    private val empath = ClocktowerInteractionId("other_night:role:Empath")
    private val plan = listOf(poisoner, imp, empath)

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

}
