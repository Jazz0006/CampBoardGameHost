package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactStrategicTopologyBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NormalizedStrategicDiagnosticsTest {
    @Test
    fun `projects exact before after structure into baseline-relative strategic ratios`() {
        val before = structure(
            demons = setOf(1, 2, 3, 4),
            strategicKeys = setOf(
                key(1, 5, 6),
                key(1, 5, 7),
                key(2, 5, 6),
                key(2, 5, 7),
                key(3, 5, 6),
                key(4, 5, 6),
            ),
            forcedGood = setOf(8),
            evilCover = setOf(1, 2, 3, 4, 5, 6),
        )
        val after = structure(
            demons = setOf(1, 2),
            strategicKeys = setOf(
                key(1, 5, 6),
                key(1, 5, 7),
                key(2, 5, 6),
            ),
            forcedGood = setOf(3, 4, 8, 9),
            evilCover = setOf(1, 2, 5),
        )

        val metrics = NormalizedStrategicDiagnosticsProjector.project(
            diagnostics(before, after),
            playerCount = 10,
        )

        assertEquals(StrategicRatio.Defined(2, 4), metrics.demonCoverRetention)
        assertEquals(StrategicRatio.Defined(3, 6), metrics.evilTopologyRetention)
        assertEquals(StrategicRatio.Defined(3, 6), metrics.evilCoverRetention)
        assertEquals(StrategicRatio.Defined(4, 10), metrics.forcedGoodFraction)
        assertEquals(0.5, metrics.demonCoverRetention.valueOrNull()!!, 0.0)
        assertEquals(0.5, metrics.evilTopologyRetention.valueOrNull()!!, 0.0)
        assertEquals(0.5, metrics.evilCoverRetention.valueOrNull()!!, 0.0)
        assertEquals(0.4, metrics.forcedGoodFraction.valueOrNull()!!, 0.0)
    }

    @Test
    fun `topology first diagnostics project through the same normalized strategic seam`() {
        val before = structure(
            demons = setOf(1, 2, 3, 4),
            strategicKeys = setOf(
                key(1, 5, 6),
                key(1, 5, 7),
                key(2, 5, 6),
                key(2, 5, 7),
                key(3, 5, 6),
                key(4, 5, 6),
            ),
            forcedGood = setOf(8),
            evilCover = setOf(1, 2, 3, 4, 5, 6),
        )
        val after = structure(
            demons = setOf(1, 2),
            strategicKeys = setOf(
                key(1, 5, 6),
                key(1, 5, 7),
                key(2, 5, 6),
            ),
            forcedGood = setOf(3, 4, 8, 9),
            evilCover = setOf(1, 2, 5),
        )

        val metrics = NormalizedStrategicDiagnosticsProjector.project(
            ExactStrategicTopologyBundleDiagnostics(
                bundleId = "topology-normalized",
                recipientSeat = 1,
                beforeStructure = before,
                afterStructure = after,
            ),
            playerCount = 10,
        )

        assertEquals(StrategicRatio.Defined(2, 4), metrics.demonCoverRetention)
        assertEquals(StrategicRatio.Defined(3, 6), metrics.evilTopologyRetention)
        assertEquals(StrategicRatio.Defined(3, 6), metrics.evilCoverRetention)
        assertEquals(StrategicRatio.Defined(4, 10), metrics.forcedGoodFraction)
    }

    @Test
    fun `empty exact baseline produces explicit undefined retention rather than NaN or infinity`() {
        val metrics = NormalizedStrategicDiagnosticsProjector.project(
            diagnostics(
                before = ExactWorldStructureDiagnostics.EMPTY,
                after = ExactWorldStructureDiagnostics.EMPTY,
            ),
            playerCount = 5,
        )

        assertEquals(StrategicRatio.Undefined, metrics.demonCoverRetention)
        assertEquals(StrategicRatio.Undefined, metrics.evilTopologyRetention)
        assertEquals(StrategicRatio.Undefined, metrics.evilCoverRetention)
        assertNull(metrics.demonCoverRetention.valueOrNull())
        assertNull(metrics.evilTopologyRetention.valueOrNull())
        assertNull(metrics.evilCoverRetention.valueOrNull())
        assertEquals(StrategicRatio.Defined(0, 5), metrics.forcedGoodFraction)
        assertEquals(0.0, metrics.forcedGoodFraction.valueOrNull()!!, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `projector rejects after topology that is not a subset of the exact baseline`() {
        val before = structure(
            demons = setOf(1),
            strategicKeys = setOf(key(1, 2)),
            evilCover = setOf(1, 2),
        )
        val after = structure(
            demons = setOf(1, 3),
            strategicKeys = setOf(key(1, 2), key(3, 2)),
            evilCover = setOf(1, 2, 3),
        )

        NormalizedStrategicDiagnosticsProjector.project(
            diagnostics(before, after),
            playerCount = 5,
        )
    }

    private fun diagnostics(
        before: ExactWorldStructureDiagnostics,
        after: ExactWorldStructureDiagnostics,
    ) = ExactHypotheticalObservationBundleDiagnostics(
        bundleId = "normalized",
        recipientSeat = 1,
        before = WorldCardinality.Exact(BigInteger.valueOf(10)),
        after = WorldCardinality.Exact(BigInteger.valueOf(5)),
        beforeStructure = before,
        afterStructure = after,
    )

    private fun structure(
        demons: Set<Int> = emptySet(),
        strategicKeys: Set<StrategicWorldKey> = emptySet(),
        forcedGood: Set<Int> = emptySet(),
        evilCover: Set<Int> = emptySet(),
    ) = ExactWorldStructureDiagnostics(
        possibleDemonSeats = demons,
        evilTeamSeatConfigurations = strategicKeys.mapTo(linkedSetOf()) { key ->
            setOf(key.demonSeat) + key.minionSeats
        },
        strategicWorldKeys = strategicKeys,
        forcedGoodSeats = forcedGood,
        forcedEvilSeats = emptySet(),
        evilCoverSeats = evilCover,
    )

    private fun key(demon: Int, vararg minions: Int) =
        StrategicWorldKey(demonSeat = demon, minionSeats = minions.sorted())
}
