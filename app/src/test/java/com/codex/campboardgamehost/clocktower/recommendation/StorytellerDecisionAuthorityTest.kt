package com.codex.campboardgamehost.clocktower.recommendation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerDecisionAuthorityTest {
    @Test
    fun singleLegalCandidateIsRuleDeterministic() {
        assertEquals(
            StorytellerDecisionAuthority.RuleDeterministic,
            storytellerDecisionAuthority(legalCandidateCount = 1),
        )
    }

    @Test
    fun acceptedPolicyOwnsMultipleLegalCandidates() {
        assertEquals(
            StorytellerDecisionAuthority.PolicyReady("TEST_POLICY_V1"),
            storytellerDecisionAuthority(
                legalCandidateCount = 3,
                acceptedPolicyVersion = "TEST_POLICY_V1",
            ),
        )
    }

    @Test
    fun unsupportedMultipleChoiceFailsClosedToManual() {
        assertEquals(
            StorytellerDecisionAuthority.ManualRequired(
                StorytellerManualReason.POLICY_NOT_READY,
            ),
            storytellerDecisionAuthority(legalCandidateCount = 2),
        )
    }

    @Test
    fun beginnerPresentationRemainsAutomaticOnlyWhenAuthorityAllowsIt() {
        assertTrue(
            storytellerDecisionPresentationIsAutomatic(
                automaticStorytellerInfo = true,
                authority = StorytellerDecisionAuthority.RuleDeterministic,
            ),
        )
        assertTrue(
            storytellerDecisionPresentationIsAutomatic(
                automaticStorytellerInfo = true,
                authority = StorytellerDecisionAuthority.PolicyReady("TEST_POLICY_V1"),
            ),
        )
        assertFalse(
            storytellerDecisionPresentationIsAutomatic(
                automaticStorytellerInfo = true,
                authority = StorytellerDecisionAuthority.ManualRequired(
                    StorytellerManualReason.POLICY_NOT_READY,
                ),
            ),
        )
        assertFalse(
            storytellerDecisionPresentationIsAutomatic(
                automaticStorytellerInfo = false,
                authority = StorytellerDecisionAuthority.PolicyReady("TEST_POLICY_V1"),
            ),
        )
    }
}
