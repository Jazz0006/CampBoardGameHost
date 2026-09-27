package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
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
        private val drunkContrast by lazy {
            Sde2D5DrunkRealCalibrationBuilder.buildNumericContrast()
        }
        private val bluffCalibration by lazy {
            Sde2D5DemonBluffRealCalibrationBuilder.build()
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
    fun `real Drunk numeric contrast keeps truthful and false candidates on one review surface`() {
        val contrast = drunkContrast

        assertEquals(SemanticTruth.TRUE, contrast.truthful.semanticTruth)
        assertEquals(SemanticTruth.FALSE, contrast.mildFalse.semanticTruth)
        assertEquals(
            contrast.truthful.marginalNormalized,
            contrast.counterfactualHealthyTruthDanger,
        )
        contrast.candidates.forEach { candidate ->
            val evidence = candidate.evidence
            assertEquals(
                Sde2D5EvidenceKind.DRUNK_HEALTHY_CORE,
                evidence.healthyCore.evidenceKind,
            )
            assertEquals(
                Sde2D5EvidenceKind.DRUNK_FULL_BUNDLE,
                evidence.fullBundle.evidenceKind,
            )
            assertTrue(evidence.fullBundle.rawMechanicalAfter!!.signum() >= 0)
            assertTrue(evidence.rawWorldsRemoved.signum() >= 0)
            assertTrue(
                evidence.fullBundle.afterStrategicWorldCount <=
                    evidence.healthyCore.afterStrategicWorldCount,
            )
        }
    }

    @Test
    fun `real Demon bluff joint output yields low and high shared support review contrasts`() {
        val calibration = bluffCalibration

        assertTrue(calibration.evidence.size >= 2)
        assertEquals(
            Sde2D5CalibrationRoleDomainCompleteness.FULL_SCRIPT_DOMAIN,
            calibration.roleDomainCompleteness,
        )
        assertEquals(
            setOf(
                Sde2D5DemonBluffSelectionReason.LOWEST_SHARED_TO_UNION_REFERENCE,
                Sde2D5DemonBluffSelectionReason.HIGHEST_SHARED_TO_UNION_REFERENCE,
                Sde2D5DemonBluffSelectionReason.EXTERNAL_HUMAN_OBSERVED,
            ),
            calibration.selected.flatMapTo(linkedSetOf()) { it.selectionReasons },
        )
        assertTrue(
            "D5D full-domain real fixture must expose more than one shared-support level.",
            calibration.evidence
                .map { evidence -> evidence.sharedToUnionRetention }
                .distinct()
                .size > 1,
        )
        val observed = calibration.selected.single {
            Sde2D5DemonBluffSelectionReason.EXTERNAL_HUMAN_OBSERVED in it.selectionReasons
        }.evidence
        assertTrue(observed.externalHumanObservedCaseIds.isNotEmpty())
        assertTrue(observed.individualSupportFloorStrategicWorldCount >= 0)
        assertTrue(observed.narrativeRouteClassCount >= 2)
    }

    @Test
    fun `full-domain Demon bluff calibration preserves legal Butler counterworld support`() {
        val calibration = bluffCalibration
        val butler = RoleId("Butler")
        val withButler = calibration.evidence.first { evidence -> butler in evidence.roles }

        assertTrue(
            "Full Trouble Brewing calibration must retain legal BARON-profile worlds for a Butler claim.",
            withButler.roleStrategicWorldCounts.getValue(butler) > 0,
        )
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
