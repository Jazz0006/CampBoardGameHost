package com.codex.campboardgamehost.clocktower.recommendation.sde

import org.junit.Assert.assertEquals
import org.junit.Test

class PolicyEvaluationContractTest {
    @Test
    fun `survivor can retain an explicit equivalence tie without a scalar score`() {
        val evaluation = PolicyEvaluation(
            candidateId = "candidate-a",
            policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
            disposition = PolicyDisposition.SURVIVOR,
            softPreferenceReasons = setOf(PolicyReasonCode("healthy-middle-band")),
            equivalenceState = PolicyEquivalenceState.Tied(
                candidateIds = setOf("candidate-a", "candidate-b"),
            ),
        )

        assertEquals(PolicyDisposition.SURVIVOR, evaluation.disposition)
        assertEquals(
            setOf("candidate-a", "candidate-b"),
            (evaluation.equivalenceState as PolicyEquivalenceState.Tied).candidateIds,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejected candidate requires an explicit rejection reason`() {
        PolicyEvaluation(
            candidateId = "candidate-a",
            policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
            disposition = PolicyDisposition.REJECTED,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `tie must include the evaluated candidate`() {
        PolicyEvaluation(
            candidateId = "candidate-a",
            policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
            disposition = PolicyDisposition.SURVIVOR,
            equivalenceState = PolicyEquivalenceState.Tied(setOf("candidate-b", "candidate-c")),
        )
    }
}
