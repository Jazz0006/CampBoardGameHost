package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.QualityTier

/** The domain execution policy shared by Beginner automatic and Experienced assisted flows. */
enum class SelectionExecutionPolicy {
    AUTO,
    ASSISTED,
}

enum class UnifiedCandidateLegality { LEGAL, INELIGIBLE }

/** UNKNOWN/DEFERRED cannot become a selectable decision until the relevant engine can verify it. */
enum class UnifiedEpistemicStatus { VERIFIED, UNKNOWN, DEFERRED_B4 }

/**
 * The one candidate representation shared by AUTO and ASSISTED. `payload` remains
 * local to the caller; selector policy is derived exclusively from the public fields.
 */
data class UnifiedSelectionCandidate<T>(
    val candidateId: String,
    val familyId: String,
    val legality: UnifiedCandidateLegality,
    val epistemicStatus: UnifiedEpistemicStatus,
    val qualityTier: QualityTier,
    val reasonCodes: List<String> = emptyList(),
    val warningCodes: List<String> = emptyList(),
    val payload: T,
) {
    init {
        require(candidateId.isNotBlank())
        require(familyId.isNotBlank())
    }

    val selectableInAssisted: Boolean
        get() = legality == UnifiedCandidateLegality.LEGAL &&
            epistemicStatus == UnifiedEpistemicStatus.VERIFIED &&
            qualityTier != QualityTier.REJECTED

    val selectableInAuto: Boolean
        get() = selectableInAssisted &&
            qualityTier in setOf(QualityTier.RECOMMENDED, QualityTier.ACCEPTABLE_WITH_WARNING)
}

class UnifiedSelectionPool<T>(candidates: List<UnifiedSelectionCandidate<T>>) {
    init {
        require(candidates.isNotEmpty()) { "A unified selection pool cannot be empty." }
        require(candidates.map(UnifiedSelectionCandidate<T>::candidateId).distinct().size == candidates.size) {
            "Candidate IDs must be unique within a selection pool."
        }
    }

    /** Stable deterministic order is shared; execution policy only filters legal availability. */
    val orderedCandidates: List<UnifiedSelectionCandidate<T>> = candidates.sortedBy { it.candidateId }

    fun candidatesFor(policy: SelectionExecutionPolicy): List<UnifiedSelectionCandidate<T>> = when (policy) {
        SelectionExecutionPolicy.AUTO -> orderedCandidates.filter(UnifiedSelectionCandidate<T>::selectableInAuto)
        SelectionExecutionPolicy.ASSISTED -> orderedCandidates.filter(UnifiedSelectionCandidate<T>::selectableInAssisted)
    }

}
