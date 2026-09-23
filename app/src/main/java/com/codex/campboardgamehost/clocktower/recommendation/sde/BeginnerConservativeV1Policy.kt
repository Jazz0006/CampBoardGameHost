package com.codex.campboardgamehost.clocktower.recommendation.sde

internal data class PolicyLimitationCode(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Policy limitation code cannot be blank." }
    }
}

internal enum class BeginnerConservativePolicyDeferralReason {
    UPSTREAM_FEATURE_EVALUATION_DEFERRED,
    STRATEGIC_FEATURE_UNAVAILABLE,
    STRATEGIC_BASELINE_UNDEFINED,
    NO_NON_CONTRADICTORY_SURVIVOR,
}

internal object BeginnerConservativeV1PolicyReasons {
    val NO_CREDIBLE_EVIL_WORLD = PolicyReasonCode("no-credible-evil-world")
}

internal object BeginnerConservativeV1PolicyLimitations {
    val PREFERENCE_DIMENSIONS_NOT_PROJECTED =
        PolicyLimitationCode("preference-dimensions-not-projected")
}

/**
 * Decision-level BEGINNER_CONSERVATIVE_V1 result.
 *
 * Candidate-local [PolicyEvaluation] remains score-free. This wrapper records whether policy could
 * evaluate the current candidate set at all and, when it can, which known feature gaps limit the
 * strength of the resulting ordering.
 */
internal sealed interface BeginnerConservativePolicyEvaluation {
    val policyVersion: PolicyVersion
    val candidateIds: List<String>

    data class Ready(
        val evaluations: List<PolicyEvaluation>,
        val limitations: Set<PolicyLimitationCode> = emptySet(),
    ) : BeginnerConservativePolicyEvaluation {
        override val policyVersion: PolicyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1

        init {
            require(evaluations.isNotEmpty()) { "Ready policy evaluation requires candidates." }
            require(evaluations.map(PolicyEvaluation::candidateId).distinct().size == evaluations.size) {
                "Ready policy candidate IDs must be unique."
            }
            require(evaluations.all { it.policyVersion == policyVersion }) {
                "All candidate evaluations must use BEGINNER_CONSERVATIVE_V1."
            }
            require(evaluations.any { it.disposition == PolicyDisposition.SURVIVOR }) {
                "Ready policy evaluation requires at least one survivor."
            }
        }

        override val candidateIds: List<String>
            get() = evaluations.map(PolicyEvaluation::candidateId)
    }

    data class Deferred(
        override val candidateIds: List<String>,
        val reasons: Set<BeginnerConservativePolicyDeferralReason>,
    ) : BeginnerConservativePolicyEvaluation {
        override val policyVersion: PolicyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1

        init {
            require(candidateIds.isNotEmpty()) { "Deferred policy evaluation requires candidate IDs." }
            require(candidateIds.all(String::isNotBlank) && candidateIds.distinct().size == candidateIds.size) {
                "Deferred policy candidate IDs must be non-blank and unique."
            }
            require(reasons.isNotEmpty()) { "Deferred policy evaluation requires at least one reason." }
        }
    }
}

/**
 * First conservative policy slice over currently projected SDE features.
 *
 * This policy deliberately implements only an exact structural contradiction gate. Any non-zero
 * retained strategic structure stays viable; no calibrated threshold or weighted preference is
 * invented. Missing non-strategic preference dimensions are exposed as limitations rather than
 * interpreted as neutral evidence.
 */
internal object BeginnerConservativeV1Policy {
    fun evaluate(
        featureEvaluation: DecisionFeatureEvaluation,
    ): BeginnerConservativePolicyEvaluation {
        if (featureEvaluation is DecisionFeatureEvaluation.Deferred) {
            return BeginnerConservativePolicyEvaluation.Deferred(
                candidateIds = featureEvaluation.candidateIds,
                reasons = setOf(
                    BeginnerConservativePolicyDeferralReason.UPSTREAM_FEATURE_EVALUATION_DEFERRED,
                ),
            )
        }

        featureEvaluation as DecisionFeatureEvaluation.Ready
        val projected = mutableListOf<Pair<String, StrategicDecisionFeatures>>()
        featureEvaluation.candidates.forEach { candidate ->
            when (val strategic = candidate.features.strategic) {
                is FeatureProjection.Projected ->
                    projected += candidate.candidateId to strategic.value

                is FeatureProjection.Unavailable ->
                    return BeginnerConservativePolicyEvaluation.Deferred(
                        candidateIds = featureEvaluation.candidateIds,
                        reasons = setOf(
                            BeginnerConservativePolicyDeferralReason.STRATEGIC_FEATURE_UNAVAILABLE,
                        ),
                    )
            }
        }

        if (projected.any { (_, strategic) -> strategic.hasUndefinedBaseline() }) {
            return BeginnerConservativePolicyEvaluation.Deferred(
                candidateIds = featureEvaluation.candidateIds,
                reasons = setOf(
                    BeginnerConservativePolicyDeferralReason.STRATEGIC_BASELINE_UNDEFINED,
                ),
            )
        }

        val contradictoryIds = projected
            .filter { (_, strategic) -> strategic.hasNoCredibleEvilWorld() }
            .mapTo(linkedSetOf()) { it.first }
        val survivorIds = featureEvaluation.candidateIds.filterNot(contradictoryIds::contains)

        if (survivorIds.isEmpty()) {
            return BeginnerConservativePolicyEvaluation.Deferred(
                candidateIds = featureEvaluation.candidateIds,
                reasons = setOf(
                    BeginnerConservativePolicyDeferralReason.NO_NON_CONTRADICTORY_SURVIVOR,
                ),
            )
        }

        val survivorSet = survivorIds.toCollection(linkedSetOf())
        val equivalence = if (survivorIds.size == 1) {
            PolicyEquivalenceState.Unique
        } else {
            PolicyEquivalenceState.Tied(survivorSet)
        }

        return BeginnerConservativePolicyEvaluation.Ready(
            evaluations = featureEvaluation.candidateIds.map { candidateId ->
                if (candidateId in contradictoryIds) {
                    PolicyEvaluation(
                        candidateId = candidateId,
                        policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                        disposition = PolicyDisposition.REJECTED,
                        rejectionReasons = setOf(
                            BeginnerConservativeV1PolicyReasons.NO_CREDIBLE_EVIL_WORLD,
                        ),
                    )
                } else {
                    PolicyEvaluation(
                        candidateId = candidateId,
                        policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                        disposition = PolicyDisposition.SURVIVOR,
                        equivalenceState = equivalence,
                    )
                }
            },
            limitations = preferenceLimitations(featureEvaluation),
        )
    }

    private fun StrategicDecisionFeatures.hasUndefinedBaseline(): Boolean =
        demonCoverRetention is StrategicRatio.Undefined ||
            evilTopologyRetention is StrategicRatio.Undefined ||
            evilCoverRetention is StrategicRatio.Undefined

    private fun StrategicDecisionFeatures.hasNoCredibleEvilWorld(): Boolean =
        demonCoverRetention.isDefinedZero() ||
            evilTopologyRetention.isDefinedZero() ||
            evilCoverRetention.isDefinedZero()

    private fun StrategicRatio.isDefinedZero(): Boolean =
        this is StrategicRatio.Defined && numerator == 0

    private fun preferenceLimitations(
        evaluation: DecisionFeatureEvaluation.Ready,
    ): Set<PolicyLimitationCode> =
        if (evaluation.candidates.any { candidate -> candidate.features.hasUnprojectedPreferenceDimension() }) {
            setOf(BeginnerConservativeV1PolicyLimitations.PREFERENCE_DIMENSIONS_NOT_PROJECTED)
        } else {
            emptySet()
        }

    private fun DecisionFeatures.hasUnprojectedPreferenceDimension(): Boolean =
        listOf(
            confirmationChainImpact,
            healthyInformationUtility,
            truthCredibility,
            roleFunctionExposure,
            semanticTruth,
            impairedNarrative,
            bluffNarrative,
            relationships,
            futureFlexibility,
        ).any { projection ->
            projection is FeatureProjection.Unavailable &&
                projection.reason != FeatureUnavailableReason.NOT_APPLICABLE
        }
}
