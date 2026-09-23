package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionFeaturesTest {
    @Test
    fun `strategic feature projection keeps independent exact dimensions and no global score`() {
        val before = structure(
            keys = setOf(
                StrategicWorldKey(1, listOf(2)),
                StrategicWorldKey(1, listOf(3)),
                StrategicWorldKey(2, listOf(3)),
                StrategicWorldKey(3, listOf(2)),
            ),
            forcedGood = setOf(5),
            forcedEvil = emptySet(),
        )
        val after = structure(
            keys = setOf(
                StrategicWorldKey(1, listOf(2)),
                StrategicWorldKey(1, listOf(3)),
            ),
            forcedGood = setOf(4, 5),
            forcedEvil = setOf(1),
        )

        val features = DecisionFeaturesProjector.project(
            diagnostic = ExactHypotheticalObservationBundleDiagnostics(
                bundleId = "candidate-a",
                recipientSeat = 4,
                before = WorldCardinality.Exact(BigInteger.valueOf(20)),
                after = WorldCardinality.Exact(BigInteger.valueOf(8)),
                beforeStructure = before,
                afterStructure = after,
            ),
            playerCount = 5,
            semanticTruth = SemanticTruth.TRUE,
        )

        val strategic = (features.strategic as FeatureProjection.Projected<StrategicDecisionFeatures>).value
        assertEquals(StrategicRatio.Defined(1, 3), strategic.demonCoverRetention)
        assertEquals(StrategicRatio.Defined(2, 4), strategic.evilTopologyRetention)
        assertEquals(StrategicRatio.Defined(3, 3), strategic.evilCoverRetention)
        assertEquals(StrategicRatio.Defined(2, 5), strategic.forcedGoodFraction)
        assertEquals(StrategicRatio.Defined(1, 5), strategic.forcedEvilFraction)
        assertEquals(setOf(4, 5), strategic.forcedGoodSeats)
        assertEquals(setOf(1), strategic.forcedEvilSeats)
        assertEquals(SemanticTruth.TRUE, (features.semanticTruth as FeatureProjection.Projected<SemanticTruth>).value)
        assertTrue(features.confirmationChainImpact is FeatureProjection.Unavailable)
        assertTrue(features.impairedNarrative is FeatureProjection.Unavailable)
    }

    private fun structure(
        keys: Set<StrategicWorldKey>,
        forcedGood: Set<Int>,
        forcedEvil: Set<Int>,
    ): ExactWorldStructureDiagnostics {
        val evilConfigurations = keys.mapTo(linkedSetOf()) { setOf(it.demonSeat) + it.minionSeats }
        return ExactWorldStructureDiagnostics(
            possibleDemonSeats = keys.mapTo(sortedSetOf()) { it.demonSeat },
            evilTeamSeatConfigurations = evilConfigurations,
            strategicWorldKeys = keys,
            forcedGoodSeats = forcedGood,
            forcedEvilSeats = forcedEvil,
            evilCoverSeats = evilConfigurations.flatten().toSortedSet(),
        )
    }
}
