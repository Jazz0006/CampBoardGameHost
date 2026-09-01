package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.StorytellerAutomationMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnifiedSelectionPoolTest {
    @Test fun `AUTO and ASSISTED share ranking while assisted also exposes expert candidates`() {
        val pool = UnifiedSelectionPool(listOf(
            candidate("recommended", QualityTier.RECOMMENDED, 100),
            candidate("warning", QualityTier.ACCEPTABLE_WITH_WARNING, 90),
            candidate("expert", QualityTier.EXPERT_ONLY, 80),
        ))

        assertEquals(listOf("recommended", "warning"), pool.candidatesFor(SelectionExecutionPolicy.AUTO).map { it.candidateId })
        assertEquals(listOf("recommended", "warning", "expert"), pool.candidatesFor(SelectionExecutionPolicy.ASSISTED).map { it.candidateId })
        assertEquals(pool.paritySignature().take(2), pool.candidatesFor(SelectionExecutionPolicy.AUTO).map {
            UnifiedCandidateParity(it.candidateId, it.qualityTier, it.rankFixedPoint)
        })
    }

    @Test fun `assisted selection keeps legal verified candidates independent of recommendation quality`() {
        val pool = UnifiedSelectionPool(listOf(
            candidate("recommended", QualityTier.RECOMMENDED, 100),
            candidate("rejected-quality", QualityTier.REJECTED, 10),
        ))

        assertEquals(
            listOf("recommended"),
            pool.candidatesFor(SelectionExecutionPolicy.AUTO).map { it.candidateId },
        )
        assertEquals(
            listOf("recommended", "rejected-quality"),
            pool.candidatesFor(SelectionExecutionPolicy.ASSISTED).map { it.candidateId },
        )
    }

    @Test fun `ineligible and epistemically deferred candidates are selectable in neither policy`() {
        val pool = UnifiedSelectionPool(listOf(
            candidate("legal", QualityTier.RECOMMENDED, 2),
            candidate("illegal", QualityTier.RECOMMENDED, 3, legality = UnifiedCandidateLegality.INELIGIBLE),
            candidate("deferred", QualityTier.EXPERT_ONLY, 4, epistemic = UnifiedEpistemicStatus.DEFERRED_B4),
        ))

        assertEquals(listOf("legal"), pool.candidatesFor(SelectionExecutionPolicy.AUTO).map { it.candidateId })
        assertEquals(listOf("legal"), pool.candidatesFor(SelectionExecutionPolicy.ASSISTED).map { it.candidateId })
    }

    @Test fun `manual preference maps to assisted without changing its persisted value`() {
        assertEquals("manual", StorytellerAutomationMode.MANUAL.prefsValue)
        assertEquals(SelectionExecutionPolicy.ASSISTED, StorytellerAutomationMode.MANUAL.executionPolicy())
        assertEquals(SelectionExecutionPolicy.AUTO, StorytellerAutomationMode.AUTO_BALANCED.executionPolicy())
    }

    @Test fun `parity recorder retains aggregate match information only`() {
        val recorder = SelectionPoolParityRecorder()
        recorder.record("chef", listOf(UnifiedCandidateParity("a", QualityTier.RECOMMENDED, 1)), listOf(UnifiedCandidateParity("a", QualityTier.RECOMMENDED, 1)))
        recorder.recordResult("chef", matches = false)

        val totals = recorder.snapshot().getValue("chef")
        assertEquals(2, totals.comparisons)
        assertEquals(1, totals.matches)
        assertEquals(1, totals.mismatches)
        assertTrue(recorder.snapshot().keys.all { it == "chef" })
    }

    private fun candidate(
        id: String,
        tier: QualityTier,
        rank: Long,
        legality: UnifiedCandidateLegality = UnifiedCandidateLegality.LEGAL,
        epistemic: UnifiedEpistemicStatus = UnifiedEpistemicStatus.VERIFIED,
    ) = UnifiedSelectionCandidate(
        candidateId = id,
        familyId = "first-night",
        legality = legality,
        epistemicStatus = epistemic,
        qualityTier = tier,
        rankFixedPoint = rank,
        payload = id,
    )
}
