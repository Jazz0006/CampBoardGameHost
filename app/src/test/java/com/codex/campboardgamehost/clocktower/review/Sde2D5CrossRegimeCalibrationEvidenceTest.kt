package com.codex.campboardgamehost.clocktower.review

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sde2D5CrossRegimeCalibrationEvidenceTest {
    @Test
    fun `baseline evidence spans all four player count regimes and both setup profile families`() {
        val evidence = Sde2D5CrossRegimeCalibrationEvidenceBuilder.buildBaseline()

        assertEquals(
            listOf(6, 6, 9, 9, 12, 12, 15, 15),
            evidence.points.map { it.playerCount },
        )
        assertEquals(
            Sde2D5PlayerCountRegime.entries.toSet(),
            evidence.points.mapTo(linkedSetOf()) { it.regime },
        )
        assertEquals(
            Sde2D5SetupProfileKind.entries.toSet(),
            evidence.points.mapTo(linkedSetOf()) { it.profileKind },
        )
        Sde2D5PlayerCountRegime.entries.forEach { regime ->
            assertEquals(
                Sde2D5SetupProfileKind.entries.toSet(),
                evidence.points
                    .filter { it.regime == regime }
                    .mapTo(linkedSetOf()) { it.profileKind },
            )
        }
        assertTrue(evidence.points.all { it.beforeStrategicWorldCount > 0 })
        assertTrue(evidence.points.all { it.afterStrategicWorldCount > 0 })
        assertTrue(evidence.points.all {
            it.afterStrategicWorldCount <= it.beforeStrategicWorldCount
        })
        assertTrue(evidence.points.all {
            it.normalized.demonCoverRetention.valueOrNull() != null &&
                it.normalized.evilTopologyRetention.valueOrNull() != null &&
                it.normalized.evilCoverRetention.valueOrNull() != null
        })
        evidence.points.forEach { point ->
            assertNull(point.rawMechanicalBefore)
            assertNull(point.rawMechanicalAfter)
        }
    }

    @Test
    fun `player count regimes are explicit at every supported boundary`() {
        val expected = mapOf(
            5 to Sde2D5PlayerCountRegime.FIVE_TO_SIX,
            6 to Sde2D5PlayerCountRegime.FIVE_TO_SIX,
            7 to Sde2D5PlayerCountRegime.SEVEN_TO_NINE,
            9 to Sde2D5PlayerCountRegime.SEVEN_TO_NINE,
            10 to Sde2D5PlayerCountRegime.TEN_TO_TWELVE,
            12 to Sde2D5PlayerCountRegime.TEN_TO_TWELVE,
            13 to Sde2D5PlayerCountRegime.THIRTEEN_TO_FIFTEEN,
            15 to Sde2D5PlayerCountRegime.THIRTEEN_TO_FIFTEEN,
        )

        expected.forEach { (playerCount, regime) ->
            assertEquals(regime, Sde2D5PlayerCountRegime.from(playerCount))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `player count regime rejects unsupported counts`() {
        Sde2D5PlayerCountRegime.from(16)
    }
}
