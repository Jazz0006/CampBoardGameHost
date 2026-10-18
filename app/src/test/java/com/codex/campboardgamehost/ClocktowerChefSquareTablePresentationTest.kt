package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerChefSquareTablePresentationTest {
    @Test
    fun `host visual distinguishes actual evil and recluse without inferring a registration witness`() {
        val evil = clocktowerChefSeatVisual(
            seatNumber = 2, actorSeat = 1, actualEvilSeats = setOf(2),
            spySeat = null, recluseSeat = 4, language = "zh",
        )
        val recluse = clocktowerChefSeatVisual(
            seatNumber = 4, actorSeat = 1, actualEvilSeats = setOf(2),
            spySeat = null, recluseSeat = 4, language = "zh",
        )
        val actor = clocktowerChefSeatVisual(
            seatNumber = 1, actorSeat = 1, actualEvilSeats = setOf(2),
            spySeat = null, recluseSeat = 4, language = "zh",
        )
        assertEquals(ClocktowerSquareTableSeatState.HighlightedInformation, evil.state)
        assertEquals(ClocktowerSquareTableSeatState.RegistrationHint, recluse.state)
        assertEquals("隐", recluse.badge)
        assertTrue(actor.isCurrentActor)
        assertFalse(evil.isCurrentActor)
    }

    @Test
    fun `a confirmed direct Chef number stays usable without legacy automatic witness`() {
        val proposition = InformationProposition.NumericResult(
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            sourceSeat = 1,
            subjectSeats = listOf(1, 2, 3),
            value = 1,
        )
        val step = ClocktowerNightStepUi(
            title = "Chef", actor = null, isRealAction = true, reason = "",
            storytellerAction = "", tellPlayer = "1", explanation = "",
            displayKind = ClocktowerDisplayKind.Number,
            displayPrimary = "1", displayProposition = proposition, roleEnName = "Chef",
        )
        val choices = clocktowerChefResultChoices(
            step = step,
            allowRuleDeterminedAutoUi = true,
            automaticDisplayOption = null,
            resultFirstRegistrationCandidates = emptyList(),
        )
        assertEquals(1, choices.size)
        assertEquals(ClocktowerChefResultSourceKind.Direct, choices.single().sourceKind)
        assertEquals(1, choices.single().value)
    }
}

