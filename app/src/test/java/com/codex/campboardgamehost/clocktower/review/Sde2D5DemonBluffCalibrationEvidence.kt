package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.recommendation.sde.DemonBluffJointOutputDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnosticsProjector
import com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio

internal data class Sde2D5DemonBluffCalibrationEvidence(
    val candidateId: String,
    val recipientSeat: Int,
    val playerCount: Int,
    val regime: Sde2D5PlayerCountRegime,
    val profileKind: Sde2D5SetupProfileKind,
    val evidenceKind: Sde2D5EvidenceKind,
    val roles: List<RoleId>,
    val supportedRoles: Set<RoleId>,
    val supportedRoleCount: Int,
    val roleSupportNormalized: Map<RoleId, NormalizedStrategicDiagnostics>,
    val roleStrategicWorldCounts: Map<RoleId, Int>,
    val unionStrategicWorldCount: Int,
    val sharedStrategicWorldCount: Int,
    val sharedToUnionRetention: StrategicRatio,
    val distinctRoleStrategicPatternCount: Int,
) {
    init {
        require(candidateId.isNotBlank())
        require(recipientSeat > 0)
        require(playerCount in 5..15)
        require(regime == Sde2D5PlayerCountRegime.from(playerCount))
        require(evidenceKind == Sde2D5EvidenceKind.DEMON_BLUFF_SUPPORT)
        require(roles.size == 3 && roles.distinct().size == 3)
        require(supportedRoles.all(roles::contains))
        require(supportedRoleCount == supportedRoles.size)
        require(roleSupportNormalized.keys == roles.toSet())
        require(roleStrategicWorldCounts.keys == roles.toSet())
        require(sharedStrategicWorldCount in 0..unionStrategicWorldCount)
        require(distinctRoleStrategicPatternCount in 1..3)
    }
}

/**
 * Review-only projection over already-exact D2D2 Demon-bluff joint-output diagnostics.
 *
 * It does not own bluff legality or evaluate new counterworlds. Each role keeps its own normalized
 * support evidence, while union/shared support and shared-to-union retention stay separate axes.
 * No fragility threshold or aggregate score is introduced here.
 */
internal object Sde2D5DemonBluffCalibrationEvidenceProjector {
    fun project(
        playerCount: Int,
        profileKind: Sde2D5SetupProfileKind,
        recipientSeat: Int,
        diagnostic: DemonBluffJointOutputDiagnostics,
    ): Sde2D5DemonBluffCalibrationEvidence {
        require(playerCount in 5..15)
        require(recipientSeat > 0)

        val recipient = diagnostic.byRecipient.single { it.recipientSeat == recipientSeat }
        val supportDiagnostics = diagnostic.roleSupports.associate { support ->
            support.role to support.byRecipient.single { it.recipientSeat == recipientSeat }
        }
        require(supportDiagnostics.keys == diagnostic.roles.toSet()) {
            "Every Demon bluff role must expose support for the selected calibration recipient."
        }

        return Sde2D5DemonBluffCalibrationEvidence(
            candidateId = diagnostic.candidateId,
            recipientSeat = recipientSeat,
            playerCount = playerCount,
            regime = Sde2D5PlayerCountRegime.from(playerCount),
            profileKind = profileKind,
            evidenceKind = Sde2D5EvidenceKind.DEMON_BLUFF_SUPPORT,
            roles = diagnostic.roles,
            supportedRoles = recipient.supportedRoles,
            supportedRoleCount = recipient.supportedRoles.size,
            roleSupportNormalized = diagnostic.roles.associateWith { role ->
                NormalizedStrategicDiagnosticsProjector.project(
                    diagnostic = supportDiagnostics.getValue(role),
                    playerCount = playerCount,
                )
            },
            roleStrategicWorldCounts = diagnostic.roles.associateWith { role ->
                supportDiagnostics.getValue(role).afterStructure.distinctStrategicWorldCount
            },
            unionStrategicWorldCount = recipient.unionStrategicWorldKeys.size,
            sharedStrategicWorldCount = recipient.sharedStrategicWorldKeys.size,
            sharedToUnionRetention = StrategicRatio.bounded(
                numerator = recipient.sharedStrategicWorldKeys.size,
                denominator = recipient.unionStrategicWorldKeys.size,
            ),
            distinctRoleStrategicPatternCount = recipient.distinctRoleStrategicPatternCount,
        )
    }
}
