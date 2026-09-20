package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnosticsProjector
import java.math.BigInteger

internal data class Sde2D5RoleInformationCalibrationEvidence(
    val point: Sde2D5CalibrationEvidencePoint,
    val sourceSeat: Int,
    val sourceRole: RoleId,
    val control: FirstNightBundleEntryControl,
    val rawWorldsRemoved: BigInteger,
    val hasMechanicalInformationGain: Boolean,
    val topologyNeutral: Boolean,
) {
    init {
        require(point.evidenceKind == Sde2D5EvidenceKind.STRATEGIC_ROLE_INFORMATION_CONTRAST)
        require(sourceSeat > 0)
        require(rawWorldsRemoved.signum() >= 0)
        require(hasMechanicalInformationGain == (rawWorldsRemoved.signum() > 0))
    }
}

/**
 * Exact bounded evidence pair with identical raw BEFORE/AFTER cardinality but different strategic
 * topology retention. This exists only to demonstrate that raw role-world count is not a sufficient
 * strategic objective.
 */
internal class Sde2D5RoleInformationMatchedRawContrast private constructor(
    val first: Sde2D5RoleInformationCalibrationEvidence,
    val second: Sde2D5RoleInformationCalibrationEvidence,
    val beforeWorlds: BigInteger,
    val afterWorlds: BigInteger,
) {
    companion object {
        fun exact(
            first: Sde2D5RoleInformationCalibrationEvidence,
            second: Sde2D5RoleInformationCalibrationEvidence,
        ): Sde2D5RoleInformationMatchedRawContrast {
            val firstBefore = requireNotNull(first.point.rawMechanicalBefore)
            val firstAfter = requireNotNull(first.point.rawMechanicalAfter)
            val secondBefore = requireNotNull(second.point.rawMechanicalBefore)
            val secondAfter = requireNotNull(second.point.rawMechanicalAfter)

            require(firstBefore == secondBefore) {
                "Matched-raw contrast requires identical exact BEFORE cardinality."
            }
            require(firstAfter == secondAfter) {
                "Matched-raw contrast requires identical exact AFTER cardinality."
            }
            require(first.point.playerCount == second.point.playerCount)
            require(first.point.profileKind == second.point.profileKind)
            require(first.point.contrastId == second.point.contrastId)
            require(
                first.point.normalized.evilTopologyRetention !=
                    second.point.normalized.evilTopologyRetention,
            ) {
                "Matched-raw contrast must expose different strategic topology retention."
            }

            return Sde2D5RoleInformationMatchedRawContrast(
                first = first,
                second = second,
                beforeWorlds = firstBefore,
                afterWorlds = firstAfter,
            )
        }
    }
}

/**
 * Review-only bounded exact projection for the D5E role-information/strategic-topology contrast.
 *
 * Mechanical cardinality remains auxiliary evidence. Strategic pressure is still represented by the
 * normalized topology diagnostics, so a clue may be mechanically informative while topology-neutral.
 */
internal object Sde2D5RoleInformationCalibrationEvidenceProjector {
    fun project(
        playerCount: Int,
        profileKind: Sde2D5SetupProfileKind,
        contrastId: String,
        sourceSeat: Int,
        sourceRole: RoleId,
        control: FirstNightBundleEntryControl,
        diagnostic: ExactHypotheticalObservationBundleDiagnostics,
    ): Sde2D5RoleInformationCalibrationEvidence {
        require(playerCount in 5..15)
        require(contrastId.isNotBlank())
        require(sourceSeat > 0)
        require(diagnostic.after.value <= diagnostic.before.value) {
            "Hypothetical calibration evidence cannot create exact mechanical worlds."
        }

        val removed = diagnostic.before.value - diagnostic.after.value
        return Sde2D5RoleInformationCalibrationEvidence(
            point = Sde2D5CalibrationEvidencePoint(
                pointId = diagnostic.bundleId,
                playerCount = playerCount,
                regime = Sde2D5PlayerCountRegime.from(playerCount),
                profileKind = profileKind,
                evidenceKind = Sde2D5EvidenceKind.STRATEGIC_ROLE_INFORMATION_CONTRAST,
                contrastId = contrastId,
                beforeStrategicWorldCount =
                    diagnostic.beforeStructure.distinctStrategicWorldCount,
                afterStrategicWorldCount =
                    diagnostic.afterStructure.distinctStrategicWorldCount,
                normalized = NormalizedStrategicDiagnosticsProjector.project(
                    diagnostic = diagnostic,
                    playerCount = playerCount,
                ),
                rawMechanicalBefore = diagnostic.before.value,
                rawMechanicalAfter = diagnostic.after.value,
            ),
            sourceSeat = sourceSeat,
            sourceRole = sourceRole,
            control = control,
            rawWorldsRemoved = removed,
            hasMechanicalInformationGain = removed.signum() > 0,
            topologyNeutral =
                diagnostic.beforeStructure.strategicWorldKeys ==
                    diagnostic.afterStructure.strategicWorldKeys,
        )
    }
}
