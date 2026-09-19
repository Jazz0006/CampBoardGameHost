package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
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
    ): Sde2D5DrunkCalibrationEvidence {
        require(playerCount in 5..15)
        val recipientSeat = healthyCore.recipientSeat
        val fullBundle = candidate.fullBundleByRecipient
            .single { it.recipientSeat == recipientSeat }
        val marginal = candidate.marginalByRecipient
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

        val contrastId = candidate.candidateId
        return Sde2D5DrunkCalibrationEvidence(
            candidateId = candidate.candidateId,
            semanticTruth = candidate.semanticTruth,
            healthyCore = evidencePoint(
                pointId = "${candidate.candidateId}:healthy-core",
                contrastId = contrastId,
                evidenceKind = Sde2D5EvidenceKind.DRUNK_HEALTHY_CORE,
                playerCount = playerCount,
                profileKind = profileKind,
                diagnostic = healthyCore,
            ),
            fullBundle = evidencePoint(
                pointId = "${candidate.candidateId}:full-bundle",
                contrastId = contrastId,
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
