package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision

/**
 * GSP-2A model/vendor-neutral seam for a general Storyteller reasoner.
 *
 * The Host owns canonical state, freshness and the complete legal domain. A provider only ranks
 * candidate IDs already present in [input]. This contract deliberately contains no network/API
 * concepts and grants no commit authority.
 */
internal data class StorytellerPolicyRequestV1(
    val schemaId: String = SCHEMA_ID,
    val schemaVersion: Int = SCHEMA_VERSION,
    val input: RecommendationDecisionInputV1,
    val coordinationHorizon: CoordinationHorizonV1 = CoordinationHorizonV1.CURRENT_DECISION_ONLY,
) {
    init {
        require(schemaId == SCHEMA_ID)
        require(schemaVersion == SCHEMA_VERSION)
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
    val primary: StorytellerPolicyRecommendationV1,
    val alternatives: List<StorytellerPolicyRecommendationV1> = emptyList(),
    val confidence: StorytellerPolicyConfidenceV1 = StorytellerPolicyConfidenceV1.UNSPECIFIED,
    val uncertainty: List<String> = emptyList(),
    val providerProvenance: String? = null,
) {
    init {
        require(schemaId == SCHEMA_ID)
        require(schemaVersion == SCHEMA_VERSION)
        require(decisionId.isNotBlank())
        require(alternatives.map { it.candidateId }.distinct().size == alternatives.size) {
            "Provider alternatives must have unique candidate IDs."
        }
        require(alternatives.none { it.candidateId == primary.candidateId }) {
            "Provider alternatives must be distinct from the primary recommendation."
        }
    }

    companion object {
        const val SCHEMA_ID = "botc.storyteller-policy-response"
        const val SCHEMA_VERSION = 1
    }
}

internal data class StorytellerPolicyRecommendationV1(
    val candidateId: String,
    val rationale: List<String> = emptyList(),
    val tradeoffs: List<String> = emptyList(),
    val risks: List<String> = emptyList(),
) {
    init {
        require(candidateId.isNotBlank())
    }
}

internal enum class StorytellerPolicyConfidenceV1 {
    LOW,
    MEDIUM,
    HIGH,
    UNSPECIFIED,
}

internal sealed interface StorytellerPolicyValidationV1 {
    data class Accepted(
        val primaryCandidateId: String,
        val alternativeCandidateIds: List<String>,
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
        if (response.primary.candidateId !in legalIds) {
            failures += StorytellerPolicyValidationFailureV1.UNKNOWN_PRIMARY_CANDIDATE
        }
        if (response.alternatives.any { it.candidateId !in legalIds }) {
            failures += StorytellerPolicyValidationFailureV1.UNKNOWN_ALTERNATIVE_CANDIDATE
        }
        return if (failures.isEmpty()) {
            StorytellerPolicyValidationV1.Accepted(
                primaryCandidateId = response.primary.candidateId,
                alternativeCandidateIds = response.alternatives.map { it.candidateId },
            )
        } else {
            StorytellerPolicyValidationV1.Rejected(failures)
        }
    }
}
