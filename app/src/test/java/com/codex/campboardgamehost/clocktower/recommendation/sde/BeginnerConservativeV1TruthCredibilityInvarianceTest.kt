package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.RoleId
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BeginnerConservativeV1TruthCredibilityInvarianceTest {
    @Test
    fun `typed truth credibility cannot change V1 rejection survivor or seeded selection semantics`() {
        val baseline = DecisionFeatureEvaluation.Ready(
            listOf(
                CandidateDecisionFeatures(
                    "collapsed",
                    policyNeutralFeatures(
                        topology = StrategicRatio.Defined(0, 7),
                        truthCredibility =
                            FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
                    ),
                ),
                CandidateDecisionFeatures(
                    "viable",
                    policyNeutralFeatures(
                        topology = StrategicRatio.Defined(2, 7),
                        truthCredibility =
                            FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
                    ),
                ),
            ),
        )
        val source = ConfirmationChannelRef.Source(
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
        )
        val typedTruthCredibility = FeatureProjection.Projected(
            TruthCredibilityFeatures(
                truthDangerSources = setOf(
                    TruthDangerSourceImpact(
                        source = source,
                        exactWorldReduction = BigInteger.ONE,
                        strategicWorldKeysRemoved = emptySet(),
                        demonSeatsRemoved = emptySet(),
                    ),
                ),
            ),
        )
        val enriched = DecisionFeatureEvaluation.Ready(
            baseline.candidates.map { candidate ->
                candidate.copy(
                    features = candidate.features.copy(
                        truthCredibility = typedTruthCredibility,
                    ),
                )
            },
        )

        val baselinePolicy =
            BeginnerConservativeV1Policy.evaluate(baseline) as BeginnerConservativePolicyEvaluation.Ready
        val enrichedPolicy =
            BeginnerConservativeV1Policy.evaluate(enriched) as BeginnerConservativePolicyEvaluation.Ready

        assertEquals(baselinePolicy.evaluations, enrichedPolicy.evaluations)
        assertEquals(
            setOf(BeginnerConservativeV1PolicyReasons.NO_CREDIBLE_EVIL_WORLD),
            enrichedPolicy.evaluations.single { it.candidateId == "collapsed" }.rejectionReasons,
        )
        assertTrue(
            enrichedPolicy.evaluations.single { it.candidateId == "viable" }.equivalenceState ===
                PolicyEquivalenceState.Unique,
        )

        val baselineSelection = BeginnerConservativeV1Selector.select(
            evaluation = baselinePolicy,
            decisionId = "c4-v1-invariance",
            selectionSeed = 20260926L,
        )
        val enrichedSelection = BeginnerConservativeV1Selector.select(
            evaluation = enrichedPolicy,
            decisionId = "c4-v1-invariance",
            selectionSeed = 20260926L,
        )
        assertEquals(baselineSelection, enrichedSelection)
        assertEquals(PolicySelectionMethod.SEEDED_HASH_V1, enrichedSelection?.method)

        assertEquals(
            setOf(
                BeginnerConservativeV1PolicyLimitations.PREFERENCE_DIMENSIONS_NOT_PROJECTED,
                BeginnerConservativeV1PolicyLimitations.PREFERENCE_EVIDENCE_NOT_AUTHORIZED,
            ),
            baselinePolicy.limitations,
        )
        assertEquals(
            setOf(BeginnerConservativeV1PolicyLimitations.PREFERENCE_EVIDENCE_NOT_AUTHORIZED),
            enrichedPolicy.limitations,
        )
        assertEquals(
            EvidenceCheckpointId("sde-3b-merged-2026-09-24"),
            StorytellerPolicyDefinitions.BEGINNER_CONSERVATIVE_V1.evidenceCheckpoint,
        )
    }

    private fun policyNeutralFeatures(
        topology: StrategicRatio,
        truthCredibility: FeatureProjection<TruthCredibilityFeatures>,
    ): DecisionFeatures {
        val notApplicable = FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_APPLICABLE)
        return DecisionFeatures(
            strategic = FeatureProjection.Projected(
                StrategicDecisionFeatures(
                    demonCoverRetention = StrategicRatio.Defined(3, 3),
                    evilTopologyRetention = topology,
                    evilCoverRetention = StrategicRatio.Defined(4, 4),
                    forcedGoodFraction = StrategicRatio.Defined(0, 5),
                    forcedEvilFraction = StrategicRatio.Defined(0, 5),
                    forcedGoodSeats = emptySet(),
                    forcedEvilSeats = emptySet(),
                ),
            ),
            confirmationChainImpact = notApplicable,
            healthyInformationUtility = notApplicable,
            truthCredibility = truthCredibility,
            roleFunctionExposure = notApplicable,
            semanticTruth = notApplicable,
            impairedNarrative = notApplicable,
            bluffNarrative = notApplicable,
            relationships = notApplicable,
            futureFlexibility = notApplicable,
        )
    }
}