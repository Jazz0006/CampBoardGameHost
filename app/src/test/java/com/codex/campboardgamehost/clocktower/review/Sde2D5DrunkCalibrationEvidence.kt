package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightDrunkFortuneTellerCandidateEvaluation
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightDrunkMarginalDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightDrunkNumericCandidateEvaluation
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightDrunkPairCandidateEvaluation
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightDrunkWholeBundleCandidateExactEvaluation
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnosticsProjector
import java.math.BigInteger

internal data class Sde2D5DrunkCalibrationEvidence(
    val candidateId: String,
    val semanticTruth: SemanticTruth,
    val healthyCore: Sde2D5CalibrationEvidencePoint,
    val fullBundle: Sde2D5CalibrationEvidencePoint,
    val marginalNormalized: NormalizedStrategicDiagnostics,
    val rawWorldsRemoved: BigInteger,
)

internal enum class Sde2D5DrunkContrastCandidateKind {
    TRUTHFUL,
    MILD_FALSE,
    STRONGER_FALSE,
}

internal data class Sde2D5DrunkContrastCandidate(
    val kind: Sde2D5DrunkContrastCandidateKind,
    val evidence: Sde2D5DrunkCalibrationEvidence,
) {
    init {
        when (kind) {
            Sde2D5DrunkContrastCandidateKind.TRUTHFUL ->
                require(evidence.semanticTruth == SemanticTruth.TRUE)
            Sde2D5DrunkContrastCandidateKind.MILD_FALSE,
            Sde2D5DrunkContrastCandidateKind.STRONGER_FALSE ->
                require(evidence.semanticTruth == SemanticTruth.FALSE)
        }
    }
}

internal data class Sde2D5DrunkCalibrationContrast(
    val contrastId: String,
    val counterfactualHealthyTruthDanger: NormalizedStrategicDiagnostics,
    val candidates: List<Sde2D5DrunkContrastCandidate>,
) {
    init {
        require(contrastId.isNotBlank())
        require(candidates.count { it.kind == Sde2D5DrunkContrastCandidateKind.TRUTHFUL } == 1)
        require(candidates.count { it.kind == Sde2D5DrunkContrastCandidateKind.MILD_FALSE } == 1)
        require(candidates.count { it.kind == Sde2D5DrunkContrastCandidateKind.STRONGER_FALSE } <= 1)
        require(candidates.map { it.evidence.candidateId }.distinct().size == candidates.size)

        val healthyCore = candidates.first().evidence.healthyCore
        candidates.forEach { candidate ->
            val candidateCore = candidate.evidence.healthyCore
            require(candidateCore.playerCount == healthyCore.playerCount)
            require(candidateCore.profileKind == healthyCore.profileKind)
            require(candidateCore.beforeStrategicWorldCount == healthyCore.beforeStrategicWorldCount)
            require(candidateCore.afterStrategicWorldCount == healthyCore.afterStrategicWorldCount)
            require(candidateCore.normalized == healthyCore.normalized)
            require(candidateCore.rawMechanicalBefore == healthyCore.rawMechanicalBefore)
            require(candidateCore.rawMechanicalAfter == healthyCore.rawMechanicalAfter)
        }
    }

    val truthful: Sde2D5DrunkCalibrationEvidence
        get() = candidates.single { it.kind == Sde2D5DrunkContrastCandidateKind.TRUTHFUL }.evidence

    val mildFalse: Sde2D5DrunkCalibrationEvidence
        get() = candidates.single { it.kind == Sde2D5DrunkContrastCandidateKind.MILD_FALSE }.evidence

    val strongerFalse: Sde2D5DrunkCalibrationEvidence?
        get() = candidates.singleOrNull {
            it.kind == Sde2D5DrunkContrastCandidateKind.STRONGER_FALSE
        }?.evidence
}

/**
 * Builds one same-setup review contrast without treating truth relation as a quality ordering.
 *
 * The truthful candidate supplies counterfactualHealthyTruthDanger because the actual source is
 * already a legally committed Drunk. False candidates are described as mild/stronger only by the
 * observed evil-topology pressure in this calibration fixture; semantic falsehood itself receives
 * no reward.
 */
internal object Sde2D5DrunkCalibrationContrastBuilder {
    fun build(
        contrastId: String,
        evidence: List<Sde2D5DrunkCalibrationEvidence>,
    ): Sde2D5DrunkCalibrationContrast {
        require(evidence.isNotEmpty())
        val truthful = evidence
            .filter { it.semanticTruth == SemanticTruth.TRUE }
            .sortedBy(Sde2D5DrunkCalibrationEvidence::candidateId)
            .single()
        val falseCandidates = evidence
            .filter { it.semanticTruth == SemanticTruth.FALSE }
            .sortedBy(Sde2D5DrunkCalibrationEvidence::candidateId)
        require(falseCandidates.isNotEmpty()) {
            "D5 Drunk contrast requires at least one legal false candidate."
        }

        val mild = falseCandidates.maxWith(
            Comparator { left, right ->
                val pressureOrder = compareRetention(
                    left.marginalNormalized.evilTopologyRetention,
                    right.marginalNormalized.evilTopologyRetention,
                )
                if (pressureOrder != 0) pressureOrder else right.candidateId.compareTo(left.candidateId)
            },
        )
        val strongest = falseCandidates.minWith(
            Comparator { left, right ->
                val pressureOrder = compareRetention(
                    left.marginalNormalized.evilTopologyRetention,
                    right.marginalNormalized.evilTopologyRetention,
                )
                if (pressureOrder != 0) pressureOrder else left.candidateId.compareTo(right.candidateId)
            },
        )
        val hasStrictStrongerFalse =
            strongest.candidateId != mild.candidateId &&
                compareRetention(
                    strongest.marginalNormalized.evilTopologyRetention,
                    mild.marginalNormalized.evilTopologyRetention,
                ) < 0

        return Sde2D5DrunkCalibrationContrast(
            contrastId = contrastId,
            counterfactualHealthyTruthDanger = truthful.marginalNormalized,
            candidates = buildList {
                add(
                    Sde2D5DrunkContrastCandidate(
                        kind = Sde2D5DrunkContrastCandidateKind.TRUTHFUL,
                        evidence = truthful,
                    ),
                )
                add(
                    Sde2D5DrunkContrastCandidate(
                        kind = Sde2D5DrunkContrastCandidateKind.MILD_FALSE,
                        evidence = mild,
                    ),
                )
                if (hasStrictStrongerFalse) {
                    add(
                        Sde2D5DrunkContrastCandidate(
                            kind = Sde2D5DrunkContrastCandidateKind.STRONGER_FALSE,
                            evidence = strongest,
                        ),
                    )
                }
            },
        )
    }

    private fun compareRetention(
        left: com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio,
        right: com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio,
    ): Int = when {
        left is com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio.Undefined &&
            right is com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio.Undefined -> 0
        left is com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio.Undefined -> -1
        right is com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio.Undefined -> 1
        left is com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio.Defined &&
            right is com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio.Defined ->
            (left.numerator.toLong() * right.denominator.toLong())
                .compareTo(right.numerator.toLong() * left.denominator.toLong())
        else -> error("Unknown strategic ratio implementation.")
    }
}

/**
 * Review-only projection of the existing D2D1 HealthyCore / FullBundle / DrunkMarginal contract.
 *
 * It adds no Drunk legality, clue generation or ranking. The common normalized diagnostics owner
 * projects both absolute bundle state and the marginal HealthyCore.after -> FullBundle.after change.
 */
internal object Sde2D5DrunkCalibrationEvidenceProjector {
    fun project(
        playerCount: Int,
        profileKind: Sde2D5SetupProfileKind,
        healthyCore: ExactHypotheticalObservationBundleDiagnostics,
        candidate: FirstNightDrunkWholeBundleCandidateExactEvaluation,
    ): Sde2D5DrunkCalibrationEvidence =
        projectCandidate(
            playerCount = playerCount,
            profileKind = profileKind,
            healthyCore = healthyCore,
            candidateId = candidate.candidateId,
            semanticTruth = candidate.semanticTruth,
            fullBundleByRecipient = candidate.fullBundleByRecipient,
            marginalByRecipient = candidate.marginalByRecipient,
        )

    fun project(
        playerCount: Int,
        profileKind: Sde2D5SetupProfileKind,
        healthyCore: ExactHypotheticalObservationBundleDiagnostics,
        candidate: FirstNightDrunkNumericCandidateEvaluation,
    ): Sde2D5DrunkCalibrationEvidence =
        projectCandidate(
            playerCount = playerCount,
            profileKind = profileKind,
            healthyCore = healthyCore,
            candidateId = candidate.candidateId,
            semanticTruth = candidate.semanticTruth,
            fullBundleByRecipient = candidate.fullBundleByRecipient,
            marginalByRecipient = candidate.marginalByRecipient,
        )

    fun project(
        playerCount: Int,
        profileKind: Sde2D5SetupProfileKind,
        healthyCore: ExactHypotheticalObservationBundleDiagnostics,
        candidate: FirstNightDrunkPairCandidateEvaluation,
    ): Sde2D5DrunkCalibrationEvidence =
        projectCandidate(
            playerCount = playerCount,
            profileKind = profileKind,
            healthyCore = healthyCore,
            candidateId = candidate.candidateId,
            semanticTruth = candidate.semanticTruth,
            fullBundleByRecipient = candidate.fullBundleByRecipient,
            marginalByRecipient = candidate.marginalByRecipient,
        )

    fun project(
        playerCount: Int,
        profileKind: Sde2D5SetupProfileKind,
        healthyCore: ExactHypotheticalObservationBundleDiagnostics,
        candidate: FirstNightDrunkFortuneTellerCandidateEvaluation,
    ): Sde2D5DrunkCalibrationEvidence =
        projectCandidate(
            playerCount = playerCount,
            profileKind = profileKind,
            healthyCore = healthyCore,
            candidateId = candidate.candidateId,
            semanticTruth = candidate.semanticTruth,
            fullBundleByRecipient = candidate.fullBundleByRecipient,
            marginalByRecipient = candidate.marginalByRecipient,
        )

    private fun projectCandidate(
        playerCount: Int,
        profileKind: Sde2D5SetupProfileKind,
        healthyCore: ExactHypotheticalObservationBundleDiagnostics,
        candidateId: String,
        semanticTruth: SemanticTruth,
        fullBundleByRecipient: List<ExactHypotheticalObservationBundleDiagnostics>,
        marginalByRecipient: List<FirstNightDrunkMarginalDiagnostics>,
    ): Sde2D5DrunkCalibrationEvidence {
        require(playerCount in 5..15)
        val recipientSeat = healthyCore.recipientSeat
        val fullBundle = fullBundleByRecipient
            .single { it.recipientSeat == recipientSeat }
        val marginal = marginalByRecipient
            .single { it.recipientSeat == recipientSeat }

        require(fullBundle.beforeStructure == healthyCore.beforeStructure) {
            "Drunk FullBundle and HealthyCore calibration evidence must share one baseline structure."
        }
        require(fullBundle.before == healthyCore.before) {
            "Drunk FullBundle and HealthyCore calibration evidence must share one mechanical baseline."
        }
        require(fullBundle.after.value <= healthyCore.after.value) {
            "Drunk FullBundle cannot create mechanical worlds relative to HealthyCore."
        }
        require(marginal.rawWorldsRemoved == healthyCore.after.value - fullBundle.after.value) {
            "Drunk marginal raw-world delta must match HealthyCore minus FullBundle."
        }

        return Sde2D5DrunkCalibrationEvidence(
            candidateId = candidateId,
            semanticTruth = semanticTruth,
            healthyCore = evidencePoint(
                pointId = "$candidateId:healthy-core",
                contrastId = candidateId,
                evidenceKind = Sde2D5EvidenceKind.DRUNK_HEALTHY_CORE,
                playerCount = playerCount,
                profileKind = profileKind,
                diagnostic = healthyCore,
            ),
            fullBundle = evidencePoint(
                pointId = "$candidateId:full-bundle",
                contrastId = candidateId,
                evidenceKind = Sde2D5EvidenceKind.DRUNK_FULL_BUNDLE,
                playerCount = playerCount,
                profileKind = profileKind,
                diagnostic = fullBundle,
            ),
            marginalNormalized = NormalizedStrategicDiagnosticsProjector.project(
                before = healthyCore.afterStructure,
                after = fullBundle.afterStructure,
                playerCount = playerCount,
            ),
            rawWorldsRemoved = marginal.rawWorldsRemoved,
        )
    }

    private fun evidencePoint(
        pointId: String,
        contrastId: String,
        evidenceKind: Sde2D5EvidenceKind,
        playerCount: Int,
        profileKind: Sde2D5SetupProfileKind,
        diagnostic: ExactHypotheticalObservationBundleDiagnostics,
    ) = Sde2D5CalibrationEvidencePoint(
        pointId = pointId,
        playerCount = playerCount,
        regime = Sde2D5PlayerCountRegime.from(playerCount),
        profileKind = profileKind,
        evidenceKind = evidenceKind,
        contrastId = contrastId,
        beforeStrategicWorldCount = diagnostic.beforeStructure.distinctStrategicWorldCount,
        afterStrategicWorldCount = diagnostic.afterStructure.distinctStrategicWorldCount,
        normalized = NormalizedStrategicDiagnosticsProjector.project(
            diagnostic = diagnostic,
            playerCount = playerCount,
        ),
        rawMechanicalBefore = diagnostic.before.value,
        rawMechanicalAfter = diagnostic.after.value,
    )
}
