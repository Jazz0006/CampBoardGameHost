package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/**
 * C8's replayable, aggregate-only view of a selector invocation.
 *
 * Candidate IDs, player names and private propositions deliberately do not
 * cross this boundary.  The audit answers whether a family is consistently
 * withheld after it has passed the AUTO eligibility gate, without creating a
 * second source of recommendation truth.
 */
data class SelectionAuditDimensions(
    val playerCount: Int,
    val phase: StorytellerPhase,
    val style: RecommendationStyle,
) {
    init {
        require(playerCount > 0) { "playerCount must be positive." }
    }
}

data class SelectionAuditCandidate(
    val familyId: String,
    val qualityTier: QualityTier,
) {
    init {
        require(familyId.isNotBlank()) { "familyId cannot be blank." }
    }

    /** EXPERT_ONLY remains visible to ASSISTED, but is not AUTO eligible. */
    val autoEligible: Boolean
        get() = qualityTier == QualityTier.RECOMMENDED || qualityTier == QualityTier.ACCEPTABLE_WITH_WARNING
}

data class SelectionAuditRecord(
    /** Stable decision/generation identifier used only for in-memory deduplication. */
    val selectionId: String,
    val dimensions: SelectionAuditDimensions,
    val candidates: List<SelectionAuditCandidate>,
) {
    init {
        require(selectionId.isNotBlank()) { "selectionId cannot be blank." }
        require(candidates.isNotEmpty()) { "Selection audit requires the complete candidate pool." }
    }
}

/**
 * A selection is counted only after the storyteller has actually adopted it.
 * `selectionId` is the exact decision key used when the preview was generated.
 */
data class SelectionAuditCommit(
    val selectionId: String,
    val dimensions: SelectionAuditDimensions,
    val selectedFamilyId: String,
) {
    init {
        require(selectionId.isNotBlank()) { "selectionId cannot be blank." }
        require(selectedFamilyId.isNotBlank()) { "selectedFamilyId cannot be blank." }
    }
}

data class SelectionAuditKey(
    val familyId: String,
    val playerCount: Int,
    val phase: StorytellerPhase,
    val style: RecommendationStyle,
)

data class SelectionAuditTotals(
    val familyOpportunityCount: Long,
    val familyEligibleCount: Long,
    val familyHighestTierCount: Long,
    val familySelectedCount: Long,
) {
    init {
        require(familyOpportunityCount >= familyEligibleCount)
        require(familyEligibleCount >= familyHighestTierCount)
        require(familyEligibleCount >= familySelectedCount)
    }

    val selectionRateGivenEligibility: Double?
        get() = familyEligibleCount.takeIf { it > 0 }?.let { eligible ->
            familySelectedCount.toDouble() / eligible.toDouble()
        }
}

/** Thread-safe in-memory accumulator; a persistence adapter may periodically export [snapshot]. */
class SelectionDistributionTelemetryRecorder {
    private val totals = linkedMapOf<SelectionAuditKey, MutableSelectionAuditTotals>()
    private val previewedSelections = linkedMapOf<RecordedSelectionKey, Set<String>>()
    private val committedSelections = mutableSetOf<RecordedSelectionKey>()

    @Synchronized
    fun recordPreview(record: SelectionAuditRecord) {
        val selectionKey = RecordedSelectionKey(record.selectionId, record.dimensions)
        if (previewedSelections.containsKey(selectionKey)) return
        val highestEligibleTier = record.candidates
            .filter(SelectionAuditCandidate::autoEligible)
            .maxOfOrNull { it.qualityTier.rankingPriority() }

        previewedSelections[selectionKey] = record.candidates
            .filter(SelectionAuditCandidate::autoEligible)
            .mapTo(linkedSetOf(), SelectionAuditCandidate::familyId)

        record.candidates.groupBy(SelectionAuditCandidate::familyId).forEach { (familyId, candidates) ->
            val key = SelectionAuditKey(
                familyId = familyId,
                playerCount = record.dimensions.playerCount,
                phase = record.dimensions.phase,
                style = record.dimensions.style,
            )
            val accumulator = totals.getOrPut(key, ::MutableSelectionAuditTotals)
            // These are decision-invocation counters, not candidate counters. A family
            // with several variants must not dilute its own withholding denominator.
            accumulator.opportunities += 1
            if (candidates.any(SelectionAuditCandidate::autoEligible)) accumulator.eligible += 1
            if (candidates.any {
                it.autoEligible && it.qualityTier.rankingPriority() == highestEligibleTier
            }) accumulator.highestTier += 1
        }
    }

    /** Records a real AUTO or ASSISTED confirmation; rendering a preview never calls this. */
    @Synchronized
    fun recordCommittedSelection(commit: SelectionAuditCommit) {
        val selectionKey = RecordedSelectionKey(commit.selectionId, commit.dimensions)
        val eligibleFamilies = requireNotNull(previewedSelections[selectionKey]) {
            "A selection must have a recorded preview from the same exact decision key."
        }
        require(commit.selectedFamilyId in eligibleFamilies) {
            "A selected family must have an AUTO-eligible candidate."
        }
        if (!committedSelections.add(selectionKey)) return
        val key = SelectionAuditKey(
            familyId = commit.selectedFamilyId,
            playerCount = commit.dimensions.playerCount,
            phase = commit.dimensions.phase,
            style = commit.dimensions.style,
        )
        totals.getOrPut(key, ::MutableSelectionAuditTotals).selected += 1
    }

    @Synchronized
    fun snapshot(): Map<SelectionAuditKey, SelectionAuditTotals> = totals
        .mapValues { (_, value) -> value.toImmutable() }
        .toSortedMap(compareBy<SelectionAuditKey>({ it.familyId }, { it.playerCount }, { it.phase.name }, { it.style.name }))

    /** Bounded aggregate-only export suitable for diagnostics; contains no game or candidate data. */
    @Synchronized
    fun exportSnapshot(maxStrata: Int = 200): Map<SelectionAuditKey, SelectionAuditTotals> {
        require(maxStrata in 1..1_000) { "maxStrata must be between 1 and 1000." }
        return snapshot().entries.take(maxStrata).associate { it.toPair() }
    }

    private class MutableSelectionAuditTotals {
        var opportunities = 0L
        var eligible = 0L
        var highestTier = 0L
        var selected = 0L

        fun toImmutable() = SelectionAuditTotals(opportunities, eligible, highestTier, selected)
    }

    private data class RecordedSelectionKey(
        val selectionId: String,
        val dimensions: SelectionAuditDimensions,
    )
}
