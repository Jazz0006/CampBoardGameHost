package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.StorytellerAutomationMode

/** The domain execution policy; persisted [StorytellerAutomationMode] values remain unchanged. */
enum class SelectionExecutionPolicy {
    AUTO,
    ASSISTED,
}

fun StorytellerAutomationMode.executionPolicy(): SelectionExecutionPolicy =
    if (isAutomatic) SelectionExecutionPolicy.AUTO else SelectionExecutionPolicy.ASSISTED

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
    val rankFixedPoint: Long,
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
            epistemicStatus == UnifiedEpistemicStatus.VERIFIED

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

    /** Stable display order is shared; the policy only determines which entries can be acted upon. */
    val rankedCandidates: List<UnifiedSelectionCandidate<T>> = candidates.sortedWith(
        compareByDescending<UnifiedSelectionCandidate<T>> { it.rankFixedPoint }
            .thenBy { it.candidateId },
    )

    fun candidatesFor(policy: SelectionExecutionPolicy): List<UnifiedSelectionCandidate<T>> = when (policy) {
        SelectionExecutionPolicy.AUTO -> rankedCandidates.filter(UnifiedSelectionCandidate<T>::selectableInAuto)
        SelectionExecutionPolicy.ASSISTED -> rankedCandidates.filter(UnifiedSelectionCandidate<T>::selectableInAssisted)
    }

    /** Exact parity surface: IDs, tiers and ranks, excluding payload and display-only text. */
    fun paritySignature(): List<UnifiedCandidateParity> = rankedCandidates.map {
        UnifiedCandidateParity(it.candidateId, it.qualityTier, it.rankFixedPoint)
    }
}

data class UnifiedCandidateParity(
    val candidateId: String,
    val qualityTier: QualityTier,
    val rankFixedPoint: Long,
)

/** Aggregate-only shadow telemetry; it deliberately retains neither decision nor candidate IDs. */
class SelectionPoolParityRecorder {
    private val totals = linkedMapOf<String, MutableParityTotals>()

    @Synchronized
    fun record(familyId: String, legacy: List<UnifiedCandidateParity>, migrated: List<UnifiedCandidateParity>) {
        require(familyId.isNotBlank())
        val total = totals.getOrPut(familyId, ::MutableParityTotals)
        total.comparisons += 1
        if (legacy == migrated) total.matches += 1 else total.mismatches += 1
    }

    @Synchronized
    fun recordResult(familyId: String, matches: Boolean) {
        require(familyId.isNotBlank())
        val total = totals.getOrPut(familyId, ::MutableParityTotals)
        total.comparisons += 1
        if (matches) total.matches += 1 else total.mismatches += 1
    }

    @Synchronized
    fun snapshot(): Map<String, SelectionPoolParityTotals> = totals.toSortedMap().mapValues { it.value.freeze() }

    private class MutableParityTotals {
        var comparisons = 0L
        var matches = 0L
        var mismatches = 0L
        fun freeze() = SelectionPoolParityTotals(comparisons, matches, mismatches)
    }
}

data class SelectionPoolParityTotals(
    val comparisons: Long,
    val matches: Long,
    val mismatches: Long,
)
