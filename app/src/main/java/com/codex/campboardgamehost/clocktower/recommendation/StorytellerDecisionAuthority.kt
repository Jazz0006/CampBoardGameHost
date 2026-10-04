package com.codex.campboardgamehost.clocktower.recommendation

/**
 * Production authority for one Storyteller decision boundary.
 *
 * Legality is owned upstream. This type only answers whether an already-legal domain may be
 * resolved automatically, is owned by an accepted versioned policy, or must remain manual.
 */
internal sealed interface StorytellerDecisionAuthority {
    data object RuleDeterministic : StorytellerDecisionAuthority

    data class PolicyReady(
        val policyVersion: String,
    ) : StorytellerDecisionAuthority {
        init {
            require(policyVersion.isNotBlank()) { "Policy version cannot be blank." }
        }
    }

    data class ManualRequired(
        val reason: StorytellerManualReason,
    ) : StorytellerDecisionAuthority
}

internal enum class StorytellerManualReason {
    POLICY_NOT_READY,
}

internal fun storytellerDecisionAuthority(
    legalCandidateCount: Int,
    acceptedPolicyVersion: String? = null,
): StorytellerDecisionAuthority {
    require(legalCandidateCount > 0) { "Storyteller decision requires at least one legal candidate." }
    return when {
        legalCandidateCount == 1 -> StorytellerDecisionAuthority.RuleDeterministic
        !acceptedPolicyVersion.isNullOrBlank() ->
            StorytellerDecisionAuthority.PolicyReady(acceptedPolicyVersion)
        else ->
            StorytellerDecisionAuthority.ManualRequired(StorytellerManualReason.POLICY_NOT_READY)
    }
}

internal fun storytellerDecisionPresentationIsAutomatic(
    automaticStorytellerInfo: Boolean,
    authority: StorytellerDecisionAuthority,
): Boolean =
    automaticStorytellerInfo &&
        authority !is StorytellerDecisionAuthority.ManualRequired
