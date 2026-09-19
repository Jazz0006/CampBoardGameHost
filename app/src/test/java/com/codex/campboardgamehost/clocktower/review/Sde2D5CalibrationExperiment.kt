package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        private val drunkEvidence by lazy {
            Sde2D5DrunkRealCalibrationBuilder.buildFalseNumericEvidence()
        }
        private val bluffCalibration by lazy {
            Sde2D5DemonBluffRealCalibrationBuilder.build()
        }
        private val roleInformationCalibration by lazy {
            Sde2D5RoleInformationRealCalibrationBuilder.build()
        }
        private val reviewMaterial by lazy {
            Sde2D5FRealCalibrationReviewBuilder.build(
                baseline = baselineEvidence,
                drunk = drunkEvidence,
                bluff = bluffCalibration,
                roleInformation = roleInformationCalibration,
            )
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
    fun `real Drunk numeric candidate projects HealthyCore FullBundle and marginal evidence`() {
        val evidence = drunkEvidence

        assertEquals(SemanticTruth.FALSE, evidence.semanticTruth)
        assertEquals(Sde2D5EvidenceKind.DRUNK_HEALTHY_CORE, evidence.healthyCore.evidenceKind)
        assertEquals(Sde2D5EvidenceKind.DRUNK_FULL_BUNDLE, evidence.fullBundle.evidenceKind)
        assertTrue(evidence.fullBundle.rawMechanicalAfter!!.signum() > 0)
        assertTrue(
            evidence.fullBundle.afterStrategicWorldCount <=
                evidence.healthyCore.afterStrategicWorldCount,
        )
    }

    @Test
    fun `real Demon bluff joint output yields low and high shared support review contrasts`() {
        val calibration = bluffCalibration

        assertTrue(calibration.evidence.size >= 2)
        assertEquals(
            setOf(
                Sde2D5DemonBluffSelectionReason.LOWEST_SHARED_TO_UNION,
                Sde2D5DemonBluffSelectionReason.HIGHEST_SHARED_TO_UNION,
            ),
            calibration.selected.flatMapTo(linkedSetOf()) { it.selectionReasons },
        )
        assertTrue(
            "D5D bounded real fixture must expose more than one shared-support level.",
            calibration.evidence
                .map { evidence -> evidence.sharedToUnionRetention }
                .distinct()
                .size > 1,
        )
    }

    @Test
    fun `real healthy bundle marginals expose topology neutral and near raw strategic contrasts`() {
        val calibration = roleInformationCalibration

        assertTrue(calibration.topologyNeutral.hasMechanicalInformationGain)
        assertTrue(calibration.topologyNeutral.topologyNeutral)
        assertTrue(calibration.topologyNeutral.rawWorldsRemoved.signum() > 0)

        val nearRaw = calibration.closestRawDifferentTopology
        assertTrue(nearRaw.first.hasMechanicalInformationGain)
        assertTrue(nearRaw.second.hasMechanicalInformationGain)
        assertFalse(
            sameStrategicRatioValue(
                nearRaw.first.point.normalized.evilTopologyRetention,
                nearRaw.second.point.normalized.evilTopologyRetention,
            ),
        )
        assertTrue(nearRaw.rawWorldRemovalDifference.signum() >= 0)
    }

    @Test
    fun `real D5F calibration review export stays calibration only and deterministic`() {
        val material = reviewMaterial

        assertEquals(
            FirstNightBundleBeginnerCorpusBuilder.SEALED_HOLDOUT_SCENARIO_COUNT,
            material.sealedHoldoutScenarioCount,
        )
        assertEquals(
            Sde2D5PlayerCountRegime.entries.toSet(),
            material.records
                .filter { it.evidenceKind == Sde2D5FReviewEvidenceKind.BASELINE_REFERENCE }
                .mapTo(linkedSetOf()) { it.regime },
        )
        Sde2D5PlayerCountRegime.entries.forEach { regime ->
            assertEquals(
                Sde2D5SetupProfileKind.entries.toSet(),
                material.records
                    .filter {
                        it.evidenceKind == Sde2D5FReviewEvidenceKind.BASELINE_REFERENCE &&
                            it.regime == regime
                    }
                    .mapTo(linkedSetOf()) { it.profileKind },
            )
        }
        material.records
            .filter { it.reviewability == Sde2D5FReviewability.REVIEWABLE }
            .forEach { record ->
                assertEquals(FirstNightBeginnerCorpusLabel.UNREVIEWED, record.initialLabel)
            }

        val drunkRecord = material.records.single {
            it.evidenceKind == Sde2D5FReviewEvidenceKind.DRUNK_CONTRAST
        }
        val drunkDetails = drunkRecord.details as Sde2D5FReviewDetails.DrunkContrast
        assertEquals(SemanticTruth.FALSE, drunkDetails.evidence.semanticTruth)

        val bluffReasons = material.records
            .filter { it.evidenceKind == Sde2D5FReviewEvidenceKind.DEMON_BLUFF_SUPPORT }
            .flatMapTo(linkedSetOf()) { record ->
                (record.details as Sde2D5FReviewDetails.DemonBluffSupport)
                    .selection.selectionReasons
            }
        assertEquals(Sde2D5DemonBluffSelectionReason.entries.toSet(), bluffReasons)

        val expectedRolePointIds = setOf(
            roleInformationCalibration.topologyNeutral.point.pointId,
            roleInformationCalibration.closestRawDifferentTopology.first.point.pointId,
            roleInformationCalibration.closestRawDifferentTopology.second.point.pointId,
            roleInformationCalibration.strongestStrategicCollapse.point.pointId,
            roleInformationCalibration.weakestMechanicalInformation.point.pointId,
        )
        val actualRolePointIds = material.records
            .filter { it.evidenceKind == Sde2D5FReviewEvidenceKind.ROLE_INFORMATION_CONTRAST }
            .mapTo(linkedSetOf()) { record ->
                (record.details as Sde2D5FReviewDetails.RoleInformationContrast)
                    .evidence.point.pointId
            }
        assertEquals(expectedRolePointIds, actualRolePointIds)

        val report = Sde2D5FCalibrationReviewRenderer.renderMarkdown(material)
        assertTrue(report.contains("Sealed holdout scenarios: ${material.sealedHoldoutScenarioCount}"))
        assertTrue(report.contains("Holdout diagnostics are sealed until gate freeze."))
        assertTrue(report.contains("initialLabel=UNREVIEWED"))
        val reportFile = File("build/reports/sde-2d5f-calibration-review.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(report, Charsets.UTF_8)
    }

    @Test
    fun `real D5F human label template is valid but incomplete until human review`() {
        val manifest = Sde2D5FHumanLabelManifestBuilder.unreviewedTemplate(
            material = reviewMaterial,
            version = "d5f-b-calibration-v1",
        )
        val validation = Sde2D5FHumanLabelManifestValidator.validate(
            material = reviewMaterial,
            manifest = manifest,
        )
        val requiredReviewIds = reviewMaterial.records
            .filter { it.reviewability == Sde2D5FReviewability.REVIEWABLE }
            .mapTo(linkedSetOf(), Sde2D5FReviewRecord::reviewId)

        assertTrue(validation.isValid)
        assertFalse(validation.isCompleteForGateDerivation)
        assertEquals(requiredReviewIds, validation.unreviewedRequiredReviewIds)
        assertEquals(requiredReviewIds, manifest.entries.mapTo(linkedSetOf()) { it.reviewId })
        assertTrue(manifest.entries.all { it.label == FirstNightBeginnerCorpusLabel.UNREVIEWED })
        assertTrue(manifest.entries.all { it.reasons.isEmpty() })

        val rendered = Sde2D5FHumanLabelManifestCodec.render(manifest)
        assertEquals(manifest, Sde2D5FHumanLabelManifestCodec.parse(rendered))

        val manifestFile = File("build/reports/sde-2d5f-human-label-manifest.tsv")
        requireNotNull(manifestFile.parentFile).mkdirs()
        manifestFile.writeText(rendered, Charsets.UTF_8)
    }

}
