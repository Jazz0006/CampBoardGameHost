package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Typed reason that a role/function target benefits from remaining ambiguous.
 *
 * This is descriptive capability identity only. It carries no severity, score, weight or policy.
 */
internal enum class RoleFunctionExposureCapability {
    REGISTRATION_AMBIGUITY,
}

/**
 * Stable identity of one role-function target as perceived by one information recipient.
 *
 * The target is role-agnostic at the projector boundary: upstream rules semantics decide whether a
 * candidate exposes a target and which capability applies. This type does not perform rules lookup.
 */
internal data class RoleFunctionExposureTargetRef(
    val seat: Int,
    val role: RoleId,
    val recipientSeat: Int,
    val capability: RoleFunctionExposureCapability,
) {
    init {
        require(seat > 0) {
            "Role-function exposure target seat must be positive."
        }
        require(recipientSeat > 0) {
            "Role-function exposure recipient seat must be positive."
        }
    }
}

/**
 * Upstream typed evidence for one legal candidate.
 *
 * All sets describe facts already established by semantic owners. The exposure projector compares
 * the complete legal candidate set only to classify novelty and forced-versus-avoidable exposure.
 */
internal data class RoleFunctionExposureCandidateEvidence(
    val candidateId: String,
    val directlyExposedTargets: Set<RoleFunctionExposureTargetRef> = emptySet(),
    val alreadyExposedTargets: Set<RoleFunctionExposureTargetRef> = emptySet(),
    val confirmationAmplifiedTargets: Set<RoleFunctionExposureTargetRef> = emptySet(),
) {
    init {
        require(candidateId.isNotBlank()) {
            "Role-function exposure evidence requires a stable candidate ID."
        }
        require(alreadyExposedTargets.all { it in directlyExposedTargets }) {
            "Already-exposed targets must also be directly exposed by the candidate."
        }
        require(confirmationAmplifiedTargets.all { it in directlyExposedTargets }) {
            "Confirmation-amplified targets must also be directly exposed by the candidate."
        }
    }
}

/**
 * Score-free contextual role-function exposure facts for one legal candidate.
 */
internal data class RoleFunctionExposureFeatures(
    val directlyExposedTargets: Set<RoleFunctionExposureTargetRef>,
    val alreadyExposedTargets: Set<RoleFunctionExposureTargetRef>,
    val newlyExposedTargets: Set<RoleFunctionExposureTargetRef>,
    val confirmationAmplifiedTargets: Set<RoleFunctionExposureTargetRef>,
    val forcedExposureTargets: Set<RoleFunctionExposureTargetRef>,
    val avoidableExposureTargets: Set<RoleFunctionExposureTargetRef>,
) {
    init {
        require(alreadyExposedTargets.all { it in directlyExposedTargets }) {
            "Already-exposed targets must be a subset of direct exposure."
        }
        require(newlyExposedTargets.all { it in directlyExposedTargets }) {
            "New exposure must be a subset of direct exposure."
        }
        require(confirmationAmplifiedTargets.all { it in directlyExposedTargets }) {
            "Confirmation amplification must be a subset of direct exposure."
        }
        require(forcedExposureTargets.all { it in directlyExposedTargets }) {
            "Forced exposure must be a subset of direct exposure."
        }
        require(avoidableExposureTargets.all { it in directlyExposedTargets }) {
            "Avoidable exposure must be a subset of direct exposure."
        }
        require(alreadyExposedTargets.intersect(newlyExposedTargets).isEmpty()) {
            "An exposure cannot be both already exposed and newly exposed."
        }
        require(forcedExposureTargets.intersect(avoidableExposureTargets).isEmpty()) {
            "An exposure cannot be both forced and avoidable."
        }
        require(
            forcedExposureTargets + avoidableExposureTargets == directlyExposedTargets,
        ) {
            "Every direct exposure must be classified as forced or avoidable."
        }
    }
}

/**
 * Pure candidate-set projector for contextual role-function exposure.
 *
 * No rules lookup, role-name policy, persistence or policy evaluation occurs here. Forced exposure
 * means every legal candidate exposes the same target. Any direct exposure not shared by the full
 * legal candidate set is avoidable because at least one legal alternative preserves ambiguity.
 */
internal object RoleFunctionExposureFeaturesProjector {
    fun project(
        evidence: List<RoleFunctionExposureCandidateEvidence>,
    ): Map<String, RoleFunctionExposureFeatures> {
        require(evidence.map { it.candidateId }.distinct().size == evidence.size) {
            "Role-function exposure candidate IDs must be unique."
        }

        if (evidence.isEmpty()) {
            return emptyMap()
        }

        val forcedTargets = evidence
            .map { it.directlyExposedTargets }
            .reduce { intersection, targets -> intersection intersect targets }

        return evidence.associate { candidate ->
            val direct = candidate.directlyExposedTargets
            val forced = direct.intersect(forcedTargets)
            val avoidable = direct - forced

            candidate.candidateId to RoleFunctionExposureFeatures(
                directlyExposedTargets = direct,
                alreadyExposedTargets = candidate.alreadyExposedTargets,
                newlyExposedTargets = direct - candidate.alreadyExposedTargets,
                confirmationAmplifiedTargets = candidate.confirmationAmplifiedTargets,
                forcedExposureTargets = forced,
                avoidableExposureTargets = avoidable,
            )
        }
    }
}
