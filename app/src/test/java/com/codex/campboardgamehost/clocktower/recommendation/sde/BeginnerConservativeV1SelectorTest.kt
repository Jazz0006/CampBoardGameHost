package com.codex.campboardgamehost.clocktower.recommendation.sde

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BeginnerConservativeV1SelectorTest {
    @Test
    fun `selection is stable and independent of evaluation iteration order`() {
        val first = ready("a", "b", "c")
        val reversed = ready("c", "b", "a")

        val selectedA = BeginnerConservativeV1Selector.select(
            evaluation = first,
            decisionId = "decision-1",
            selectionSeed = 42L,
        )
        val selectedB = BeginnerConservativeV1Selector.select(
            evaluation = reversed,
            decisionId = "decision-1",
            selectionSeed = 42L,
        )

        assertEquals(selectedA, selectedB)
    }

    @Test
    fun `selector only selects survivors and ignores accepted or rejected candidates`() {
        val evaluation = BeginnerConservativePolicyEvaluation.Ready(
            evaluations = listOf(
                PolicyEvaluation(
                    candidateId = "rejected",
                    policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                    disposition = PolicyDisposition.REJECTED,
                    rejectionReasons = setOf(BeginnerConservativeV1PolicyReasons.NO_CREDIBLE_EVIL_WORLD),
                ),
                PolicyEvaluation(
                    candidateId = "accepted",
                    policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                    disposition = PolicyDisposition.ACCEPTED,
                ),
                PolicyEvaluation(
                    candidateId = "survivor",
                    policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                    disposition = PolicyDisposition.SURVIVOR,
                    equivalenceState = PolicyEquivalenceState.Unique,
                ),
            ),
        )

        assertEquals(
            PolicySelection(
                policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                candidateId = "survivor",
                method = PolicySelectionMethod.SEEDED_HASH_V1,
            ),
            BeginnerConservativeV1Selector.select(evaluation, "decision-2", 99L),
        )
    }

    @Test
    fun `deferred policy has no selection`() {
        val deferred = BeginnerConservativePolicyEvaluation.Deferred(
            candidateIds = listOf("a", "b"),
            reasons = setOf(BeginnerConservativePolicyDeferralReason.STRATEGIC_FEATURE_UNAVAILABLE),
        )

        assertNull(BeginnerConservativeV1Selector.select(deferred, "decision-3", 1L))
    }

    private fun ready(
        vararg candidateIds: String,
    ): BeginnerConservativePolicyEvaluation.Ready {
        val tie = if (candidateIds.size == 1) {
            PolicyEquivalenceState.Unique
        } else {
            PolicyEquivalenceState.Tied(candidateIds.toSet())
        }
        return BeginnerConservativePolicyEvaluation.Ready(
            evaluations = candidateIds.map { candidateId ->
                PolicyEvaluation(
                    candidateId = candidateId,
                    policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                    disposition = PolicyDisposition.SURVIVOR,
                    equivalenceState = tie,
                )
            },
        )
    }
}
