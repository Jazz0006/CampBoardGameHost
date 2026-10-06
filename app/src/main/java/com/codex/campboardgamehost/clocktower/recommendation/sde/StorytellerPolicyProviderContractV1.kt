package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision

/**
 * GSP-2A model/vendor-neutral seam for a general Storyteller reasoner.
 *
 * The Host owns canonical state, freshness and the complete legal domain. A provider may rank only
 * candidate IDs already present in [input], or explicitly defer when context is insufficient. This
 * contract deliberately contains no network/API concepts and grants no commit authority.
 */
internal data class StorytellerPolicyRequestV1(
    val schemaId: String = SCHEMA_ID,
    val schemaVersion: Int = SCHEMA_VERSION,
    val decisionType: RecommendationDecisionExportTypeV1,
    val input: RecommendationDecisionInputV1,
    val coordinationHorizon: CoordinationHorizonV1 = CoordinationHorizonV1.CURRENT_DECISION_ONLY,
) {
    init {
        require(schemaId == SCHEMA_ID) { "Unsupported Storyteller policy request schema ID." }
        require(schemaVersion == SCHEMA_VERSION) { "Unsupported Storyteller policy request schema version." }
        require(input.featureProjection.candidateIds == input.legalCandidateIds) {
            "Provider request features must preserve the complete legal-candidate order."
        }
        require(input.historyPrefixRef.gameId == input.snapshot.gameId) {
            "Provider request history prefix must belong to the canonical snapshot game."
        }

        val snapshotSeats = input.snapshot.grimoireSeats.map { it.seat }.toSet()
        when (decisionType) {
            RecommendationDecisionExportTypeV1.DRUNK_ASSIGNMENT -> {
                val context = input.context as? RecommendationDecisionContextV1.DrunkAssignment
                    ?: throw IllegalArgumentException(
                        "Drunk provider request requires a Drunk-assignment typed context.",
                    )
                require(context.legalCandidates.map { it.candidateId } == input.legalCandidateIds) {
                    "Drunk provider request candidate payload must preserve the legal-candidate order."
                }
                require(context.legalCandidates.all { it.seat in snapshotSeats }) {
                    "Drunk provider request candidate seats must belong to the canonical snapshot."
                }
                require(input.featureProjection is RecommendationFeatureProjectionV1.DrunkAssignment) {
                    "Drunk provider request requires the dedicated Drunk-assignment feature surface."
                }
            }

            RecommendationDecisionExportTypeV1.FIRST_NIGHT_PAIR_INFORMATION -> {
                val context = input.context as? RecommendationDecisionContextV1.FirstNightPairInformation
                    ?: throw IllegalArgumentException(
                        "Pair provider request requires a first-night pair typed context.",
                    )
                require(context.legalCandidates.map { it.candidateId } == input.legalCandidateIds) {
                    "Pair provider request candidate payload must preserve the legal-candidate order."
                }
                require(context.sourceSeat in snapshotSeats) {
                    "Pair provider request source seat must belong to the canonical snapshot."
                }
                require(context.legalCandidates.flatMap { it.candidateSeats }.all { it in snapshotSeats }) {
                    "Pair provider request candidate seats must belong to the canonical snapshot."
                }
                require(
                    context.legalCandidates
                        .flatMap { it.registrations }
                        .all { registration -> registration.subjectSeat in snapshotSeats },
                ) {
                    "Pair provider request registration-witness seats must belong to the canonical snapshot."
                }
                require(input.featureProjection is RecommendationFeatureProjectionV1.StandardDecision) {
                    "Pair provider request requires the standard decision feature surface."
                }
            }
        }
    }

    companion object {
        const val SCHEMA_ID = "botc.storyteller-policy-request"
        const val SCHEMA_VERSION = 1
    }
}

internal enum class CoordinationHorizonV1 {
    CURRENT_DECISION_ONLY,
    BOUNDED_DOWNSTREAM_DECISIONS,
}

internal data class StorytellerPolicyResponseV1(
    val schemaId: String = SCHEMA_ID,
    val schemaVersion: Int = SCHEMA_VERSION,
    val decisionId: String,
    val sourceRevision: InformationDecisionRevision,
    val outcome: StorytellerPolicyOutcomeV1,
    val confidence: StorytellerPolicyConfidenceV1 = StorytellerPolicyConfidenceV1.UNSPECIFIED,
    val uncertainty: List<String> = emptyList(),
    val providerProvenance: String? = null,
) {
    init {
        require(schemaId == SCHEMA_ID) { "Unsupported Storyteller policy response schema ID." }
        require(schemaVersion == SCHEMA_VERSION) { "Unsupported Storyteller policy response schema version." }
        require(decisionId.isNotBlank()) { "Provider response decision ID cannot be blank." }
        require(uncertainty.none(String::isBlank)) {
            "Provider uncertainty entries cannot be blank."
        }
        require(providerProvenance == null || providerProvenance.isNotBlank()) {
            "Provider provenance cannot be blank when present."
        }
    }

    companion object {
        const val SCHEMA_ID = "botc.storyteller-policy-response"
        const val SCHEMA_VERSION = 1
    }
}

internal sealed interface StorytellerPolicyOutcomeV1 {
    data class Recommendation(
        val primary: StorytellerPolicyRecommendationV1,
        val alternatives: List<StorytellerPolicyRecommendationV1> = emptyList(),
    ) : StorytellerPolicyOutcomeV1 {
        init {
            require(alternatives.map { it.candidateId }.distinct().size == alternatives.size) {
                "Provider alternatives must have unique candidate IDs."
            }
            require(alternatives.none { it.candidateId == primary.candidateId }) {
                "Provider alternatives must be distinct from the primary recommendation."
            }
        }
    }

    data class Deferred(
        val reasons: List<String>,
        val missingContext: List<String> = emptyList(),
    ) : StorytellerPolicyOutcomeV1 {
        init {
            require(reasons.isNotEmpty() && reasons.none(String::isBlank)) {
                "Provider deferral requires at least one non-blank reason."
            }
            require(missingContext.none(String::isBlank)) {
                "Provider missing-context entries cannot be blank."
            }
        }
    }
}

internal data class StorytellerPolicyRecommendationV1(
    val candidateId: String,
    val rationale: List<String> = emptyList(),
    val tradeoffs: List<String> = emptyList(),
    val risks: List<String> = emptyList(),
) {
    init {
        require(candidateId.isNotBlank()) { "Provider candidate ID cannot be blank." }
        require(rationale.none(String::isBlank)) { "Provider rationale entries cannot be blank." }
        require(tradeoffs.none(String::isBlank)) { "Provider tradeoff entries cannot be blank." }
        require(risks.none(String::isBlank)) { "Provider risk entries cannot be blank." }
    }
}

internal enum class StorytellerPolicyConfidenceV1 {
    LOW,
    MEDIUM,
    HIGH,
    UNSPECIFIED,
}

internal sealed interface StorytellerPolicyValidationV1 {
    data class AcceptedRecommendation(
        val primaryCandidateId: String,
        val alternativeCandidateIds: List<String>,
    ) : StorytellerPolicyValidationV1

    data class AcceptedDeferral(
        val reasons: List<String>,
        val missingContext: List<String>,
    ) : StorytellerPolicyValidationV1

    data class Rejected(
        val reasons: Set<StorytellerPolicyValidationFailureV1>,
    ) : StorytellerPolicyValidationV1
}

internal enum class StorytellerPolicyValidationFailureV1 {
    DECISION_ID_MISMATCH,
    STALE_SOURCE_REVISION,
    UNKNOWN_PRIMARY_CANDIDATE,
    UNKNOWN_ALTERNATIVE_CANDIDATE,
}

internal object StorytellerPolicyResponseValidatorV1 {
    fun validate(
        request: StorytellerPolicyRequestV1,
        response: StorytellerPolicyResponseV1,
    ): StorytellerPolicyValidationV1 {
        val failures = linkedSetOf<StorytellerPolicyValidationFailureV1>()
        if (response.decisionId != request.input.decisionId) {
            failures += StorytellerPolicyValidationFailureV1.DECISION_ID_MISMATCH
        }
        if (response.sourceRevision != request.input.sourceRevision) {
            failures += StorytellerPolicyValidationFailureV1.STALE_SOURCE_REVISION
        }

        val legalIds = request.input.legalCandidateIds.toSet()
        val recommendation = response.outcome as? StorytellerPolicyOutcomeV1.Recommendation
        if (recommendation != null) {
            if (recommendation.primary.candidateId !in legalIds) {
                failures += StorytellerPolicyValidationFailureV1.UNKNOWN_PRIMARY_CANDIDATE
            }
            if (recommendation.alternatives.any { it.candidateId !in legalIds }) {
                failures += StorytellerPolicyValidationFailureV1.UNKNOWN_ALTERNATIVE_CANDIDATE
            }
        }

        if (failures.isNotEmpty()) {
            return StorytellerPolicyValidationV1.Rejected(failures)
        }

        return when (val outcome = response.outcome) {
            is StorytellerPolicyOutcomeV1.Recommendation ->
                StorytellerPolicyValidationV1.AcceptedRecommendation(
                    primaryCandidateId = outcome.primary.candidateId,
                    alternativeCandidateIds = outcome.alternatives.map { it.candidateId },
                )

            is StorytellerPolicyOutcomeV1.Deferred ->
                StorytellerPolicyValidationV1.AcceptedDeferral(
                    reasons = outcome.reasons,
                    missingContext = outcome.missingContext,
                )
        }
    }
}
