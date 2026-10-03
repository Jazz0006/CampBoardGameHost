package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BeginnerConservativeV2PolicyTest {
    @Test
    fun `admitted Outsider pair preference narrows only the V1 survivor band`() {
        val features = ready(
            "preferred" to features(futureReason = true),
            "ordinary" to features(futureReason = false),
            "collapsed" to features(
                topology = StrategicRatio.Defined(0, 7),
                futureReason = true,
            ),
        )

        val evaluation = BeginnerConservativeV2Policy.evaluate(features, admittedScope())
        assertTrue(evaluation is BeginnerConservativeV2PolicyEvaluation.Ready)
        evaluation as BeginnerConservativeV2PolicyEvaluation.Ready

        assertTrue(evaluation.preferenceApplied)
        assertEquals(
            BeginnerConservativeV2SelectionMode.PREFERRED_BAND,
            evaluation.selectionMode,
        )
        assertEquals(
            listOf(
                PolicyDisposition.SURVIVOR,
                PolicyDisposition.ACCEPTED,
                PolicyDisposition.REJECTED,
            ),
            evaluation.evaluations.map(PolicyEvaluation::disposition),
        )
        assertEquals(
            setOf(BeginnerConservativeV2PolicyReasons.PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES),
            evaluation.evaluations[0].softPreferenceReasons,
        )
        assertEquals(
            setOf(BeginnerConservativeV1PolicyReasons.NO_CREDIBLE_EVIL_WORLD),
            evaluation.evaluations[2].rejectionReasons,
        )
        assertEquals(
            "preferred",
            BeginnerConservativeV2Selector.select(
                evaluation = evaluation,
                decisionId = "decision-v2-preferred",
                selectionSeed = 44L,
            )?.candidateId,
        )
    }

    @Test
    fun `multiple preferred survivors stay tied and seeded selection stays inside preferred band`() {
        val evaluation = BeginnerConservativeV2Policy.evaluate(
            ready(
                "preferred-a" to features(futureReason = true),
                "ordinary" to features(futureReason = false),
                "preferred-b" to features(futureReason = true),
            ),
            admittedScope(),
        ) as BeginnerConservativeV2PolicyEvaluation.Ready

        val preferred = evaluation.evaluations.filter { it.disposition == PolicyDisposition.SURVIVOR }
        assertEquals(setOf("preferred-a", "preferred-b"), preferred.mapTo(linkedSetOf()) { it.candidateId })
        assertTrue(
            preferred.all {
                (it.equivalenceState as PolicyEquivalenceState.Tied).candidateIds ==
                    setOf("preferred-a", "preferred-b")
            },
        )

        val selected = BeginnerConservativeV2Selector.select(
            evaluation = evaluation,
            decisionId = "decision-v2-tie",
            selectionSeed = 92L,
        )
        assertTrue(selected?.candidateId in setOf("preferred-a", "preferred-b"))
        assertEquals(PolicyVersions.BEGINNER_CONSERVATIVE_V2, selected?.policyVersion)
        assertEquals(PolicySelectionMethod.SEEDED_HASH_V1, selected?.method)
    }

    @Test
    fun `outside admitted pair surface falls back to exact V1 selection and equivalence`() {
        val features = ready(
            "a" to features(futureReason = true),
            "b" to features(futureReason = false),
        )
        val v1 = BeginnerConservativeV1Policy.evaluate(features) as BeginnerConservativePolicyEvaluation.Ready
        val v1Selection = BeginnerConservativeV1Selector.select(v1, "decision-fallback-scope", 81L)

        val v2 = BeginnerConservativeV2Policy.evaluate(
            features,
            admittedScope(targetType = CharacterType.TOWNSFOLK),
        ) as BeginnerConservativeV2PolicyEvaluation.Ready
        val v2Selection = BeginnerConservativeV2Selector.select(v2, "decision-fallback-scope", 81L)

        assertFalse(v2.preferenceApplied)
        assertEquals(BeginnerConservativeV2SelectionMode.V1_FALLBACK, v2.selectionMode)
        assertV1Equivalent(v1, v2)
        assertEquals(v1Selection?.candidateId, v2Selection?.candidateId)
    }

    @Test
    fun `unavailable future flexibility falls back exactly to V1`() {
        val features = ready(
            "a" to features(futureReason = true),
            "b" to features(
                futureProjection = FeatureProjection.Unavailable(
                    FeatureUnavailableReason.MISSING_CAPABILITY,
                ),
            ),
        )
        val v1 = BeginnerConservativeV1Policy.evaluate(features) as BeginnerConservativePolicyEvaluation.Ready
        val v2 = BeginnerConservativeV2Policy.evaluate(
            features,
            admittedScope(),
        ) as BeginnerConservativeV2PolicyEvaluation.Ready

        assertFalse(v2.preferenceApplied)
        assertV1Equivalent(v1, v2)
        assertEquals(
            BeginnerConservativeV1Selector.select(v1, "decision-unavailable", 19L)?.candidateId,
            BeginnerConservativeV2Selector.select(v2, "decision-unavailable", 19L)?.candidateId,
        )
    }

    @Test
    fun `no proper preferred subset falls back exactly to V1`() {
        val features = ready(
            "a" to features(futureReason = true),
            "b" to features(futureReason = true),
        )
        val v1 = BeginnerConservativeV1Policy.evaluate(features) as BeginnerConservativePolicyEvaluation.Ready
        val v2 = BeginnerConservativeV2Policy.evaluate(
            features,
            admittedScope(),
        ) as BeginnerConservativeV2PolicyEvaluation.Ready

        assertFalse(v2.preferenceApplied)
        assertV1Equivalent(v1, v2)
        assertEquals(
            BeginnerConservativeV1Selector.select(v1, "decision-no-delta", 55L)?.candidateId,
            BeginnerConservativeV2Selector.select(v2, "decision-no-delta", 55L)?.candidateId,
        )
    }

    @Test
    fun `unrelated feature dimensions cannot change V2 preference result`() {
        val baseline = ready(
            "preferred" to features(futureReason = true),
            "ordinary" to features(futureReason = false),
        )
        val unrelated = ready(
            "preferred" to features(
                futureReason = true,
                bluffNarrative = FeatureProjection.Projected(
                    BluffNarrativeFeatures(
                        claimBurdenReasonCodes = setOf("unrelated-a"),
                    ),
                ),
            ),
            "ordinary" to features(
                futureReason = false,
                bluffNarrative = FeatureProjection.Projected(
                    BluffNarrativeFeatures(
                        claimBurdenReasonCodes = setOf("unrelated-b"),
                    ),
                ),
            ),
        )

        val baselineEval = BeginnerConservativeV2Policy.evaluate(
            baseline,
            admittedScope(),
        ) as BeginnerConservativeV2PolicyEvaluation.Ready
        val unrelatedEval = BeginnerConservativeV2Policy.evaluate(
            unrelated,
            admittedScope(),
        ) as BeginnerConservativeV2PolicyEvaluation.Ready

        assertEquals(
            baselineEval.evaluations.map { it.candidateId to it.disposition },
            unrelatedEval.evaluations.map { it.candidateId to it.disposition },
        )
        assertEquals(
            BeginnerConservativeV2Selector.select(baselineEval, "decision-unrelated", 14L)?.candidateId,
            BeginnerConservativeV2Selector.select(unrelatedEval, "decision-unrelated", 14L)?.candidateId,
        )
    }

    @Test
    fun `V2 definition is evidence bound and replay registry keeps immutable V1 alongside V2`() {
        assertEquals(
            EvidenceCheckpointId(
                "clocktower-evidence-lab-g10-librarian-future-flexibility-" +
                    "78f672868ea6603317aeefa20ad91686c5886db9",
            ),
            StorytellerPolicyDefinitions.BEGINNER_CONSERVATIVE_V2.evidenceCheckpoint,
        )
        assertEquals(
            setOf(
                PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                PolicyVersions.BEGINNER_CONSERVATIVE_V2,
            ),
            DecisionPolicyReplayRegistry.production().supportedVersions,
        )
    }

    private fun assertV1Equivalent(
        v1: BeginnerConservativePolicyEvaluation.Ready,
        v2: BeginnerConservativeV2PolicyEvaluation.Ready,
    ) {
        assertEquals(v1.candidateIds, v2.candidateIds)
        assertEquals(
            v1.evaluations.map {
                Triple(it.candidateId, it.disposition, it.equivalenceState)
            },
            v2.evaluations.map {
                Triple(it.candidateId, it.disposition, it.equivalenceState)
            },
        )
        assertEquals(
            v1.evaluations.map(PolicyEvaluation::rejectionReasons),
            v2.evaluations.map(PolicyEvaluation::rejectionReasons),
        )
    }

    private fun admittedScope(
        targetType: CharacterType = CharacterType.OUTSIDER,
    ): DecisionPolicyReplayScope.PairInformation = DecisionPolicyReplayScope.PairInformation(
        script = ScriptId("trouble_brewing"),
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        targetType = targetType,
        reliability = ReliabilityState.RELIABLE,
        truthfulLegalOutcomes = true,
    )

    private fun ready(
        vararg candidates: Pair<String, DecisionFeatures>,
    ): DecisionFeatureEvaluation.Ready = DecisionFeatureEvaluation.Ready(
        candidates = candidates.map { (candidateId, features) ->
            CandidateDecisionFeatures(candidateId, features)
        },
    )

    private fun features(
        topology: StrategicRatio = StrategicRatio.Defined(6, 6),
        futureReason: Boolean = false,
        futureProjection: FeatureProjection<FutureFlexibilityFeatures>? = null,
        bluffNarrative: FeatureProjection<BluffNarrativeFeatures> =
            FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
    ): DecisionFeatures {
        val future = futureProjection ?: FeatureProjection.Projected(
            FutureFlexibilityFeatures(
                retainedRouteIds = if (futureReason) setOf("route-a", "route-b") else setOf("route-a"),
                reasonCodes = if (futureReason) {
                    setOf(
                        PairInformationFutureFlexibilityReasonCodes
                            .PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES,
                    )
                } else {
                    emptySet()
                },
            ),
        )
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
            bluffNarrative = bluffNarrative,
            futureFlexibility = future,
        )
    }
}
