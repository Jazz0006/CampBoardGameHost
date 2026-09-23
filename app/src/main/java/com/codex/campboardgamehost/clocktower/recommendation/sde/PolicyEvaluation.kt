package com.codex.campboardgamehost.clocktower.recommendation.sde

internal data class PolicyVersion(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Policy version cannot be blank." }
    }
}

internal object PolicyVersions {
    val BEGINNER_CONSERVATIVE_V1 = PolicyVersion("BEGINNER_CONSERVATIVE_V1")
}

internal data class PolicyReasonCode(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Policy reason code cannot be blank." }
    }
}

internal enum class PolicyDisposition {
    /** Removed by an explicit hard or near-hard rule. */
    REJECTED,

    /** Passed rejection gates but did not reach the final equivalence band. */
    ACCEPTED,

    /** Remains eligible for seeded selection inside the final equivalence band. */
    SURVIVOR,
}

internal sealed interface PolicyEquivalenceState {
    object NotEvaluated : PolicyEquivalenceState
    object Unique : PolicyEquivalenceState

    data class Tied(
        val candidateIds: Set<String>,
    ) : PolicyEquivalenceState {
        init {
            require(candidateIds.size >= 2) { "An equivalence tie requires at least two candidates." }
            require(candidateIds.all { it.isNotBlank() }) { "Equivalent candidate IDs cannot be blank." }
        }
    }
}

/**
 * Per-candidate, score-free policy result.
 *
 * SDE-3B may add interpretable filtering/preference logic, but this contract intentionally exposes
 * only disposition, typed reasons, equivalence, and policy identity.
 */
internal data class PolicyEvaluation(
    val candidateId: String,
    val policyVersion: PolicyVersion,
    val disposition: PolicyDisposition,
    val rejectionReasons: Set<PolicyReasonCode> = emptySet(),
    val softPreferenceReasons: Set<PolicyReasonCode> = emptySet(),
    val equivalenceState: PolicyEquivalenceState = PolicyEquivalenceState.NotEvaluated,
) {
    init {
        require(candidateId.isNotBlank()) { "Policy candidate ID cannot be blank." }
        when (disposition) {
            PolicyDisposition.REJECTED -> {
                require(rejectionReasons.isNotEmpty()) {
                    "Rejected candidates require at least one explicit rejection reason."
                }
                require(equivalenceState === PolicyEquivalenceState.NotEvaluated) {
                    "Rejected candidates do not participate in a survivor equivalence band."
                }
            }
            PolicyDisposition.ACCEPTED -> {
                require(rejectionReasons.isEmpty()) {
                    "Accepted candidates cannot carry rejection reasons."
                }
            }
            PolicyDisposition.SURVIVOR -> {
                require(rejectionReasons.isEmpty()) {
                    "Survivors cannot carry rejection reasons."
                }
                require(equivalenceState !is PolicyEquivalenceState.NotEvaluated) {
                    "Survivors require an explicit final equivalence state."
                }
            }
        }
        if (equivalenceState is PolicyEquivalenceState.Tied) {
            require(candidateId in equivalenceState.candidateIds) {
                "A tied policy evaluation must include its own candidate ID."
            }
        }
    }
}
