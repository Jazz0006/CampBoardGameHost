package com.codex.campboardgamehost.clocktower.recommendation.sde

/** Stable identifier for the external evidence/corpus checkpoint that justified one policy version. */
internal data class EvidenceCheckpointId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Evidence checkpoint ID cannot be blank." }
    }
}

/**
 * Immutable release identity for one Storyteller policy.
 *
 * The definition binds policy semantics to their evidence provenance and tie-selection method without
 * owning legality, feature projection, candidate ordering logic, or canonical game state.
 */
internal data class StorytellerPolicyDefinition(
    val policyVersion: PolicyVersion,
    val evidenceCheckpoint: EvidenceCheckpointId,
    val selectionMethod: PolicySelectionMethod,
)

internal object StorytellerPolicyDefinitions {
    val BEGINNER_CONSERVATIVE_V1 = StorytellerPolicyDefinition(
        policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
        evidenceCheckpoint = EvidenceCheckpointId("sde-3b-merged-2026-09-24"),
        selectionMethod = PolicySelectionMethod.SEEDED_HASH_V1,
    )
}
