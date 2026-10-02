package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateRef

internal object DrunkAssignmentQ04V1PolicyDefinition {
    val policyVersion: PolicyVersion = PolicyVersions.DRUNK_ASSIGNMENT_Q04_V1
    val evidenceCheckpoint: EvidenceCheckpointId = EvidenceCheckpointId(
        "clocktower-evidence-lab-c3-q04-08d95a0c258f687187c0476a0f430fa5ff8229cb",
    )
}

internal data class DrunkAssignmentQ04V1Request(
    val decisionContext: DrunkAssignmentDecisionContext,
    val featureEvaluation: DrunkAssignmentFeatureEvaluation,
    val compatibilityCandidate: TroubleBrewingDrunkCandidateRef,
) {
    init {
        require(featureEvaluation.candidateIds == decisionContext.legalCandidateIds) {
            "Q04 Drunk-assignment features must preserve the complete legal-candidate order."
        }
        require(compatibilityCandidate in decisionContext.legalCandidates) {
            "Q04 compatibility candidate must belong to the current rules-legal Drunk domain."
        }
    }
}

internal enum class DrunkAssignmentQ04V1Disposition {
    Q04_MONK_OVERRIDE,
    COMPATIBILITY_FALLBACK,
}

internal enum class DrunkAssignmentQ04V1Reason {
    PRESERVE_HEALTHY_EMPATH_INFORMATION,
    COMPATIBILITY_CANDIDATE_NOT_EMPATH,
    MONK_NOT_LEGAL,
    EMPATH_TOPOLOGY_UNAVAILABLE,
    EMPATH_TOPOLOGY_CONDITION_NOT_MET,
}

internal data class DrunkAssignmentQ04V1Evaluation(
    val selectedCandidate: TroubleBrewingDrunkCandidateRef,
    val disposition: DrunkAssignmentQ04V1Disposition,
    val reason: DrunkAssignmentQ04V1Reason,
) {
    val policyVersion: PolicyVersion = DrunkAssignmentQ04V1PolicyDefinition.policyVersion
}

internal object DrunkAssignmentQ04V1Policy {
    fun evaluate(
        request: DrunkAssignmentQ04V1Request,
    ): DrunkAssignmentQ04V1Evaluation {
        val compatibility = request.compatibilityCandidate
        if (compatibility.shownRoleId != EMPATH_ROLE_ID) {
            return fallback(
                compatibility = compatibility,
                reason = DrunkAssignmentQ04V1Reason.COMPATIBILITY_CANDIDATE_NOT_EMPATH,
            )
        }

        val monk = request.decisionContext.legalCandidates.singleOrNull { candidate ->
            candidate.shownRoleId == MONK_ROLE_ID
        } ?: return fallback(
            compatibility = compatibility,
            reason = DrunkAssignmentQ04V1Reason.MONK_NOT_LEGAL,
        )

        val empathCandidateId = candidateId(compatibility)
        val empathFeatures = request.featureEvaluation.candidates.singleOrNull { candidate ->
            candidate.candidateId == empathCandidateId
        } ?: throw IllegalArgumentException(
            "Q04 feature evaluation must contain the compatibility Empath candidate.",
        )

        val topology = when (val projection = empathFeatures.features.topology) {
            is FeatureProjection.Projected -> projection.value
            is FeatureProjection.Unavailable -> {
                return fallback(
                    compatibility = compatibility,
                    reason = DrunkAssignmentQ04V1Reason.EMPATH_TOPOLOGY_UNAVAILABLE,
                )
            }
        }

        if (topology.adjacentEvilSeats.isNotEmpty() || topology.adjacentDemonSeats.isNotEmpty()) {
            return fallback(
                compatibility = compatibility,
                reason = DrunkAssignmentQ04V1Reason.EMPATH_TOPOLOGY_CONDITION_NOT_MET,
            )
        }

        return DrunkAssignmentQ04V1Evaluation(
            selectedCandidate = monk,
            disposition = DrunkAssignmentQ04V1Disposition.Q04_MONK_OVERRIDE,
            reason = DrunkAssignmentQ04V1Reason.PRESERVE_HEALTHY_EMPATH_INFORMATION,
        )
    }

    private fun fallback(
        compatibility: TroubleBrewingDrunkCandidateRef,
        reason: DrunkAssignmentQ04V1Reason,
    ): DrunkAssignmentQ04V1Evaluation =
        DrunkAssignmentQ04V1Evaluation(
            selectedCandidate = compatibility,
            disposition = DrunkAssignmentQ04V1Disposition.COMPATIBILITY_FALLBACK,
            reason = reason,
        )

    internal fun candidateId(candidate: TroubleBrewingDrunkCandidateRef): String =
        "setup:drunk-seat:seat-${candidate.seat}"

    private const val EMPATH_ROLE_ID = "empath"
    private const val MONK_ROLE_ID = "monk"
}
