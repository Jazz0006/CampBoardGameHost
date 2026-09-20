package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class Sde2D5FHumanLabelManifestTest {
    @Test
    fun `unreviewed template covers every reviewable ID and excludes references`() {
        val material = reviewMaterial()
        val manifest = Sde2D5FHumanLabelManifestBuilder.unreviewedTemplate(
            material = material,
            version = "d5f-b-template-v1",
        )
        val reviewableIds = material.records
            .filter { it.reviewability == Sde2D5FReviewability.REVIEWABLE }
            .mapTo(linkedSetOf(), Sde2D5FReviewRecord::reviewId)

        assertEquals(reviewableIds, manifest.entries.mapTo(linkedSetOf()) { it.reviewId })
        assertTrue(manifest.entries.all { it.label == FirstNightBeginnerCorpusLabel.UNREVIEWED })
        assertTrue(manifest.entries.all { it.reasons.isEmpty() })
        assertFalse(manifest.entries.any { it.reviewId == "d5f:baseline:baseline" })

        val validation = Sde2D5FHumanLabelManifestValidator.validate(material, manifest)
        assertTrue(validation.isValid)
        assertEquals(reviewableIds, validation.unreviewedRequiredReviewIds)
        assertFalse(validation.isCompleteForGateDerivation)

        assertEquals(
            manifest,
            Sde2D5FHumanLabelManifestCodec.parse(
                Sde2D5FHumanLabelManifestCodec.render(manifest),
            ),
        )
    }

    @Test
    fun `manifest round trips settled human labels and keeps uncertain explicit`() {
        val material = reviewMaterial()
        val manifest = Sde2D5FHumanLabelManifest(
            version = "d5f-b-test-v1",
            entries = listOf(
                Sde2D5FHumanLabelEntry(
                    reviewId = "d5f:role-info:role-a",
                    label = FirstNightBeginnerCorpusLabel.ACCEPTABLE,
                    reasons = setOf(
                        Sde2D5FReviewReason.MECHANICALLY_USEFUL_TOPOLOGY_NEUTRAL,
                    ),
                    note = "Useful role information without strategic collapse.",
                ),
                Sde2D5FHumanLabelEntry(
                    reviewId = "d5f:role-info:role-b",
                    label = FirstNightBeginnerCorpusLabel.UNCERTAIN,
                    reasons = setOf(Sde2D5FReviewReason.OTHER_EXPLICIT_REVIEW_REASON),
                    note = "Needs more human table review.",
                ),
            ),
        )

        val rendered = Sde2D5FHumanLabelManifestCodec.render(manifest)
        val parsed = Sde2D5FHumanLabelManifestCodec.parse(rendered)
        val validation = Sde2D5FHumanLabelManifestValidator.validate(
            material = material,
            manifest = parsed,
        )

        assertEquals(manifest, parsed)
        assertTrue(validation.unknownReviewIds.isEmpty())
        assertTrue(validation.referenceReviewIds.isEmpty())
        assertTrue(validation.duplicateReviewIds.isEmpty())
        assertTrue(validation.entriesMissingReasons.isEmpty())
        assertTrue(validation.unreviewedRequiredReviewIds.isEmpty())
        assertTrue(validation.isCompleteForGateDerivation)
        assertEquals(
            FirstNightBeginnerCorpusLabel.UNCERTAIN,
            parsed.entries.single { it.reviewId.endsWith("role-b") }.label,
        )
    }

    @Test
    fun `validation rejects unknown duplicate reference and reasonless settled entries`() {
        val material = reviewMaterial()
        val manifest = Sde2D5FHumanLabelManifest(
            version = "d5f-b-invalid",
            entries = listOf(
                Sde2D5FHumanLabelEntry(
                    reviewId = "d5f:baseline:baseline",
                    label = FirstNightBeginnerCorpusLabel.ACCEPTABLE,
                    reasons = setOf(Sde2D5FReviewReason.CROSS_REGIME_REFERENCE),
                ),
                Sde2D5FHumanLabelEntry(
                    reviewId = "d5f:role-info:role-a",
                    label = FirstNightBeginnerCorpusLabel.BAD_TOO_STRONG,
                    reasons = emptySet(),
                ),
                Sde2D5FHumanLabelEntry(
                    reviewId = "d5f:role-info:role-a",
                    label = FirstNightBeginnerCorpusLabel.ACCEPTABLE,
                    reasons = setOf(Sde2D5FReviewReason.OTHER_EXPLICIT_REVIEW_REASON),
                ),
                Sde2D5FHumanLabelEntry(
                    reviewId = "d5f:unknown",
                    label = FirstNightBeginnerCorpusLabel.BAD_TOO_WEAK,
                    reasons = setOf(Sde2D5FReviewReason.INSUFFICIENT_HEALTHY_INFORMATION),
                ),
            ),
        )

        val validation = Sde2D5FHumanLabelManifestValidator.validate(
            material = material,
            manifest = manifest,
        )

        assertEquals(setOf("d5f:unknown"), validation.unknownReviewIds)
        assertEquals(setOf("d5f:baseline:baseline"), validation.referenceReviewIds)
        assertEquals(setOf("d5f:role-info:role-a"), validation.duplicateReviewIds)
        assertEquals(setOf("d5f:role-info:role-a"), validation.entriesMissingReasons)
        assertEquals(setOf("d5f:role-info:role-b"), validation.unreviewedRequiredReviewIds)
        assertFalse(validation.isValid)
        assertFalse(validation.isCompleteForGateDerivation)
    }

    @Test
    fun `unreviewed entries stay incomplete and cannot carry inferred reasons`() {
        val material = reviewMaterial()
        val manifest = Sde2D5FHumanLabelManifest(
            version = "d5f-b-unreviewed",
            entries = listOf(
                Sde2D5FHumanLabelEntry(
                    reviewId = "d5f:role-info:role-a",
                    label = FirstNightBeginnerCorpusLabel.UNREVIEWED,
                    reasons = emptySet(),
                ),
                Sde2D5FHumanLabelEntry(
                    reviewId = "d5f:role-info:role-b",
                    label = FirstNightBeginnerCorpusLabel.ACCEPTABLE,
                    reasons = setOf(
                        Sde2D5FReviewReason.MECHANICALLY_USEFUL_TOPOLOGY_NEUTRAL,
                    ),
                ),
            ),
        )

        val validation = Sde2D5FHumanLabelManifestValidator.validate(material, manifest)

        assertTrue(validation.isValid)
        assertEquals(setOf("d5f:role-info:role-a"), validation.unreviewedRequiredReviewIds)
        assertFalse(validation.isCompleteForGateDerivation)

        assertThrows(IllegalArgumentException::class.java) {
            Sde2D5FHumanLabelEntry(
                reviewId = "d5f:role-info:role-a",
                label = FirstNightBeginnerCorpusLabel.UNREVIEWED,
                reasons = setOf(Sde2D5FReviewReason.STRATEGIC_COLLAPSE),
            )
        }
    }

    @Test
    fun `codec rejects malformed labels reasons and duplicate manifest lines`() {
        assertThrows(IllegalArgumentException::class.java) {
            Sde2D5FHumanLabelManifestCodec.parse(
                """
                version=d5f-b-test
                d5f:role-info:role-a\tNOT_A_LABEL\tSTRATEGIC_COLLAPSE\tnote
                """.trimIndent(),
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            Sde2D5FHumanLabelManifestCodec.parse(
                """
                version=d5f-b-test
                d5f:role-info:role-a\tACCEPTABLE\tNOT_A_REASON\tnote
                """.trimIndent(),
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            Sde2D5FHumanLabelManifestCodec.parse(
                """
                version=d5f-b-test
                d5f:role-info:role-a\tACCEPTABLE\tOTHER_EXPLICIT_REVIEW_REASON\tone
                d5f:role-info:role-a\tUNCERTAIN\tOTHER_EXPLICIT_REVIEW_REASON\ttwo
                """.trimIndent(),
            )
        }
    }

    private fun reviewMaterial(): Sde2D5FCalibrationReviewMaterial {
        val baseline = point(
            pointId = "baseline",
            evidenceKind = Sde2D5EvidenceKind.BASELINE,
            contrastId = null,
        )
        val first = roleEvidence("role-a")
        val second = roleEvidence("role-b")
        return Sde2D5FCalibrationReviewBuilder.build(
            sealedHoldoutScenarioCount = 1,
            baselineReferences = listOf(baseline),
            drunkContrasts = emptyList(),
            bluffSelections = emptyList(),
            roleInformationEvidence = listOf(first, second),
        )
    }

    private fun roleEvidence(id: String) =
        Sde2D5RoleInformationCalibrationEvidence(
            point = point(
                pointId = id,
                evidenceKind = Sde2D5EvidenceKind.STRATEGIC_ROLE_INFORMATION_CONTRAST,
                contrastId = "role-contrast",
            ),
            sourceSeat = 2,
            sourceRole = RoleId("Chef"),
            control = FirstNightBundleEntryControl.RULE_DETERMINED,
            rawWorldsRemoved = BigInteger.TEN,
            hasMechanicalInformationGain = true,
            topologyNeutral = true,
        )

    private fun point(
        pointId: String,
        evidenceKind: Sde2D5EvidenceKind,
        contrastId: String?,
    ) = Sde2D5CalibrationEvidencePoint(
        pointId = pointId,
        playerCount = 7,
        regime = Sde2D5PlayerCountRegime.SEVEN_TO_NINE,
        profileKind = Sde2D5SetupProfileKind.STANDARD,
        evidenceKind = evidenceKind,
        contrastId = contrastId,
        beforeStrategicWorldCount = 20,
        afterStrategicWorldCount = 20,
        normalized = NormalizedStrategicDiagnostics(
            demonCoverRetention = StrategicRatio.Defined(5, 5),
            evilTopologyRetention = StrategicRatio.Defined(20, 20),
            evilCoverRetention = StrategicRatio.Defined(7, 7),
            forcedGoodFraction = StrategicRatio.Defined(1, 7),
        ),
        rawMechanicalBefore = BigInteger.valueOf(100),
        rawMechanicalAfter = BigInteger.valueOf(90),
    )
}
