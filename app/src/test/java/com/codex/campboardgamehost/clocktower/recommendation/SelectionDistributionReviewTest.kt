package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionDistributionReviewTest {
    @Test fun `signals an eligible family materially below its same cohort leader`() {
        val review = SelectionDistributionReviewer.review(
            snapshot = mapOf(
                key("leader") to totals(eligible = 20, selected = 16),
                key("withheld") to totals(eligible = 20, selected = 4),
            ),
        )

        assertEquals(2, review.reviewedStrata)
        assertEquals(listOf("withheld"), review.signals.map { it.key.familyId })
        assertFalse(review.approvedForExpansion)
    }

    @Test fun `does not infer a signal before the minimum aggregate sample`() {
        val review = SelectionDistributionReviewer.review(
            snapshot = mapOf(
                key("leader") to totals(eligible = 19, selected = 19),
                key("low") to totals(eligible = 19, selected = 0),
            ),
        )

        assertEquals(0, review.reviewedStrata)
        assertTrue(review.signals.isEmpty())
        assertFalse(review.approvedForExpansion)
    }

    @Test fun `keeps different phase style or player counts out of the comparison cohort`() {
        val review = SelectionDistributionReviewer.review(
            snapshot = mapOf(
                key("day", phase = StorytellerPhase.DAY) to totals(eligible = 20, selected = 0),
                key("night", phase = StorytellerPhase.NIGHT) to totals(eligible = 20, selected = 20),
            ),
        )

        assertTrue(review.signals.isEmpty())
        assertTrue(review.approvedForExpansion)
    }

    private fun key(familyId: String, phase: StorytellerPhase = StorytellerPhase.NIGHT) = SelectionAuditKey(
        familyId = familyId,
        playerCount = 5,
        phase = phase,
        style = RecommendationStyle.BALANCED,
    )

    private fun totals(eligible: Long, selected: Long) = SelectionAuditTotals(
        familyOpportunityCount = eligible,
        familyEligibleCount = eligible,
        familyHighestTierCount = eligible,
        familySelectedCount = selected,
    )
}
