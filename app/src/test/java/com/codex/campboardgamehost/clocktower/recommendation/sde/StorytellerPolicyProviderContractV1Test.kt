package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerPolicyProviderContractV1Test {
    @Test
    fun acceptsOnlyCurrentLegalCandidateIds() {
        val request = request()
        val response = StorytellerPolicyResponseV1(
            decisionId = request.input.decisionId,
            sourceRevision = request.input.sourceRevision,
            primary = StorytellerPolicyRecommendationV1("a", rationale = listOf("global interaction")),
            alternatives = listOf(StorytellerPolicyRecommendationV1("b")),
            confidence = StorytellerPolicyConfidenceV1.MEDIUM,
        )

        assertEquals(
            StorytellerPolicyValidationV1.Accepted("a", listOf("b")),
            StorytellerPolicyResponseValidatorV1.validate(request, response),
        )
    }

    @Test
    fun rejectsStaleOrInventedProviderOutputWithoutChangingLegalDomain() {
        val request = request()
        val response = StorytellerPolicyResponseV1(
            decisionId = request.input.decisionId,
            sourceRevision = InformationDecisionRevision(99, 99),
            primary = StorytellerPolicyRecommendationV1("invented"),
            alternatives = listOf(StorytellerPolicyRecommendationV1("b")),
        )

        val result = StorytellerPolicyResponseValidatorV1.validate(request, response)
        assertTrue(result is StorytellerPolicyValidationV1.Rejected)
        result as StorytellerPolicyValidationV1.Rejected
        assertTrue(StorytellerPolicyValidationFailureV1.STALE_SOURCE_REVISION in result.reasons)
        assertTrue(StorytellerPolicyValidationFailureV1.UNKNOWN_PRIMARY_CANDIDATE in result.reasons)
        assertEquals(listOf("a", "b"), request.input.legalCandidateIds)
    }

    private fun request(): StorytellerPolicyRequestV1 {
        val revision = InformationDecisionRevision(4, 2)
        return StorytellerPolicyRequestV1(
            input = RecommendationDecisionInputV1(
                snapshot = TroubleBrewingGameSnapshotV1(
                    gameId = "gsp2a-test",
                    gameSeed = 1L,
                    position = TroubleBrewingSnapshotPosition(
                        stage = TroubleBrewingSnapshotStage.SETUP_PRECOMMIT,
                        phase = SnapshotField.NotApplicable,
                        round = SnapshotField.NotApplicable,
                    ),
                    grimoireSeats = emptyList(),
                    setupState = TroubleBrewingSnapshotSetupState(
                        hasDrunk = SnapshotField.Known(false),
                        drunkAssignmentSeat = SnapshotField.NotApplicable,
                    ),
                ),
                decisionId = "decision:test",
                lifecycleStage = SdeDecisionLifecycleStage.SetupPrecommit,
                sourceRevision = revision,
                historyPrefixRef = SdeHistoricalPrefixRef.Global(
                    gameId = "gsp2a-test",
                    actionRefs = emptyList(),
                    observationRefs = emptyList(),
                ),
                legalCandidateIds = listOf("a", "b"),
                context = RecommendationDecisionContextV1.DrunkAssignment(
                    legalCandidates = listOf(
                        RecommendationDrunkCandidateContextV1("a", 1, "Empath"),
                        RecommendationDrunkCandidateContextV1("b", 2, "Monk"),
                    ),
                ),
                featureProjection = RecommendationFeatureProjectionV1.DrunkAssignment(
                    evaluation = DrunkAssignmentFeatureEvaluation(
                        candidates = emptyList(),
                    ),
                ),
            ),
        )
    }
}
