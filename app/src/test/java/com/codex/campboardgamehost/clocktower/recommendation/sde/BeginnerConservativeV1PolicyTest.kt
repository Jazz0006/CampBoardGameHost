package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BeginnerConservativeV1PolicyTest {
    @Test
    fun `upstream deferred features defer policy without fabricating candidate evaluations`() {
        val result = BeginnerConservativeV1Policy.evaluate(
            DecisionFeatureEvaluation.Deferred(
                candidateIds = listOf("a", "b"),
                missingCapabilities = setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY),
            ),
        )

        assertTrue(result is BeginnerConservativePolicyEvaluation.Deferred)
        result as BeginnerConservativePolicyEvaluation.Deferred
        assertEquals(listOf("a", "b"), result.candidateIds)
        assertEquals(
            setOf(BeginnerConservativePolicyDeferralReason.UPSTREAM_FEATURE_EVALUATION_DEFERRED),
            result.reasons,
        )
    }

    @Test
    fun `missing strategic projection defers policy instead of treating missing as neutral`() {
        val result = BeginnerConservativeV1Policy.evaluate(
            ready(
                "a" to DecisionFeatures.unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
                "b" to features(),
            ),
        )

        assertTrue(result is BeginnerConservativePolicyEvaluation.Deferred)
        assertEquals(
            setOf(BeginnerConservativePolicyDeferralReason.STRATEGIC_FEATURE_UNAVAILABLE),
            (result as BeginnerConservativePolicyEvaluation.Deferred).reasons,
        )
    }

    @Test
    fun `undefined strategic baseline defers policy instead of inventing a healthy interpretation`() {
        val result = BeginnerConservativeV1Policy.evaluate(
            ready(
                "a" to features(topology = StrategicRatio.Undefined),
                "b" to features(),
            ),
        )

        assertTrue(result is BeginnerConservativePolicyEvaluation.Deferred)
        assertEquals(
            setOf(BeginnerConservativePolicyDeferralReason.STRATEGIC_BASELINE_UNDEFINED),
            (result as BeginnerConservativePolicyEvaluation.Deferred).reasons,
        )
    }

    @Test
    fun `zero retained Evil structure is rejected when a non contradictory alternative exists`() {
        val result = BeginnerConservativeV1Policy.evaluate(
            ready(
                "collapsed" to features(topology = StrategicRatio.Defined(0, 7)),
                "viable" to features(topology = StrategicRatio.Defined(1, 7)),
            ),
        )

        assertTrue(result is BeginnerConservativePolicyEvaluation.Ready)
        result as BeginnerConservativePolicyEvaluation.Ready
        assertEquals(listOf("collapsed", "viable"), result.candidateIds)

        val collapsed = result.evaluations[0]
        assertEquals(PolicyDisposition.REJECTED, collapsed.disposition)
        assertEquals(
            setOf(BeginnerConservativeV1PolicyReasons.NO_CREDIBLE_EVIL_WORLD),
            collapsed.rejectionReasons,
        )

        val viable = result.evaluations[1]
        assertEquals(PolicyDisposition.SURVIVOR, viable.disposition)
        assertTrue(viable.equivalenceState === PolicyEquivalenceState.Unique)
    }

    @Test
    fun `zero live Demon cover alone is not treated as an impossible historical world`() {
        val result = BeginnerConservativeV1Policy.evaluate(
            ready(
                "dead-demon-history" to features(
                    demon = StrategicRatio.Defined(0, 3),
                    topology = StrategicRatio.Defined(2, 6),
                    evilCover = StrategicRatio.Defined(2, 4),
                ),
                "other" to features(),
            ),
        ) as BeginnerConservativePolicyEvaluation.Ready

        assertEquals(
            listOf(PolicyDisposition.SURVIVOR, PolicyDisposition.SURVIVOR),
            result.evaluations.map(PolicyEvaluation::disposition),
        )
    }

    @Test
    fun `non zero strategic retention is not rejected by an invented numeric threshold`() {
        val result = BeginnerConservativeV1Policy.evaluate(
            ready(
                "narrow" to features(
                    demon = StrategicRatio.Defined(1, 9),
                    topology = StrategicRatio.Defined(1, 40),
                    evilCover = StrategicRatio.Defined(1, 9),
                ),
                "broad" to features(),
            ),
        ) as BeginnerConservativePolicyEvaluation.Ready

        assertEquals(
            listOf(PolicyDisposition.SURVIVOR, PolicyDisposition.SURVIVOR),
            result.evaluations.map(PolicyEvaluation::disposition),
        )
        val tie = result.evaluations.map { it.equivalenceState as PolicyEquivalenceState.Tied }
        assertTrue(tie.all { it.candidateIds == setOf("narrow", "broad") })
        assertEquals(
            setOf(
                BeginnerConservativeV1PolicyLimitations.PREFERENCE_DIMENSIONS_NOT_PROJECTED,
                BeginnerConservativeV1PolicyLimitations.PREFERENCE_EVIDENCE_NOT_AUTHORIZED,
            ),
            result.limitations,
        )
    }

    @Test
    fun `all structurally contradictory candidates defer instead of inventing a survivor`() {
        val result = BeginnerConservativeV1Policy.evaluate(
            ready(
                "a" to features(topology = StrategicRatio.Defined(0, 5)),
                "b" to features(topology = StrategicRatio.Defined(0, 6)),
            ),
        )

        assertTrue(result is BeginnerConservativePolicyEvaluation.Deferred)
        assertEquals(
            setOf(BeginnerConservativePolicyDeferralReason.NO_NON_CONTRADICTORY_SURVIVOR),
            (result as BeginnerConservativePolicyEvaluation.Deferred).reasons,
        )
    }

    private fun ready(
        vararg candidates: Pair<String, DecisionFeatures>,
    ): DecisionFeatureEvaluation.Ready = DecisionFeatureEvaluation.Ready(
        candidates = candidates.map { (candidateId, features) ->
            CandidateDecisionFeatures(candidateId, features)
        },
    )

    private fun features(
        demon: StrategicRatio = StrategicRatio.Defined(3, 3),
        topology: StrategicRatio = StrategicRatio.Defined(6, 6),
        evilCover: StrategicRatio = StrategicRatio.Defined(4, 4),
    ): DecisionFeatures = DecisionFeatures(
        strategic = FeatureProjection.Projected(
            StrategicDecisionFeatures(
                demonCoverRetention = demon,
                evilTopologyRetention = topology,
                evilCoverRetention = evilCover,
                forcedGoodFraction = StrategicRatio.Defined(0, 5),
                forcedEvilFraction = StrategicRatio.Defined(0, 5),
                forcedGoodSeats = emptySet(),
                forcedEvilSeats = emptySet(),
            ),
        ),
    )
}
