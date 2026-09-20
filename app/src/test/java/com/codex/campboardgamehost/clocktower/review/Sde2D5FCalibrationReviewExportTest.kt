package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sde2D5FCalibrationReviewExportTest {
    @Test
    fun `review export keeps references non labelable and every reviewable item unreviewed`() {
        val baseline = point(
            pointId = "baseline-standard-6",
            evidenceKind = Sde2D5EvidenceKind.BASELINE,
            contrastId = null,
            topology = StrategicRatio.Defined(12, 20),
        )
        val drunk = Sde2D5DrunkCalibrationEvidence(
            candidateId = "drunk-false-1",
            semanticTruth = SemanticTruth.FALSE,
            healthyCore = point(
                pointId = "drunk-false-1:healthy",
                evidenceKind = Sde2D5EvidenceKind.DRUNK_HEALTHY_CORE,
                contrastId = "drunk-false-1",
                topology = StrategicRatio.Defined(10, 20),
            ),
            fullBundle = point(
                pointId = "drunk-false-1:full",
                evidenceKind = Sde2D5EvidenceKind.DRUNK_FULL_BUNDLE,
                contrastId = "drunk-false-1",
                topology = StrategicRatio.Defined(8, 20),
            ),
            marginalNormalized = normalized(
                topology = StrategicRatio.Defined(8, 10),
            ),
            rawWorldsRemoved = BigInteger.valueOf(12),
        )
        val roles = listOf(RoleId("Monk"), RoleId("Soldier"), RoleId("Butler"))
        val bluff = Sde2D5DemonBluffCalibrationSelection(
            evidence = Sde2D5DemonBluffCalibrationEvidence(
                candidateId = "bluff-low-shared",
                recipientSeat = 1,
                playerCount = 7,
                regime = Sde2D5PlayerCountRegime.SEVEN_TO_NINE,
                profileKind = Sde2D5SetupProfileKind.STANDARD,
                evidenceKind = Sde2D5EvidenceKind.DEMON_BLUFF_SUPPORT,
                roles = roles,
                supportedRoles = roles.toSet(),
                supportedRoleCount = 3,
                roleSupportNormalized = roles.associateWith {
                    normalized(StrategicRatio.Defined(3, 4))
                },
                roleStrategicWorldCounts = roles.associateWith { 3 },
                unionStrategicWorldCount = 3,
                sharedStrategicWorldCount = 1,
                sharedToUnionRetention = StrategicRatio.Defined(1, 3),
                distinctRoleStrategicPatternCount = 3,
                individualSupportFloorStrategicWorldCount = 3,
                roleTraits = roles.associateWith(Sde2D5DemonBluffRoleTraits::forRole),
                claimCadenceClassCount =
                    roles.map { Sde2D5DemonBluffRoleTraits.forRole(it).claimCadence }.distinct().size,
                narrativeRouteClassCount =
                    roles.map { Sde2D5DemonBluffRoleTraits.forRole(it).narrativeRouteClass }.distinct().size,
                pairwiseStrategicCoverage = listOf(
                    Sde2D5BluffPairwiseCoverage(
                        firstRole = roles[0],
                        secondRole = roles[1],
                        unionStrategicWorldCount = 3,
                        sharedStrategicWorldCount = 1,
                        sharedToUnionRetention = StrategicRatio.Defined(1, 3),
                    ),
                    Sde2D5BluffPairwiseCoverage(
                        firstRole = roles[0],
                        secondRole = roles[2],
                        unionStrategicWorldCount = 3,
                        sharedStrategicWorldCount = 1,
                        sharedToUnionRetention = StrategicRatio.Defined(1, 3),
                    ),
                    Sde2D5BluffPairwiseCoverage(
                        firstRole = roles[1],
                        secondRole = roles[2],
                        unionStrategicWorldCount = 3,
                        sharedStrategicWorldCount = 1,
                        sharedToUnionRetention = StrategicRatio.Defined(1, 3),
                    ),
                ),
                externalHumanObservedCaseIds = emptySet(),
            ),
            selectionReasons = setOf(
                Sde2D5DemonBluffSelectionReason.LOWEST_SHARED_TO_UNION_REFERENCE,
            ),
        )
        val roleInformation = Sde2D5RoleInformationCalibrationEvidence(
            point = point(
                pointId = "role-info-neutral",
                evidenceKind = Sde2D5EvidenceKind.STRATEGIC_ROLE_INFORMATION_CONTRAST,
                contrastId = "role-info-contrast",
                topology = StrategicRatio.Defined(20, 20),
            ),
            sourceSeat = 2,
            sourceRole = RoleId("Chef"),
            control = FirstNightBundleEntryControl.RULE_DETERMINED,
            rawWorldsRemoved = BigInteger.valueOf(15),
            hasMechanicalInformationGain = true,
            topologyNeutral = true,
        )

        val material = Sde2D5FCalibrationReviewBuilder.build(
            sealedHoldoutScenarioCount = 1,
            baselineReferences = listOf(baseline),
            drunkContrasts = listOf(
                Sde2D5DrunkCalibrationContrastBuilder.build(
                    contrastId = "drunk-group",
                    evidence = listOf(
                        drunk.copy(
                            candidateId = "drunk-true",
                            semanticTruth = SemanticTruth.TRUE,
                        ),
                        drunk,
                    ),
                ),
            ),
            bluffSelections = listOf(bluff),
            roleInformationEvidence = listOf(roleInformation),
        )

        assertEquals(4, material.records.size)
        assertEquals(1, material.sealedHoldoutScenarioCount)
        assertEquals(
            material.records.map { it.reviewId }.sorted(),
            material.records.map { it.reviewId },
        )
        val baselineRecord = material.records.single {
            it.evidenceKind == Sde2D5FReviewEvidenceKind.BASELINE_REFERENCE
        }
        assertEquals(Sde2D5FReviewability.REFERENCE, baselineRecord.reviewability)
        assertNull(baselineRecord.initialLabel)
        assertEquals(
            Sde2D5FDecisionOwner.NONE_REFERENCE,
            baselineRecord.controlSurface.decisionOwner,
        )
        assertTrue(baselineRecord.controlSurface.controllableVariables.isEmpty())

        val drunkRecord = material.records.single {
            it.evidenceKind == Sde2D5FReviewEvidenceKind.DRUNK_CONTRAST
        }
        assertEquals(
            setOf(Sde2D5FPolicyVariable.IMPAIRED_CLUE_OUTPUT),
            drunkRecord.controlSurface.controllableVariables,
        )
        assertTrue(
            Sde2D5FPolicyVariable.SEMANTIC_TRUTH_RELATION in
                drunkRecord.controlSurface.diagnosticOnlyVariables,
        )
        assertFalse(
            Sde2D5FPolicyVariable.SEMANTIC_TRUTH_RELATION in
                drunkRecord.controlSurface.controllableVariables,
        )

        val roleInformationRecord = material.records.single {
            it.evidenceKind == Sde2D5FReviewEvidenceKind.ROLE_INFORMATION_CONTRAST
        }
        assertEquals(
            Sde2D5FDecisionOwner.CALIBRATION_DIAGNOSTIC_ONLY,
            roleInformationRecord.controlSurface.decisionOwner,
        )
        assertTrue(roleInformationRecord.controlSurface.controllableVariables.isEmpty())
        assertEquals(
            Sde2D5FPersistenceBoundary.FIRST_NIGHT_CLUE_PLANNED_UNTIL_SHOWN_THEN_COMMITTED,
            roleInformationRecord.controlSurface.persistenceBoundary,
        )

        material.records
            .filter { it.reviewability == Sde2D5FReviewability.REVIEWABLE }
            .forEach { record ->
                assertEquals(FirstNightBeginnerCorpusLabel.UNREVIEWED, record.initialLabel)
            }
    }

    @Test
    fun `renderer is deterministic preserves exact ratios and exposes only sealed holdout count`() {
        val baseline = point(
            pointId = "baseline-standard-6",
            evidenceKind = Sde2D5EvidenceKind.BASELINE,
            contrastId = null,
            topology = StrategicRatio.Defined(12, 20),
        )
        val roleInformation = Sde2D5RoleInformationCalibrationEvidence(
            point = point(
                pointId = "role-info-neutral",
                evidenceKind = Sde2D5EvidenceKind.STRATEGIC_ROLE_INFORMATION_CONTRAST,
                contrastId = "role-info-contrast",
                topology = StrategicRatio.Defined(20, 20),
            ),
            sourceSeat = 2,
            sourceRole = RoleId("Chef"),
            control = FirstNightBundleEntryControl.RULE_DETERMINED,
            rawWorldsRemoved = BigInteger.valueOf(15),
            hasMechanicalInformationGain = true,
            topologyNeutral = true,
        )
        val material = Sde2D5FCalibrationReviewBuilder.build(
            sealedHoldoutScenarioCount = 1,
            baselineReferences = listOf(baseline),
            drunkContrasts = emptyList(),
            bluffSelections = emptyList(),
            roleInformationEvidence = listOf(roleInformation),
        )

        val first = Sde2D5FCalibrationReviewRenderer.renderMarkdown(material)
        val second = Sde2D5FCalibrationReviewRenderer.renderMarkdown(material)

        assertEquals(first, second)
        assertTrue(first.contains("Sealed holdout scenarios: 1"))
        assertTrue(first.contains("Holdout diagnostics are sealed until gate freeze."))
        assertTrue(first.contains("evilTopologyRetention=12/20"))
        assertTrue(first.contains("evilTopologyRetention=20/20"))
        assertTrue(first.contains("initialLabel=UNREVIEWED"))
        assertTrue(first.contains("reviewability=REFERENCE"))
        assertTrue(first.contains("lifecycleStage=CROSS_STAGE_REFERENCE"))
        assertTrue(first.contains("decisionOwner=CALIBRATION_DIAGNOSTIC_ONLY"))
        assertTrue(first.contains("persistenceBoundary=DESCRIPTIVE_BUNDLE_EVIDENCE_ONLY"))
        assertFalse(first.contains("holdout scenario id", ignoreCase = true))
        assertFalse(first.contains("holdout seating", ignoreCase = true))
    }

    @Test
    fun `review reason vocabulary covers corrected B3 policy dimensions`() {
        assertTrue(
            Sde2D5FReviewReason.entries.containsAll(
                setOf(
                    Sde2D5FReviewReason.EXCESSIVE_CONFIRMATION_CHAIN,
                    Sde2D5FReviewReason.INSUFFICIENT_HEALTHY_INFORMATION,
                    Sde2D5FReviewReason.IMPAIRED_CLUE_TOO_REVEALING,
                    Sde2D5FReviewReason.IMPAIRED_CLUE_COHERENT,
                    Sde2D5FReviewReason.BLUFF_EXECUTION_BURDEN,
                    Sde2D5FReviewReason.BLUFF_NARRATIVE_REDUNDANCY,
                    Sde2D5FReviewReason.BLUFF_COHERENCE_FRAGILE,
                    Sde2D5FReviewReason.BLUFF_ROUTES_USABLE,
                    Sde2D5FReviewReason.CROSS_CHANNEL_NARRATIVE_COHERENCE,
                    Sde2D5FReviewReason.OTHER_EXPLICIT_REVIEW_REASON,
                ),
            ),
        )
    }

    private fun point(
        pointId: String,
        evidenceKind: Sde2D5EvidenceKind,
        contrastId: String?,
        topology: StrategicRatio,
    ) = Sde2D5CalibrationEvidencePoint(
        pointId = pointId,
        playerCount = 7,
        regime = Sde2D5PlayerCountRegime.SEVEN_TO_NINE,
        profileKind = Sde2D5SetupProfileKind.STANDARD,
        evidenceKind = evidenceKind,
        contrastId = contrastId,
        beforeStrategicWorldCount = 20,
        afterStrategicWorldCount =
            (topology as StrategicRatio.Defined).numerator,
        normalized = normalized(topology),
        rawMechanicalBefore = BigInteger.valueOf(100),
        rawMechanicalAfter = BigInteger.valueOf(50),
    )

    private fun normalized(
        topology: StrategicRatio,
    ) = NormalizedStrategicDiagnostics(
        demonCoverRetention = StrategicRatio.Defined(4, 5),
        evilTopologyRetention = topology,
        evilCoverRetention = StrategicRatio.Defined(5, 7),
        forcedGoodFraction = StrategicRatio.Defined(2, 7),
    )
}
