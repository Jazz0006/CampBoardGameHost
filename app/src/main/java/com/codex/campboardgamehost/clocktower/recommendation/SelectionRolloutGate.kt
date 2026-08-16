package com.codex.campboardgamehost.clocktower.recommendation

/**
 * Explicit rollout ladder for migrated decision families.  Persisted automation
 * preferences are deliberately not mapped to this ladder: a feature may only
 * advance after an out-of-band distribution review has approved its evidence.
 */
enum class SelectionRolloutStage {
    LEGACY_ONLY,
    SHADOW_COMPARE,
    ASSISTED,
    LIMITED_AUTO,
    AUTO,
}

data class SelectionRolloutEvidence(
    val candidateParityComparisons: Long = 0,
    val candidateParityMismatches: Long = 0,
    val selectedParityComparisons: Long = 0,
    val selectedParityMismatches: Long = 0,
    val staleDiscards: Long = 0,
    val failures: Long = 0,
    val degradations: Long = 0,
    /** Number of reviewed aggregate strata with an unexplained withholding signal. */
    val unexplainedWithholdingStrata: Long = 0,
) {
    init {
        require(candidateParityComparisons >= candidateParityMismatches)
        require(selectedParityComparisons >= selectedParityMismatches)
        require(staleDiscards >= 0 && failures >= 0 && degradations >= 0)
        require(unexplainedWithholdingStrata >= 0)
    }

    val hasBlockingMismatch: Boolean
        get() = candidateParityMismatches > 0 || selectedParityMismatches > 0

    val hasBlockingIncident: Boolean
        get() = hasBlockingMismatch || failures > 0 || degradations > 0 || unexplainedWithholdingStrata > 0
}

data class SelectionRolloutDecision(
    val effectiveStage: SelectionRolloutStage,
    val expansionAllowed: Boolean,
    val rollbackReason: String? = null,
)

/**
 * Conservative B7.5 policy: no code path may expand rollout by itself, and any
 * unexplained mismatch/failure/degradation atomically returns the family to
 * LEGACY_ONLY. Stale discards are measured, but are not a mismatch by themselves.
 */
object SelectionRolloutGate {
    fun assess(
        requestedStage: SelectionRolloutStage,
        evidence: SelectionRolloutEvidence,
        distributionReviewApproved: Boolean,
    ): SelectionRolloutDecision {
        if (evidence.hasBlockingIncident) {
            return SelectionRolloutDecision(
                effectiveStage = SelectionRolloutStage.LEGACY_ONLY,
                expansionAllowed = false,
                rollbackReason = when {
                    evidence.hasBlockingMismatch -> "unexplained parity mismatch"
                    evidence.failures > 0 -> "selection failure"
                    evidence.degradations > 0 -> "selection degradation"
                    else -> "unexplained withholding signal"
                },
            )
        }
        if (requestedStage > SelectionRolloutStage.SHADOW_COMPARE && !distributionReviewApproved) {
            return SelectionRolloutDecision(
                effectiveStage = SelectionRolloutStage.SHADOW_COMPARE,
                expansionAllowed = false,
                rollbackReason = "distribution review has not approved expansion",
            )
        }
        return SelectionRolloutDecision(requestedStage, expansionAllowed = true)
    }
}
