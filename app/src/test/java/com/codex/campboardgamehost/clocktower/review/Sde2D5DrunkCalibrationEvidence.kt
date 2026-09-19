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
