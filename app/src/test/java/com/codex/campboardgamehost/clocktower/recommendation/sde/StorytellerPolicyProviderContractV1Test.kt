package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerPolicyProviderContractV1Test {
    @Test
    fun acceptsOnlyCurrentLegalCandidateIds() {
        val request = request()
        val response = StorytellerPolicyResponseV1(
            decisionId = request.input.decisionId,
            sourceRevision = request.input.sourceRevision,
            outcome = StorytellerPolicyOutcomeV1.Recommendation(
                primary = StorytellerPolicyRecommendationV1(
                    candidateId = "a",
                    rationale = listOf("global interaction"),
                ),
                alternatives = listOf(StorytellerPolicyRecommendationV1("b")),
            ),
            confidence = StorytellerPolicyConfidenceV1.MEDIUM,
        )

        assertEquals(
            StorytellerPolicyValidationV1.AcceptedRecommendation("a", listOf("b")),
            StorytellerPolicyResponseValidatorV1.validate(request, response),
        )
    }

    @Test
    fun acceptsExplicitDeferralWithoutManufacturingAChoice() {
        val request = request()
        val response = StorytellerPolicyResponseV1(
            decisionId = request.input.decisionId,
            sourceRevision = request.input.sourceRevision,
            outcome = StorytellerPolicyOutcomeV1.Deferred(
                reasons = listOf("insufficient longitudinal context"),
                missingContext = listOf("public claims"),
            ),
            confidence = StorytellerPolicyConfidenceV1.LOW,
        )

        assertEquals(
            StorytellerPolicyValidationV1.AcceptedDeferral(
                reasons = listOf("insufficient longitudinal context"),
                missingContext = listOf("public claims"),
            ),
            StorytellerPolicyResponseValidatorV1.validate(request, response),
        )
    }

    @Test
    fun rejectsStaleOrInventedProviderOutputWithoutChangingLegalDomain() {
        val request = request()
        val response = StorytellerPolicyResponseV1(
            decisionId = request.input.decisionId,
            sourceRevision = InformationDecisionRevision(99, 99),
            outcome = StorytellerPolicyOutcomeV1.Recommendation(
                primary = StorytellerPolicyRecommendationV1("invented"),
                alternatives = listOf(StorytellerPolicyRecommendationV1("b")),
            ),
        )

        val result = StorytellerPolicyResponseValidatorV1.validate(request, response)
        assertTrue(result is StorytellerPolicyValidationV1.Rejected)
        result as StorytellerPolicyValidationV1.Rejected
        assertTrue(StorytellerPolicyValidationFailureV1.STALE_SOURCE_REVISION in result.reasons)
        assertTrue(StorytellerPolicyValidationFailureV1.UNKNOWN_PRIMARY_CANDIDATE in result.reasons)
        assertEquals(listOf("a", "b"), request.input.legalCandidateIds)
    }

    @Test
    fun requestFailsClosedWhenFeatureCandidateOrderDoesNotMatchLegalDomain() {
        val valid = input()
        val mismatched = valid.copy(
            legalCandidateIds = listOf("b", "a"),
        )

        assertThrows(IllegalArgumentException::class.java) {
            StorytellerPolicyRequestV1(
                decisionType = RecommendationDecisionExportTypeV1.DRUNK_ASSIGNMENT,
                input = mismatched,
            )
        }
    }

    private fun request(): StorytellerPolicyRequestV1 =
        StorytellerPolicyRequestV1(
            decisionType = RecommendationDecisionExportTypeV1.DRUNK_ASSIGNMENT,
            input = input(),
        )

    private fun input(): RecommendationDecisionInputV1 {
        val revision = InformationDecisionRevision(4, 2)
        return RecommendationDecisionInputV1(
            snapshot = TroubleBrewingGameSnapshotV1(
                gameId = "gsp2a-test",
                gameSeed = 1L,
                position = TroubleBrewingSnapshotPosition(
                    stage = TroubleBrewingSnapshotStage.SETUP_PRECOMMIT,
                    phase = SnapshotField.NotApplicable,
                    round = SnapshotField.NotApplicable,
                ),
                grimoireSeats = listOf(
                    TroubleBrewingSnapshotSeat(
                        seat = 1,
                        shownRoleId = SnapshotField.Known("Empath"),
                        actualRoleId = SnapshotField.Uncommitted,
                        alive = SnapshotField.Known(true),
                        poisoned = SnapshotField.NotApplicable,
                    ),
                    TroubleBrewingSnapshotSeat(
                        seat = 2,
                        shownRoleId = SnapshotField.Known("Monk"),
                        actualRoleId = SnapshotField.Uncommitted,
                        alive = SnapshotField.Known(true),
                        poisoned = SnapshotField.NotApplicable,
                    ),
                ),
                setupState = TroubleBrewingSnapshotSetupState(
                    hasDrunk = SnapshotField.Known(true),
                    drunkAssignmentSeat = SnapshotField.Uncommitted,
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
                evaluation = drunkFeatures(),
            ),
        )
    }

    private fun drunkFeatures(): DrunkAssignmentFeatureEvaluation =
        DrunkAssignmentFeatureEvaluation(
            candidates = listOf("a", "b").mapIndexed { index, candidateId ->
                CandidateDrunkAssignmentFeatures(
                    candidateId = candidateId,
                    features = DrunkAssignmentFeatures(
                        topology = FeatureProjection.Projected(
                            DrunkAssignmentTopologyFeatures(
                                previousSeat = if (index == 0) 2 else 1,
                                nextSeat = if (index == 0) 2 else 1,
                                adjacentEvilSeats = emptySet(),
                                adjacentDemonSeats = emptySet(),
                                adjacentMinionSeats = emptySet(),
                            ),
                        ),
                        firstNightInformationOpportunity = FeatureProjection.Projected(
                            DrunkAssignmentFirstNightInformationOpportunityFeatures(
                                factors = emptyList(),
                            ),
                        ),
                        longitudinalNarrativeOpportunity = FeatureProjection.Unavailable(
                            FeatureUnavailableReason.MISSING_CAPABILITY,
                        ),
                        limitations = DrunkAssignmentFeatureLimitations(
                            excludedPlayerControlledElements = emptySet(),
                            deferredComplexities = emptySet(),
                        ),
                    ),
                )
            },
        )
}
