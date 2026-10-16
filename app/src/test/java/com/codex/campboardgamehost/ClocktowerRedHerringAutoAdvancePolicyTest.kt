package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRedHerringAutoAdvancePolicyTest {
    @Test
    fun `accepted joint red herring may be reused only by automatic Host`() {
        assertTrue(shouldAutoAdvanceRedHerring(
            allowRuleDeterminedAutoUi = true,
            isRedHerringStep = true,
            isRealAction = true,
            hasSelectedRedHerring = true,
            acceptedJointOpening = true,
        ))
        assertFalse(shouldAutoAdvanceRedHerring(
            allowRuleDeterminedAutoUi = true,
            isRedHerringStep = true,
            isRealAction = true,
            hasSelectedRedHerring = false,
            acceptedJointOpening = true,
        ))
        assertFalse(shouldAutoAdvanceRedHerring(
            allowRuleDeterminedAutoUi = false,
            isRedHerringStep = true,
            isRealAction = true,
            hasSelectedRedHerring = true,
            acceptedJointOpening = true,
        ))
    }

    @Test
    fun `red herring auto advance skips only non-actions and never silently confirms real selection`() {
        assertFalse(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = false,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = false,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = false,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertTrue(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = false,
                hasSelectedRedHerring = false,
            ),
        )
    }

    @Test
    fun `legacy red herring recommendation cannot bypass Host singleton gate`() {
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice", "Bob"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = false,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = false,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = false,
                currentSelection = null,
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = "Bob",
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice", "Bob"),
            ),
        )
        assertEquals(
            "Alice",
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = null,
                legalSelections = setOf("Alice"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = "Stale",
                legalSelections = setOf("Alice", "Bob"),
            ),
        )
    }

    @Test
    fun `automatic information reveal requires current independent clue and no prior publication`() {
        val chef = PlayerCard("P1", Role.Civilian, "")
        val step = ClocktowerNightStepUi(
            title = "Chef", actor = chef, isRealAction = true,
            reason = "", storytellerAction = "", tellPlayer = null,
            explanation = "", roleEnName = "Chef",
        )
        fun eligible(
            current: ClocktowerNightStepUi = step,
            mode: Boolean = true,
            hasChoice: Boolean = true,
            records: List<RecordedEpistemicObservation> = emptyList(),
        ) = clocktowerShouldAutoPresentCurrentInformation(
            automaticMode = mode,
            step = current,
            phase = ClocktowerPhase.FirstNight,
            round = 1,
            actorSeat = 1,
            hasValidatedAiChoice = hasChoice,
            confirmedObservations = records,
        )
        assertTrue(eligible())
        assertFalse(eligible(mode = false))
        assertFalse(eligible(hasChoice = false))
        assertFalse(eligible(current = step.copy(isRealAction = false)))
        assertFalse(eligible(current = step.copy(action = ClocktowerNightAction.FortuneTeller)))
        assertFalse(eligible(current = step.copy(action = ClocktowerNightAction.Ravenkeeper)))
        assertFalse(eligible(current = step.copy(actor = null)))
        val received = RecordedEpistemicObservation(
            recordId = "confirmed-chef-1", phase = StorytellerPhase.FIRST_NIGHT,
            round = 1, sequence = 1, sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE, recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, value = 0),
        )
        assertFalse(eligible(records = listOf(received)))
        assertTrue(eligible(records = listOf(received.copy(round = 2))))
        assertTrue(eligible(records = listOf(received.copy(sourceSeat = 2,
            recipientSeats = setOf(2)))))
    }
}
