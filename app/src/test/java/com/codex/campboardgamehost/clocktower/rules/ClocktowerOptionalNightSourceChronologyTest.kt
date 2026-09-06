package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerOptionalNightSourceChronologyTest {
    private val poisoner = ClocktowerInteractionId("other_night:role:Poisoner")
    private val monk = ClocktowerInteractionId("other_night:role:Monk")
    private val imp = ClocktowerInteractionId("other_night:role:Imp")
    private val empath = ClocktowerInteractionId("other_night:role:Empath")

    @Test
    fun `absent source interaction is inactive instead of ranking an unknown cursor`() {
        val canonical = listOf(monk, imp, empath)

        assertFalse(
            ClocktowerOptionalNightSourceChronology.hasActedBy(
                canonicalInteractionIds = canonical,
                cursor = ClocktowerEffectiveNightCursor(
                    empath,
                    ClocktowerInteractionBoundary.BEFORE,
                ),
                sourceInteractionId = poisoner,
            ),
        )
    }

    @Test
    fun `present source is inactive before its own action`() {
        val canonical = listOf(poisoner, monk, imp, empath)

        assertFalse(
            ClocktowerOptionalNightSourceChronology.hasActedBy(
                canonicalInteractionIds = canonical,
                cursor = ClocktowerEffectiveNightCursor(
                    poisoner,
                    ClocktowerInteractionBoundary.BEFORE,
                ),
                sourceInteractionId = poisoner,
            ),
        )
    }

    @Test
    fun `present source is active after its own action`() {
        val canonical = listOf(poisoner, monk, imp, empath)

        assertTrue(
            ClocktowerOptionalNightSourceChronology.hasActedBy(
                canonicalInteractionIds = canonical,
                cursor = ClocktowerEffectiveNightCursor(
                    empath,
                    ClocktowerInteractionBoundary.BEFORE,
                ),
                sourceInteractionId = poisoner,
            ),
        )
    }
}
