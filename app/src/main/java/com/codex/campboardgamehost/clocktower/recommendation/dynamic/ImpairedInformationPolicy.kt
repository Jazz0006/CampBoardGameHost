package com.codex.campboardgamehost.clocktower.recommendation.dynamic

internal enum class ImpairedTruthfulException {
    AVOID_EXPOSING_IMPAIRMENT,
}

internal enum class ImpairedInformationPolicyReason {
    HEALTHY_TRUTH,
    IMPAIRED_FALSE_PREFERRED,
    NO_LEGAL_FALSE_CANDIDATE,
    AVOID_EXPOSING_IMPAIRMENT,
    NO_LEGAL_TRUTHFUL_CANDIDATE,
    ONLY_LEGAL_FALSE_CANDIDATE,
    NO_LEGAL_CANDIDATES,
}

internal data class ImpairedInformationFamilyBudget(
    val truthfulMassFixedPoint: Long,
    val falseMassFixedPoint: Long,
    val reason: ImpairedInformationPolicyReason,
) {
    init {
        require(truthfulMassFixedPoint >= 0L && falseMassFixedPoint >= 0L)
        require(truthfulMassFixedPoint + falseMassFixedPoint in setOf(0L, 1_000_000L))
    }
}

internal data class ImpairedInformationPolicyConfig(
    val falseFamilyMassFixedPoint: Long = 900_000L,
) {
    init {
        require(falseFamilyMassFixedPoint in 0L..1_000_000L) {
            "falseFamilyMassFixedPoint must be a valid fixed-point probability mass."
        }
    }
}

/**
 * Owns the reliability-family boundary for information shown by a malfunctioning ability.
 *
 * Rules legality decides whether truthful / false candidates exist. Recommendation style,
 * global game balance, history pressure, and candidate misinformation pressure may rank
 * candidates inside a legal family, but they must not decide whether an impaired ability
 * is truthful versus false.
 */
internal object ImpairedInformationPolicy {
    private const val TOTAL_MASS_FIXED_POINT = 1_000_000L
    val defaultConfig = ImpairedInformationPolicyConfig()

    /** Compatibility view for callers that know both truthful and false candidates exist. */
    fun falseFamilyMassFixedPoint(
        reliability: InformationReliability,
        config: ImpairedInformationPolicyConfig = defaultConfig,
    ): Long = familyBudget(
        reliability = reliability,
        hasTruthfulCandidate = true,
        hasFalseCandidate = true,
        config = config,
    ).falseMassFixedPoint

    /**
     * Returns the semantic family budget before any within-family candidate ranking occurs.
     *
     * The default impaired path leaves a meaningful truthful allowance so impairment is not
     * mechanically solvable. Explicit truthful exceptions are deterministic and explainable.
     */
    fun familyBudget(
        reliability: InformationReliability,
        hasTruthfulCandidate: Boolean,
        hasFalseCandidate: Boolean,
        truthfulException: ImpairedTruthfulException? = null,
        config: ImpairedInformationPolicyConfig = defaultConfig,
    ): ImpairedInformationFamilyBudget {
        if (!hasTruthfulCandidate && !hasFalseCandidate) {
            return ImpairedInformationFamilyBudget(
                truthfulMassFixedPoint = 0L,
                falseMassFixedPoint = 0L,
                reason = ImpairedInformationPolicyReason.NO_LEGAL_CANDIDATES,
            )
        }

        if (reliability == InformationReliability.RELIABLE) {
            return if (hasTruthfulCandidate) {
                ImpairedInformationFamilyBudget(
                    truthfulMassFixedPoint = TOTAL_MASS_FIXED_POINT,
                    falseMassFixedPoint = 0L,
                    reason = ImpairedInformationPolicyReason.HEALTHY_TRUTH,
                )
            } else {
                ImpairedInformationFamilyBudget(
                    truthfulMassFixedPoint = 0L,
                    falseMassFixedPoint = 0L,
                    reason = ImpairedInformationPolicyReason.NO_LEGAL_TRUTHFUL_CANDIDATE,
                )
            }
        }

        if (truthfulException == ImpairedTruthfulException.AVOID_EXPOSING_IMPAIRMENT && hasTruthfulCandidate) {
            return ImpairedInformationFamilyBudget(
                truthfulMassFixedPoint = TOTAL_MASS_FIXED_POINT,
                falseMassFixedPoint = 0L,
                reason = ImpairedInformationPolicyReason.AVOID_EXPOSING_IMPAIRMENT,
            )
        }

        if (!hasFalseCandidate) {
            return ImpairedInformationFamilyBudget(
                truthfulMassFixedPoint = TOTAL_MASS_FIXED_POINT,
                falseMassFixedPoint = 0L,
                reason = ImpairedInformationPolicyReason.NO_LEGAL_FALSE_CANDIDATE,
            )
        }

        if (!hasTruthfulCandidate) {
            return ImpairedInformationFamilyBudget(
                truthfulMassFixedPoint = 0L,
                falseMassFixedPoint = TOTAL_MASS_FIXED_POINT,
                reason = ImpairedInformationPolicyReason.ONLY_LEGAL_FALSE_CANDIDATE,
            )
        }

        return ImpairedInformationFamilyBudget(
            truthfulMassFixedPoint = TOTAL_MASS_FIXED_POINT - config.falseFamilyMassFixedPoint,
            falseMassFixedPoint = config.falseFamilyMassFixedPoint,
            reason = ImpairedInformationPolicyReason.IMPAIRED_FALSE_PREFERRED,
        )
    }
}
