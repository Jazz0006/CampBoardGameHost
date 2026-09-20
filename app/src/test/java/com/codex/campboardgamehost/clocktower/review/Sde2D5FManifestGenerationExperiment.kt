package com.codex.campboardgamehost.clocktower.review

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Temporary focused B3 generator. It exists only to materialize the deterministic v2 manifest
 * without running the unrelated 14-player external-human pilot in the full D5 calibration class.
 */
class Sde2D5FManifestGenerationExperiment {
    @Test
    fun `generate corrected v2 review manifest`() {
        val material = Sde2D5FRealCalibrationReviewBuilder.build(
            baseline = Sde2D5CrossRegimeCalibrationEvidenceBuilder.buildBaseline(),
            drunk = Sde2D5DrunkRealCalibrationBuilder.buildNumericContrast(),
            bluff = Sde2D5DemonBluffRealCalibrationBuilder.build(),
            roleInformation = Sde2D5RoleInformationRealCalibrationBuilder.build(),
        )
        val manifest = Sde2D5FHumanLabelManifestBuilder.unreviewedTemplate(
            material = material,
            version = "d5f-b-calibration-v2",
        )
        val validation = Sde2D5FHumanLabelManifestValidator.validate(material, manifest)

        assertTrue(validation.isValid)
        assertFalse(validation.isCompleteForGateDerivation)
        assertEquals(
            material.records.count { it.reviewability == Sde2D5FReviewability.REVIEWABLE },
            manifest.entries.size,
        )
        assertTrue(manifest.entries.all { it.label == FirstNightBeginnerCorpusLabel.UNREVIEWED })
        assertTrue(manifest.entries.all { it.reasons.isEmpty() })
        assertEquals(11, manifest.entries.size)

        val roleInfoRecords = material.records.filter {
            it.evidenceKind == Sde2D5FReviewEvidenceKind.ROLE_INFORMATION_CONTRAST
        }
        assertTrue(
            roleInfoRecords.any {
                it.controlSurface.decisionOwner == Sde2D5FDecisionOwner.STORYTELLER_SDE
            },
        )
        assertTrue(
            roleInfoRecords.any {
                it.controlSurface.decisionOwner == Sde2D5FDecisionOwner.CALIBRATION_DIAGNOSTIC_ONLY
            },
        )

        val persisted = Sde2D5FHumanLabelManifestCodec.parse(
            File("src/test/resources/review/sde-2d5f-human-label-manifest.tsv")
                .readText(Charsets.UTF_8),
        )
        assertEquals(manifest, persisted)

        val reports = File("build/reports")
        reports.mkdirs()
        File(reports, "sde-2d5f-human-label-manifest-generated.tsv").writeText(
            Sde2D5FHumanLabelManifestCodec.render(manifest),
            Charsets.UTF_8,
        )
        File(reports, "sde-2d5f-calibration-review-generated.md").writeText(
            Sde2D5FCalibrationReviewRenderer.renderMarkdown(material),
            Charsets.UTF_8,
        )
    }
}
