package com.codex.campboardgamehost.clocktower.recommendation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionRolloutGateTest {
    @Test fun `default evidence permits only requested legacy or shadow stage without review`() {
        val decision = SelectionRolloutGate.assess(
            requestedStage = SelectionRolloutStage.SHADOW_COMPARE,
            evidence = SelectionRolloutEvidence(),
            distributionReviewApproved = false,
        )

        assertEquals(SelectionRolloutStage.SHADOW_COMPARE, decision.effectiveStage)
        assertTrue(decision.expansionAllowed)
        assertNull(decision.rollbackReason)
    }

    @Test fun `assisted expansion requires approved distribution review`() {
        val decision = SelectionRolloutGate.assess(
            requestedStage = SelectionRolloutStage.ASSISTED,
            evidence = SelectionRolloutEvidence(candidateParityComparisons = 10),
            distributionReviewApproved = false,
        )

        assertEquals(SelectionRolloutStage.SHADOW_COMPARE, decision.effectiveStage)
        assertFalse(decision.expansionAllowed)
    }

    @Test fun `any parity mismatch immediately rolls back and blocks auto expansion`() {
        val decision = SelectionRolloutGate.assess(
            requestedStage = SelectionRolloutStage.AUTO,
            evidence = SelectionRolloutEvidence(
                candidateParityComparisons = 10,
                candidateParityMismatches = 1,
            ),
            distributionReviewApproved = true,
        )

        assertEquals(SelectionRolloutStage.LEGACY_ONLY, decision.effectiveStage)
        assertFalse(decision.expansionAllowed)
        assertEquals("unexplained parity mismatch", decision.rollbackReason)
    }

    @Test fun `stale discards remain reviewable but do not cause a false rollback`() {
        val decision = SelectionRolloutGate.assess(
            requestedStage = SelectionRolloutStage.LIMITED_AUTO,
            evidence = SelectionRolloutEvidence(staleDiscards = 3),
            distributionReviewApproved = true,
        )

        assertEquals(SelectionRolloutStage.LIMITED_AUTO, decision.effectiveStage)
        assertTrue(decision.expansionAllowed)
    }
}
