package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Explicit SDE-2D5 T3 calibration evidence workload.
 *
 * Cross-regime topology evaluation and real whole-bundle calibration cases are intentionally
 * outside ordinary FAST/FULL regression. They generate deterministic review evidence and carry no
 * production policy thresholds.
 */
class Sde2D5CalibrationExperiment {
    companion object {
        private val baselineEvidence by lazy {
            Sde2D5CrossRegimeCalibrationEvidenceBuilder.buildBaseline()
        }
    }

    @Test
    fun `baseline evidence spans all four player count regimes and both setup profile families`() {
        val evidence = baselineEvidence

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
    fun `ClockTracker fourteen player human choices remain expressible and topology feasible`() {
        val pilot = Sde2D5ExternalHumanPilotBuilder.build()

        assertTrue(pilot.actualBluffLegal)
        assertTrue(pilot.actualRedHerringLegal)
        assertTrue(pilot.actualDrunkCandidateLegal)
        assertEquals(SemanticTruth.FALSE, pilot.actualDrunkSemanticTruth)
        assertTrue(pilot.actualFullBundleFeasibleForEveryRecipient)

        val reportFile = File("build/reports/sde-2d5-external-human-pilot.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(pilot.report, Charsets.UTF_8)
    }

}
