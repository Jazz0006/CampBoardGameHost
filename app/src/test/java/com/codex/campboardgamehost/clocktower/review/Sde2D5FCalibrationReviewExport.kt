package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio

internal enum class Sde2D5FReviewEvidenceKind {
    BASELINE_REFERENCE,
    DRUNK_CONTRAST,
    DEMON_BLUFF_SUPPORT,
    ROLE_INFORMATION_CONTRAST,
}

internal enum class Sde2D5FReviewability {
    REFERENCE,
    REVIEWABLE,
}

internal enum class Sde2D5FReviewReason {
    STRATEGIC_COLLAPSE,
    INSUFFICIENT_HEALTHY_INFORMATION,
    MECHANICALLY_USEFUL_TOPOLOGY_NEUTRAL,
    DRUNK_MARGINAL_PATHOLOGY,
    DRUNK_MARGINAL_ACCEPTABLE,
    BLUFF_SUPPORT_FRAGILE,
    BLUFF_SUPPORT_ROBUST,
    CROSS_REGIME_REFERENCE,
    OTHER_EXPLICIT_REVIEW_REASON,
}

internal sealed interface Sde2D5FReviewDetails {
    data class BaselineReference(
        val point: Sde2D5CalibrationEvidencePoint,
    ) : Sde2D5FReviewDetails

    data class DrunkContrast(
        val evidence: Sde2D5DrunkCalibrationEvidence,
    ) : Sde2D5FReviewDetails

    data class DemonBluffSupport(
        val selection: Sde2D5DemonBluffCalibrationSelection,
    ) : Sde2D5FReviewDetails

    data class RoleInformationContrast(
        val evidence: Sde2D5RoleInformationCalibrationEvidence,
    ) : Sde2D5FReviewDetails
}

internal data class Sde2D5FReviewRecord(
    val reviewId: String,
    val evidenceKind: Sde2D5FReviewEvidenceKind,
    val reviewability: Sde2D5FReviewability,
    val regime: Sde2D5PlayerCountRegime,
    val profileKind: Sde2D5SetupProfileKind,
    val contrastId: String?,
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
        drunkEvidence: List<Sde2D5DrunkCalibrationEvidence>,
        bluffSelections: List<Sde2D5DemonBluffCalibrationSelection>,
        roleInformationEvidence: List<Sde2D5RoleInformationCalibrationEvidence>,
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
                        details = Sde2D5FReviewDetails.BaselineReference(point),
                    ),
                )
            }
            drunkEvidence.forEach { evidence ->
                add(
                    Sde2D5FReviewRecord(
                        reviewId = "d5f:drunk:${evidence.candidateId}",
                        evidenceKind = Sde2D5FReviewEvidenceKind.DRUNK_CONTRAST,
                        reviewability = Sde2D5FReviewability.REVIEWABLE,
                        regime = evidence.fullBundle.regime,
                        profileKind = evidence.fullBundle.profileKind,
                        contrastId = evidence.candidateId,
                        details = Sde2D5FReviewDetails.DrunkContrast(evidence),
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
                        details = Sde2D5FReviewDetails.RoleInformationContrast(evidence),
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
                val evidence = details.evidence
                appendLine("detail=drunk-contrast")
                appendLine("candidateId=${evidence.candidateId}")
                appendLine("semanticTruth=${evidence.semanticTruth}")
                appendLine("rawWorldsRemoved=${evidence.rawWorldsRemoved}")
                appendLine("healthyCore:")
                appendPoint(evidence.healthyCore, prefix = "  ")
                appendLine("fullBundle:")
                appendPoint(evidence.fullBundle, prefix = "  ")
                appendNormalized("drunkMarginal", evidence.marginalNormalized)
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
                    "selectionReasons=${selection.selectionReasons.sortedBy { it.name }.joinToString(",")}",
                )
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
                appendLine("rawWorldsRemoved=${evidence.rawWorldsRemoved}")
                appendLine("hasMechanicalInformationGain=${evidence.hasMechanicalInformationGain}")
                appendLine("topologyNeutral=${evidence.topologyNeutral}")
                appendPoint(evidence.point)
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
