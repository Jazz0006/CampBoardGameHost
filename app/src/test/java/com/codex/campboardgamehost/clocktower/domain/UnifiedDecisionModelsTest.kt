package com.codex.campboardgamehost.clocktower.domain

import org.junit.Test

class UnifiedDecisionModelsTest {
    private val metadata = CandidateMetadata(
        candidateSchemaVersion = "1",
        decisionType = "numeric-information",
    )

    @Test(expected = IllegalArgumentException::class)
    fun `registered truth cannot exist without a registration fact`() {
        DecisionCandidate(
            candidateId = "candidate-3",
            candidateFamilyId = "registration-recluse",
            outcome = 1,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_REGISTERED_STATE,
            metadata = metadata,
        )
    }
}
