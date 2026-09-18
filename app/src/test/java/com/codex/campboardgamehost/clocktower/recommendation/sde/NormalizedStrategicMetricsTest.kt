package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NormalizedStrategicMetricsTest {
    @Test
    fun `projection derives exact baseline-relative strategic metrics`() {
        val before = structure(
            demonSeats = setOf(5, 7),
            strategicKeys = setOf(
                StrategicWorldKey(5, listOf(2)),
                StrategicWorldKey(5, listOf(3)),
                StrategicWorldKey(7, listOf(2)),
                StrategicWorldKey(7, listOf(3)),
            ),
            forcedGoodSeats = setOf(1),
            evilCoverSeats = setOf(2, 3, 5, 7),
        )
        val after = structure(
            demonSeats = setOf(5),
            strategicKeys = setOf(
                StrategicWorldKey(5, listOf(2)),
                StrategicWorldKey(5, listOf(3)),
            ),
            forcedGoodSeats = setOf(1, 4),
            evilCoverSeats = setOf(2, 3, 5),
        )

        val evaluation = NormalizedStrategicMetricsProjection.evaluate(
            diagnostics = diagnostics(beforeWorlds = 48, afterWorlds = 9, before = before, after = after),
            playerCount = 8,
        )

        assertTrue(evaluation is NormalizedStrategicMetricsEvaluation.Ready)
        val metrics = (evaluation as NormalizedStrategicMetricsEvaluation.Ready).metrics
        assertEquals(StrategicRetentionRatio(1, 2), metrics.demonCoverRetention)
        assertEquals(StrategicRetentionRatio(2, 4), metrics.evilTopologyRetention)
        assertEquals(StrategicRetentionRatio(3, 4), metrics.evilCoverRetention)
        assertEquals(StrategicRetentionRatio(2, 8), metrics.forcedGoodFraction)
    }

    @Test
    fun `raw role-world multiplicity cannot change normalized strategic metrics`() {
        val before = structure(
            demonSeats = setOf(6, 7),
            strategicKeys = setOf(
                StrategicWorldKey(6, listOf(2)),
                StrategicWorldKey(7, listOf(2)),
            ),
            forcedGoodSeats = setOf(1),
            evilCoverSeats = setOf(2, 6, 7),
        )
        val after = structure(
            demonSeats = setOf(6),
            strategicKeys = setOf(StrategicWorldKey(6, listOf(2))),
            forcedGoodSeats = setOf(1, 3),
            evilCoverSeats = setOf(2, 6),
        )

        val small = NormalizedStrategicMetricsProjection.evaluate(
            diagnostics = diagnostics(12, 4, before, after),
            playerCount = 7,
        )
        val multiplicityInflated = NormalizedStrategicMetricsProjection.evaluate(
            diagnostics = diagnostics(1_200_000, 400_000, before, after),
            playerCount = 7,
        )

        assertEquals(small, multiplicityInflated)
    }

    @Test
    fun `unsatisfiable candidate is not represented as zero forced-good information`() {
        val before = structure(
            demonSeats = setOf(5, 6),
            strategicKeys = setOf(
                StrategicWorldKey(5, listOf(2)),
                StrategicWorldKey(6, listOf(2)),
            ),
            forcedGoodSeats = setOf(1),
            evilCoverSeats = setOf(2, 5, 6),
        )

        val evaluation = NormalizedStrategicMetricsProjection.evaluate(
            diagnostics = diagnostics(
                beforeWorlds = 10,
                afterWorlds = 0,
                before = before,
                after = ExactWorldStructureDiagnostics.EMPTY,
            ),
            playerCount = 6,
        )

        assertEquals(NormalizedStrategicMetricsEvaluation.Unsatisfiable, evaluation)
    }

    @Test
    fun `zero baseline structural denominator fails closed`() {
        val before = structure(
            demonSeats = emptySet(),
            strategicKeys = setOf(StrategicWorldKey(5, listOf(2))),
            forcedGoodSeats = setOf(1),
            evilCoverSeats = setOf(2, 5),
        )
        val after = before

        val evaluation = NormalizedStrategicMetricsProjection.evaluate(
            diagnostics = diagnostics(10, 5, before, after),
            playerCount = 5,
        )

        assertEquals(
            NormalizedStrategicMetricsEvaluation.UndefinedBaseline(
                setOf(NormalizedStrategicMetricBaselineDimension.DEMON_COVER),
            ),
            evaluation,
        )
    }

    @Test
    fun `empty exact baseline fails closed before structural ratios are interpreted`() {
        val evaluation = NormalizedStrategicMetricsProjection.evaluate(
            diagnostics = diagnostics(
                beforeWorlds = 0,
                afterWorlds = 0,
                before = ExactWorldStructureDiagnostics.EMPTY,
                after = ExactWorldStructureDiagnostics.EMPTY,
            ),
            playerCount = 5,
        )

        assertEquals(
            NormalizedStrategicMetricsEvaluation.UndefinedBaseline(
                setOf(NormalizedStrategicMetricBaselineDimension.EXACT_WORLDS),
            ),
            evaluation,
        )
    }

    private fun diagnostics(
        beforeWorlds: Long,
        afterWorlds: Long,
        before: ExactWorldStructureDiagnostics,
        after: ExactWorldStructureDiagnostics,
    ) = ExactHypotheticalObservationBundleDiagnostics(
        bundleId = "d2d4-metrics",
        recipientSeat = 1,
        before = WorldCardinality.Exact(BigInteger.valueOf(beforeWorlds)),
        after = WorldCardinality.Exact(BigInteger.valueOf(afterWorlds)),
        beforeStructure = before,
        afterStructure = after,
    )

    private fun structure(
        demonSeats: Set<Int>,
        strategicKeys: Set<StrategicWorldKey>,
        forcedGoodSeats: Set<Int>,
        evilCoverSeats: Set<Int>,
    ) = ExactWorldStructureDiagnostics(
        possibleDemonSeats = demonSeats,
        evilTeamSeatConfigurations = strategicKeys.mapTo(linkedSetOf()) { key ->
            (key.minionSeats + key.demonSeat).toSet()
        },
        strategicWorldKeys = strategicKeys,
        forcedGoodSeats = forcedGoodSeats,
        forcedEvilSeats = emptySet(),
        evilCoverSeats = evilCoverSeats,
    )
}
