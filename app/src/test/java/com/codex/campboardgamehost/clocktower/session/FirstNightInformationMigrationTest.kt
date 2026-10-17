package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Publication lifecycle checks; old Shadow parity and candidate rankings are retired. */
class FirstNightInformationMigrationTest {
    @Test fun everyFirstNightFamilyPublishesOneSelectedObservation() {
        FirstNightInformationFamily.entries.forEachIndexed { index, family ->
            val request = request(family, index + 1, ReliabilityState.RELIABLE, "selected")
            val result = FirstNightInformationMigration().resolvePublication(request)
            assertTrue(result is FirstNightPublicationResolution.Published)
            val gate = (result as FirstNightPublicationResolution.Published).migration
            assertTrue(gate.isDisplayed(request.decisionId))
            assertFalse(gate.isReady(request.decisionId))
        }
    }

    @Test fun rejectsAnUnrecognizedOrDuplicatePublicationChoice() {
        val valid = request(FirstNightInformationFamily.CHEF, 2, ReliabilityState.POISONED, "one")
        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(selectedCandidateId = "not-in-pool")
        }
        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(candidates = valid.candidates + valid.candidates.single())
        }
    }

    @Test fun invalidationPreservesDisplayedFactsAndAllowsLaterPublication() {
        val washerwoman = request(FirstNightInformationFamily.WASHERWOMAN, 1, ReliabilityState.DRUNK, "ww")
        val chef = request(FirstNightInformationFamily.CHEF, 2, ReliabilityState.POISONED, "chef")
        val first = (FirstNightInformationMigration().resolvePublication(washerwoman)
            as FirstNightPublicationResolution.Published).migration
        val after = first.invalidateUnshown()
        assertTrue(after.isDisplayed(washerwoman.decisionId))
        assertFalse(after.isReady(washerwoman.decisionId))
        assertEquals(1, after.generation())
        val next = (after.resolvePublication(chef) as FirstNightPublicationResolution.Published).migration
        assertTrue(next.isDisplayed(chef.decisionId))
        assertTrue(next.isDisplayed(washerwoman.decisionId))
    }

    @Test fun repeatedRevealNeverChangesAlreadyDisplayedResult() {
        val original = request(FirstNightInformationFamily.CHEF, 2, ReliabilityState.RELIABLE, "one")
        val first = (FirstNightInformationMigration().resolvePublication(original)
            as FirstNightPublicationResolution.Published).migration
        val changed = original.copy(
            selectedCandidateId = "two",
            candidates = listOf(candidate(FirstNightInformationFamily.CHEF, 2, ReliabilityState.RELIABLE, "two")
                .copy(observation = original.candidates.single().observation.copy(semanticTruth = SemanticTruth.FALSE))),
        )
        assertEquals(FirstNightPublicationResolution.AlreadyDisplayed, first.resolvePublication(changed))
        assertTrue(first.isDisplayed(original.decisionId))
    }

    private fun request(
        family: FirstNightInformationFamily,
        seat: Int,
        reliability: ReliabilityState,
        id: String,
    ): FirstNightInformationRequest = FirstNightInformationRequest(
        decisionId = "first-night-${family.name.lowercase()}-$seat",
        family = family,
        sourceSeat = seat,
        reliability = reliability,
        selectedCandidateId = id,
        candidates = listOf(candidate(family, seat, reliability, id)),
    )

    private fun candidate(
        family: FirstNightInformationFamily,
        seat: Int,
        reliability: ReliabilityState,
        id: String,
    ) = FirstNightInformationCandidate(id, AbilityObservation(
        sourceSeat = seat,
        perceivedRole = family.role,
        reliability = reliability,
        semanticTruth = SemanticTruth.TRUE,
    ))
}
