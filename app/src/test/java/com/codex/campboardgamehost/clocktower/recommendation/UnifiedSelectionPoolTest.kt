package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.QualityTier
import org.junit.Assert.assertEquals
import org.junit.Test

class UnifiedSelectionPoolTest {
    @Test fun `AUTO and ASSISTED share stable order while assisted also exposes expert candidates`() {
        val pool = UnifiedSelectionPool(listOf(
            candidate("a-recommended", QualityTier.RECOMMENDED),
            candidate("b-warning", QualityTier.ACCEPTABLE_WITH_WARNING),
            candidate("c-expert", QualityTier.EXPERT_ONLY),
        ))

        assertEquals(listOf("a-recommended", "b-warning"), pool.candidatesFor(SelectionExecutionPolicy.AUTO).map { it.candidateId })
        assertEquals(listOf("a-recommended", "b-warning", "c-expert"), pool.candidatesFor(SelectionExecutionPolicy.ASSISTED).map { it.candidateId })
    }

    @Test fun `ineligible and epistemically deferred candidates are selectable in neither policy`() {
        val pool = UnifiedSelectionPool(listOf(
            candidate("legal", QualityTier.RECOMMENDED),
            candidate("illegal", QualityTier.RECOMMENDED, legality = UnifiedCandidateLegality.INELIGIBLE),
            candidate("deferred", QualityTier.EXPERT_ONLY, epistemic = UnifiedEpistemicStatus.DEFERRED_B4),
        ))

        assertEquals(listOf("legal"), pool.candidatesFor(SelectionExecutionPolicy.AUTO).map { it.candidateId })
        assertEquals(listOf("legal"), pool.candidatesFor(SelectionExecutionPolicy.ASSISTED).map { it.candidateId })
    }

    private fun candidate(
        id: String,
        tier: QualityTier,
        legality: UnifiedCandidateLegality = UnifiedCandidateLegality.LEGAL,
        epistemic: UnifiedEpistemicStatus = UnifiedEpistemicStatus.VERIFIED,
    ) = UnifiedSelectionCandidate(
        candidateId = id,
        familyId = "first-night",
        legality = legality,
        epistemicStatus = epistemic,
        qualityTier = tier,
        payload = id,
    )
}
