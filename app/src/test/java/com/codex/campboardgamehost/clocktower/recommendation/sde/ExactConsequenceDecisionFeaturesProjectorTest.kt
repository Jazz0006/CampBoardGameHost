package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExactConsequenceDecisionFeaturesProjectorTest {
    @Test
    fun `ready projection preserves legal candidate identity and order`() {
        val candidateIds = listOf("numeric:0", "numeric:1")
        val evaluation = ExactConsequenceEvaluation.Ready(
            consequences = candidateIds.mapIndexed { index, candidateId ->
                CandidateConsequence(
                    candidateId = candidateId,
                    diagnostics = diagnostics(
                        candidateId = candidateId,
                        demonSeats = if (index == 0) setOf(1, 2) else setOf(1),
                    ),
                )
            },
        )

        val projected = ExactConsequenceDecisionFeaturesProjector.project(
            evaluation = evaluation,
            legalCandidateIds = candidateIds,
            playerCount = 5,
        )

        assertTrue(projected is DecisionFeatureEvaluation.Ready)
        val ready = projected as DecisionFeatureEvaluation.Ready
        assertEquals(candidateIds, ready.candidates.map(CandidateDecisionFeatures::candidateId))
        assertTrue(ready.candidates.all { it.features.strategic is FeatureProjection.Projected })
    }

    @Test
    fun `deferred projection preserves legal candidates and exact missing capabilities without inventing features`() {
        val candidateIds = listOf("numeric:0", "numeric:1", "numeric:2")
        val missing = setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY)

        val projected = ExactConsequenceDecisionFeaturesProjector.project(
            evaluation = ExactConsequenceEvaluation.Deferred(missingCapabilities = missing),
            legalCandidateIds = candidateIds,
            playerCount = 5,
        )

        assertTrue(projected is DecisionFeatureEvaluation.Deferred)
        val deferred = projected as DecisionFeatureEvaluation.Deferred
        assertEquals(candidateIds, deferred.candidateIds)
        assertEquals(missing, deferred.missingCapabilities)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ready projection rejects consequence order drift from legal candidate authority`() {
        val legalCandidateIds = listOf("numeric:0", "numeric:1")
        val evaluation = ExactConsequenceEvaluation.Ready(
            consequences = listOf(
                CandidateConsequence("numeric:1", diagnostics("numeric:1", setOf(1))),
                CandidateConsequence("numeric:0", diagnostics("numeric:0", setOf(1, 2))),
            ),
        )

        ExactConsequenceDecisionFeaturesProjector.project(
            evaluation = evaluation,
            legalCandidateIds = legalCandidateIds,
            playerCount = 5,
        )
    }

    private fun diagnostics(
        candidateId: String,
        demonSeats: Set<Int>,
    ): ExactHypotheticalObservationBundleDiagnostics {
        val before = structure(setOf(1, 2, 3))
        val after = structure(demonSeats)
        return ExactHypotheticalObservationBundleDiagnostics(
            bundleId = candidateId,
            recipientSeat = 4,
            before = WorldCardinality.Exact(BigInteger.valueOf(20)),
            after = WorldCardinality.Exact(BigInteger.valueOf(8)),
            beforeStructure = before,
            afterStructure = after,
        )
    }

    private fun structure(
        demonSeats: Set<Int>,
    ): ExactWorldStructureDiagnostics {
        val keys = demonSeats.mapTo(linkedSetOf()) { demonSeat ->
            StrategicWorldKey(demonSeat = demonSeat, minionSeats = listOf(5))
        }
        val evilTeams = keys.mapTo(linkedSetOf()) { key -> setOf(key.demonSeat) + key.minionSeats }
        return ExactWorldStructureDiagnostics(
            possibleDemonSeats = demonSeats.toSortedSet(),
            evilTeamSeatConfigurations = evilTeams,
            strategicWorldKeys = keys,
            forcedGoodSeats = emptySet(),
            forcedEvilSeats = emptySet(),
            evilCoverSeats = evilTeams.flatten().toSortedSet(),
        )
    }
}
