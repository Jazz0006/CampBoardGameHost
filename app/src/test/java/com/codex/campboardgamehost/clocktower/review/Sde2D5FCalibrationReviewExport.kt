package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio

internal enum class Sde2D5FReviewEvidenceKind {
    BASELINE_REFERENCE,
    DRUNK_CONTRAST,
    DEMON_BLUFF_SUPPORT,
    ROLE_INFORMATION_CONTRAST,
    BUNDLE_CONFIRMATION_CHAIN,
}

internal enum class Sde2D5FReviewability {
    REFERENCE,
    REVIEWABLE,
}

internal enum class Sde2D5FLifecycleStage {
    CROSS_STAGE_REFERENCE,
    PRE_GAME_BEFORE_BLUFF_REVEAL,
    FIRST_NIGHT_AFTER_SETUP_PERSISTENCE,
}

internal enum class Sde2D5FDecisionOwner {
    NONE_REFERENCE,
    STORYTELLER_SDE,
    CALIBRATION_DIAGNOSTIC_ONLY,
}

internal enum class Sde2D5FPolicyVariable {
    NORMALIZED_STRATEGIC_DIAGNOSTICS,
    IMPAIRED_CLUE_OUTPUT,
    SEMANTIC_TRUTH_RELATION,
    COUNTERFACTUAL_HEALTHY_TRUTH_DANGER,
    DEMON_BLUFF_TRIPLET,
    BLUFF_SUPPORT_DIAGNOSTICS,
    BLUFF_EXECUTION_TRAITS,
    HEALTHY_BUNDLE_INFORMATION,
    BUNDLE_CONFIRMATION_CHAIN_DIAGNOSTICS,
}

internal enum class Sde2D5FPersistenceBoundary {
    REFERENCE_ONLY,
    DRUNK_SHOWN_IDENTITY_PERSISTENT_CLUE_PLANNED_UNTIL_SHOWN,
    DEMON_BLUFF_TRIPLET_PLANNED_UNTIL_REVEAL_THEN_PERSISTENT,
    FIRST_NIGHT_CLUE_PLANNED_UNTIL_SHOWN_THEN_COMMITTED,
    DESCRIPTIVE_BUNDLE_EVIDENCE_ONLY,
}

internal data class Sde2D5FControlSurface(
    val lifecycleStage: Sde2D5FLifecycleStage,
    val decisionOwner: Sde2D5FDecisionOwner,
    val controllableVariables: Set<Sde2D5FPolicyVariable>,
    val diagnosticOnlyVariables: Set<Sde2D5FPolicyVariable>,
    val persistenceBoundary: Sde2D5FPersistenceBoundary,
) {
    init {
        require(controllableVariables.intersect(diagnosticOnlyVariables).isEmpty()) {
            "A D5F policy variable cannot be both controllable and diagnostic-only in one lifecycle record."
        }
    }
}

internal enum class Sde2D5FReviewReason {
    STRATEGIC_COLLAPSE,
    EXCESSIVE_CONFIRMATION_CHAIN,
    INSUFFICIENT_HEALTHY_INFORMATION,
    MECHANICALLY_USEFUL_TOPOLOGY_NEUTRAL,
    DRUNK_MARGINAL_PATHOLOGY,
    DRUNK_MARGINAL_ACCEPTABLE,
    IMPAIRED_CLUE_TOO_REVEALING,
    IMPAIRED_CLUE_COHERENT,
    BLUFF_SUPPORT_FRAGILE,
    BLUFF_SUPPORT_ROBUST,
    BLUFF_EXECUTION_BURDEN,
    BLUFF_NARRATIVE_REDUNDANCY,
    BLUFF_COHERENCE_FRAGILE,
    BLUFF_ROUTES_USABLE,
    CROSS_CHANNEL_NARRATIVE_COHERENCE,
    CROSS_REGIME_REFERENCE,
    OTHER_EXPLICIT_REVIEW_REASON,
}

internal sealed interface Sde2D5FReviewDetails {
    data class BaselineReference(
        val point: Sde2D5CalibrationEvidencePoint,
    ) : Sde2D5FReviewDetails

    data class DrunkContrast(
        val contrast: Sde2D5DrunkCalibrationContrast,
    ) : Sde2D5FReviewDetails

    data class DemonBluffSupport(
        val selection: Sde2D5DemonBluffCalibrationSelection,
    ) : Sde2D5FReviewDetails

    data class RoleInformationContrast(
        val evidence: Sde2D5RoleInformationCalibrationEvidence,
    ) : Sde2D5FReviewDetails

    data class BundleConfirmationChain(
        val selection: Sde2D5BundleConfirmationSelection,
    ) : Sde2D5FReviewDetails
}

internal data class Sde2D5FReviewRecord(
    val reviewId: String,
    val evidenceKind: Sde2D5FReviewEvidenceKind,
    val reviewability: Sde2D5FReviewability,
    val regime: Sde2D5PlayerCountRegime,
    val profileKind: Sde2D5SetupProfileKind,
    val contrastId: String?,
    val controlSurface: Sde2D5FControlSurface,
    val details: Sde2D5FReviewDetails,
) {
    init {
        require(reviewId.isNotBlank())
        require(contrastId == null || contrastId.isNotBlank())
        require(
            (reviewability == Sde2D5FReviewability.REFERENCE) ==
                (evidenceKind == Sde2D5FReviewEvidenceKind.BASELINE_REFERENCE),
        ) {
            "Only D5F baseline reference records may be non-labelable."
        }
    }

    val initialLabel: FirstNightBeginnerCorpusLabel?
        get() = when (reviewability) {
            Sde2D5FReviewability.REFERENCE -> null
            Sde2D5FReviewability.REVIEWABLE -> FirstNightBeginnerCorpusLabel.UNREVIEWED
        }
}

internal data class Sde2D5FCalibrationReviewMaterial(
    val sealedHoldoutScenarioCount: Int,
    val records: List<Sde2D5FReviewRecord>,
) {
    init {
        require(sealedHoldoutScenarioCount >= 0)
        require(records.isNotEmpty())
        require(records.map(Sde2D5FReviewRecord::reviewId).distinct().size == records.size) {
            "D5F review IDs must be unique."
        }
        require(records == records.sortedBy(Sde2D5FReviewRecord::reviewId)) {
            "D5F review material must be deterministically ordered by review ID."
        }
    }
}

internal object Sde2D5FCalibrationReviewBuilder {
    fun build(
        sealedHoldoutScenarioCount: Int,
        baselineReferences: List<Sde2D5CalibrationEvidencePoint>,
        drunkContrasts: List<Sde2D5DrunkCalibrationContrast>,
        bluffSelections: List<Sde2D5DemonBluffCalibrationSelection>,
        roleInformationEvidence: List<Sde2D5RoleInformationCalibrationEvidence>,
        confirmationSelections: List<Sde2D5BundleConfirmationSelection> = emptyList(),
    ): Sde2D5FCalibrationReviewMaterial {
        require(baselineReferences.all { it.evidenceKind == Sde2D5EvidenceKind.BASELINE })

        val records = buildList {
            baselineReferences.forEach { point ->
                add(
                    Sde2D5FReviewRecord(
                        reviewId = "d5f:baseline:${point.pointId}",
                        evidenceKind = Sde2D5FReviewEvidenceKind.BASELINE_REFERENCE,
                        reviewability = Sde2D5FReviewability.REFERENCE,
                        regime = point.regime,
                        profileKind = point.profileKind,
                        contrastId = point.contrastId,
                        controlSurface = Sde2D5FControlSurface(
                            lifecycleStage = Sde2D5FLifecycleStage.CROSS_STAGE_REFERENCE,
                            decisionOwner = Sde2D5FDecisionOwner.NONE_REFERENCE,
                            controllableVariables = emptySet(),
                            diagnosticOnlyVariables = setOf(
                                Sde2D5FPolicyVariable.NORMALIZED_STRATEGIC_DIAGNOSTICS,
                            ),
                            persistenceBoundary = Sde2D5FPersistenceBoundary.REFERENCE_ONLY,
                        ),
                        details = Sde2D5FReviewDetails.BaselineReference(point),
                    ),
                )
            }
            drunkContrasts.forEach { contrast ->
                val representative = contrast.truthful
                add(
                    Sde2D5FReviewRecord(
                        reviewId = "d5f:drunk:${contrast.contrastId}",
                        evidenceKind = Sde2D5FReviewEvidenceKind.DRUNK_CONTRAST,
                        reviewability = Sde2D5FReviewability.REVIEWABLE,
                        regime = representative.fullBundle.regime,
                        profileKind = representative.fullBundle.profileKind,
                        contrastId = contrast.contrastId,
                        controlSurface = Sde2D5FControlSurface(
                            lifecycleStage = Sde2D5FLifecycleStage.FIRST_NIGHT_AFTER_SETUP_PERSISTENCE,
                            decisionOwner = Sde2D5FDecisionOwner.STORYTELLER_SDE,
                            controllableVariables = setOf(
                                Sde2D5FPolicyVariable.IMPAIRED_CLUE_OUTPUT,
                            ),
                            diagnosticOnlyVariables = setOf(
                                Sde2D5FPolicyVariable.SEMANTIC_TRUTH_RELATION,
                                Sde2D5FPolicyVariable.COUNTERFACTUAL_HEALTHY_TRUTH_DANGER,
                                Sde2D5FPolicyVariable.NORMALIZED_STRATEGIC_DIAGNOSTICS,
                            ),
                            persistenceBoundary =
                                Sde2D5FPersistenceBoundary.DRUNK_SHOWN_IDENTITY_PERSISTENT_CLUE_PLANNED_UNTIL_SHOWN,
                        ),
                        details = Sde2D5FReviewDetails.DrunkContrast(contrast),
                    ),
                )
            }
            bluffSelections.forEach { selection ->
                val evidence = selection.evidence
                add(
                    Sde2D5FReviewRecord(
                        reviewId = "d5f:bluff:${evidence.candidateId}:r${evidence.recipientSeat}",
                        evidenceKind = Sde2D5FReviewEvidenceKind.DEMON_BLUFF_SUPPORT,
                        reviewability = Sde2D5FReviewability.REVIEWABLE,
                        regime = evidence.regime,
                        profileKind = evidence.profileKind,
                        contrastId = evidence.candidateId,
                        controlSurface = Sde2D5FControlSurface(
                            lifecycleStage = Sde2D5FLifecycleStage.PRE_GAME_BEFORE_BLUFF_REVEAL,
                            decisionOwner = Sde2D5FDecisionOwner.STORYTELLER_SDE,
                            controllableVariables = setOf(
                                Sde2D5FPolicyVariable.DEMON_BLUFF_TRIPLET,
                            ),
                            diagnosticOnlyVariables = setOf(
                                Sde2D5FPolicyVariable.BLUFF_SUPPORT_DIAGNOSTICS,
                                Sde2D5FPolicyVariable.BLUFF_EXECUTION_TRAITS,
                            ),
                            persistenceBoundary =
                                Sde2D5FPersistenceBoundary.DEMON_BLUFF_TRIPLET_PLANNED_UNTIL_REVEAL_THEN_PERSISTENT,
                        ),
                        details = Sde2D5FReviewDetails.DemonBluffSupport(selection),
                    ),
                )
            }
            roleInformationEvidence.forEach { evidence ->
                val point = evidence.point
                add(
                    Sde2D5FReviewRecord(
                        reviewId = "d5f:role-info:${point.pointId}",
                        evidenceKind = Sde2D5FReviewEvidenceKind.ROLE_INFORMATION_CONTRAST,
                        reviewability = Sde2D5FReviewability.REVIEWABLE,
                        regime = point.regime,
                        profileKind = point.profileKind,
                        contrastId = point.contrastId,
                        controlSurface = when (evidence.control) {
                            com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl.STORYTELLER_CONTROLLED ->
                                Sde2D5FControlSurface(
                                    lifecycleStage = Sde2D5FLifecycleStage.FIRST_NIGHT_AFTER_SETUP_PERSISTENCE,
                                    decisionOwner = Sde2D5FDecisionOwner.STORYTELLER_SDE,
                                    controllableVariables = setOf(
                                        Sde2D5FPolicyVariable.HEALTHY_BUNDLE_INFORMATION,
                                    ),
                                    diagnosticOnlyVariables = setOf(
                                        Sde2D5FPolicyVariable.NORMALIZED_STRATEGIC_DIAGNOSTICS,
                                    ),
                                    persistenceBoundary =
                                        Sde2D5FPersistenceBoundary.FIRST_NIGHT_CLUE_PLANNED_UNTIL_SHOWN_THEN_COMMITTED,
                                )
                            com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl.RULE_DETERMINED ->
                                Sde2D5FControlSurface(
                                    lifecycleStage = Sde2D5FLifecycleStage.FIRST_NIGHT_AFTER_SETUP_PERSISTENCE,
                                    decisionOwner = Sde2D5FDecisionOwner.CALIBRATION_DIAGNOSTIC_ONLY,
                                    controllableVariables = emptySet(),
                                    diagnosticOnlyVariables = setOf(
                                        Sde2D5FPolicyVariable.HEALTHY_BUNDLE_INFORMATION,
                                        Sde2D5FPolicyVariable.NORMALIZED_STRATEGIC_DIAGNOSTICS,
                                    ),
                                    persistenceBoundary =
                                        Sde2D5FPersistenceBoundary.FIRST_NIGHT_CLUE_PLANNED_UNTIL_SHOWN_THEN_COMMITTED,
                                )
                        },
                        details = Sde2D5FReviewDetails.RoleInformationContrast(evidence),
                    ),
                )
            }
            confirmationSelections.forEach { selection ->
                val evidence = selection.evidence
                add(
                    Sde2D5FReviewRecord(
                        reviewId = "d5f:confirmation:${evidence.signatureId}",
                        evidenceKind = Sde2D5FReviewEvidenceKind.BUNDLE_CONFIRMATION_CHAIN,
                        reviewability = Sde2D5FReviewability.REVIEWABLE,
                        regime = evidence.regime,
                        profileKind = evidence.profileKind,
                        contrastId = evidence.signatureId,
                        controlSurface = Sde2D5FControlSurface(
                            lifecycleStage = Sde2D5FLifecycleStage.FIRST_NIGHT_AFTER_SETUP_PERSISTENCE,
                            decisionOwner = Sde2D5FDecisionOwner.CALIBRATION_DIAGNOSTIC_ONLY,
                            controllableVariables = emptySet(),
                            diagnosticOnlyVariables = setOf(
                                Sde2D5FPolicyVariable.HEALTHY_BUNDLE_INFORMATION,
                                Sde2D5FPolicyVariable.NORMALIZED_STRATEGIC_DIAGNOSTICS,
                                Sde2D5FPolicyVariable.BUNDLE_CONFIRMATION_CHAIN_DIAGNOSTICS,
                            ),
                            persistenceBoundary =
                                Sde2D5FPersistenceBoundary.DESCRIPTIVE_BUNDLE_EVIDENCE_ONLY,
                        ),
                        details = Sde2D5FReviewDetails.BundleConfirmationChain(selection),
                    ),
                )
            }
        }.sortedBy(Sde2D5FReviewRecord::reviewId)

        return Sde2D5FCalibrationReviewMaterial(
            sealedHoldoutScenarioCount = sealedHoldoutScenarioCount,
            records = records,
        )
    }
}

internal object Sde2D5FCalibrationReviewRenderer {
    fun renderMarkdown(
        material: Sde2D5FCalibrationReviewMaterial,
    ): String = buildString {
        appendLine("SDE_2D5F_CALIBRATION_REVIEW_START")
        appendLine("# SDE-2D5F BEGINNER calibration review")
        appendLine()
        appendLine("Sealed holdout scenarios: ${material.sealedHoldoutScenarioCount}")
        appendLine("Holdout diagnostics are sealed until gate freeze.")
        appendLine("Generated reviewable records start UNREVIEWED; human labels live in a separate manifest.")
        appendLine()

        material.records.forEach { record ->
            appendLine("## ${record.reviewId}")
            appendLine("evidenceKind=${record.evidenceKind}")
            appendLine("reviewability=${record.reviewability}")
            appendLine("initialLabel=${record.initialLabel?.name ?: "N/A"}")
            appendLine("regime=${record.regime}")
            appendLine("profile=${record.profileKind}")
            appendLine("contrastId=${record.contrastId ?: "N/A"}")
            appendLine("lifecycleStage=${record.controlSurface.lifecycleStage}")
            appendLine("decisionOwner=${record.controlSurface.decisionOwner}")
            appendLine(
                "controllableVariables=" +
                    record.controlSurface.controllableVariables.sortedBy { it.name }.joinToString(","),
            )
            appendLine(
                "diagnosticOnlyVariables=" +
                    record.controlSurface.diagnosticOnlyVariables.sortedBy { it.name }.joinToString(","),
            )
            appendLine("persistenceBoundary=${record.controlSurface.persistenceBoundary}")
            appendDetails(record.details)
            appendLine()
        }

        appendLine("SDE_2D5F_CALIBRATION_REVIEW_END")
    }

    private fun StringBuilder.appendDetails(
        details: Sde2D5FReviewDetails,
    ) {
        when (details) {
            is Sde2D5FReviewDetails.BaselineReference -> {
                appendLine("detail=baseline-reference")
                appendPoint(details.point)
            }

            is Sde2D5FReviewDetails.DrunkContrast -> {
                val contrast = details.contrast
                appendLine("detail=drunk-contrast")
                appendLine("counterfactualHealthyTruthDanger:")
                appendNormalized(
                    "counterfactualHealthyTruthDanger",
                    contrast.counterfactualHealthyTruthDanger,
                )
                contrast.candidates.forEach { candidate ->
                    val evidence = candidate.evidence
                    val prefix = "candidate[${candidate.kind}]"
                    appendLine("$prefix.id=${evidence.candidateId}")
                    appendLine("$prefix.semanticTruth=${evidence.semanticTruth}")
                    appendLine("$prefix.rawWorldsRemoved=${evidence.rawWorldsRemoved}")
                    appendLine("$prefix.healthyCore:")
                    appendPoint(evidence.healthyCore, prefix = "  $prefix.")
                    appendLine("$prefix.fullBundle:")
                    appendPoint(evidence.fullBundle, prefix = "  $prefix.")
                    appendNormalized("$prefix.drunkMarginal", evidence.marginalNormalized)
                }
            }

            is Sde2D5FReviewDetails.DemonBluffSupport -> {
                val selection = details.selection
                val evidence = selection.evidence
                appendLine("detail=demon-bluff-support")
                appendLine("candidateId=${evidence.candidateId}")
                appendLine("roles=${evidence.roles.joinToString(",") { it.value }}")
                appendLine("supportedRoleCount=${evidence.supportedRoleCount}")
                appendLine("unionStrategicWorldCount=${evidence.unionStrategicWorldCount}")
                appendLine("sharedStrategicWorldCount=${evidence.sharedStrategicWorldCount}")
                appendLine("sharedToUnionRetention=${renderRatio(evidence.sharedToUnionRetention)}")
                appendLine("distinctRoleStrategicPatternCount=${evidence.distinctRoleStrategicPatternCount}")
                appendLine(
                    "individualSupportFloorStrategicWorldCount=" +
                        evidence.individualSupportFloorStrategicWorldCount,
                )
                appendLine("claimCadenceClassCount=${evidence.claimCadenceClassCount}")
                appendLine("narrativeRouteClassCount=${evidence.narrativeRouteClassCount}")
                appendLine(
                    "externalHumanObservedCaseIds=" +
                        evidence.externalHumanObservedCaseIds.sorted().joinToString(","),
                )
                appendLine(
                    "selectionReasons=${selection.selectionReasons.sortedBy { it.name }.joinToString(",")}",
                )
                evidence.roles.sortedBy { it.value }.forEach { role ->
                    val trait = evidence.roleTraits.getValue(role)
                    appendLine(
                        "roleTrait[${role.value}]=" +
                            "executionBurden=${trait.beginnerExecutionBurden}," +
                            "claimBurden=${trait.claimBurden}," +
                            "cadence=${trait.claimCadence}," +
                            "route=${trait.narrativeRouteClass}",
                    )
                }
                evidence.pairwiseStrategicCoverage.forEach { pair ->
                    appendLine(
                        "pairCoverage[${pair.firstRole.value},${pair.secondRole.value}]=" +
                            "shared=${pair.sharedStrategicWorldCount}," +
                            "union=${pair.unionStrategicWorldCount}," +
                            "retention=${renderRatio(pair.sharedToUnionRetention)}",
                    )
                }
                evidence.roles.sortedBy { it.value }.forEach { role ->
                    appendNormalized(
                        "roleSupport[${role.value}]",
                        evidence.roleSupportNormalized.getValue(role),
                    )
                }
            }

            is Sde2D5FReviewDetails.RoleInformationContrast -> {
                val evidence = details.evidence
                appendLine("detail=role-information-contrast")
                appendLine("sourceSeat=${evidence.sourceSeat}")
                appendLine("sourceRole=${evidence.sourceRole.value}")
                appendLine("control=${evidence.control}")
                appendLine("rawWorldsRemoved=${evidence.rawWorldsRemoved}")
                appendLine("hasMechanicalInformationGain=${evidence.hasMechanicalInformationGain}")
                appendLine("topologyNeutral=${evidence.topologyNeutral}")
                appendPoint(evidence.point)
            }
            is Sde2D5FReviewDetails.BundleConfirmationChain -> {
                val selection = details.selection
                val evidence = selection.evidence
                val worst = evidence.worstGoodRecipient
                appendLine("detail=bundle-confirmation-chain")
                appendLine("signatureId=${evidence.signatureId}")
                appendLine("evaluatedGoodRecipientSeats=${evidence.byRecipient.map { it.recipientSeat }.sorted().joinToString(",")}")
                appendLine("worstGoodRecipientSeat=${evidence.worstGoodRecipientSeat}")
                appendLine("worstGoodRecipientRestoringClueCount=${evidence.worstGoodRecipientRestoringClueCount}")
                appendLine("multiChannelCollapse=${evidence.hasMultiChannelCollapse}")
                appendLine(
                    "selectionReasons=" +
                        selection.selectionReasons.sortedBy { it.name }.joinToString(","),
                )
                appendNormalized("fullBundle", worst.fullBundleNormalized)
                worst.leaveOneOut.forEach { omitted ->
                    val prefix = "leaveOneOut[${omitted.omittedObservationKey}]"
                    appendLine("$prefix.restoresDemonCover=${omitted.restoresDemonCover}")
                    appendLine("$prefix.restoresStrategicTopology=${omitted.restoresStrategicTopology}")
                    appendLine("$prefix.afterStrategicWorldCount=${omitted.afterStrategicWorldCount}")
                    appendLine("$prefix.afterDemonCoverCount=${omitted.afterDemonCoverCount}")
                    appendNormalized(prefix, omitted.normalized)
                }
            }
        }
    }

    private fun StringBuilder.appendPoint(
        point: Sde2D5CalibrationEvidencePoint,
        prefix: String = "",
    ) {
        appendLine("${prefix}pointId=${point.pointId}")
        appendLine("${prefix}beforeStrategicWorldCount=${point.beforeStrategicWorldCount}")
        appendLine("${prefix}afterStrategicWorldCount=${point.afterStrategicWorldCount}")
        point.rawMechanicalBefore?.let { appendLine("${prefix}rawMechanicalBefore=$it") }
        point.rawMechanicalAfter?.let { appendLine("${prefix}rawMechanicalAfter=$it") }
        appendNormalized("${prefix}normalized", point.normalized)
    }

    private fun StringBuilder.appendNormalized(
        prefix: String,
        normalized: NormalizedStrategicDiagnostics,
    ) {
        appendLine("$prefix.demonCoverRetention=${renderRatio(normalized.demonCoverRetention)}")
        appendLine("$prefix.evilTopologyRetention=${renderRatio(normalized.evilTopologyRetention)}")
        appendLine("$prefix.evilCoverRetention=${renderRatio(normalized.evilCoverRetention)}")
        appendLine("$prefix.forcedGoodFraction=${renderRatio(normalized.forcedGoodFraction)}")
    }

    private fun renderRatio(ratio: StrategicRatio): String = when (ratio) {
        is StrategicRatio.Defined -> "${ratio.numerator}/${ratio.denominator}"
        StrategicRatio.Undefined -> "undefined"
    }
}
