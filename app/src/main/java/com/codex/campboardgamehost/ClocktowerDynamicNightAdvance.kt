package com.codex.campboardgamehost

/**
 * A transient UI handoff after the Session has accepted a night step. This is
 * NOT another game event, tentative operation, or persistent game cursor.
 * Stable interaction identities let a newly recomputed Host flow remove dead
 * actors and insert death-triggered actions without shifting completed steps.
 */
internal data class ClocktowerConfirmedNightAdvance(
    val completedInteractionId: String,
    val completedAndEarlierInteractionIds: List<String>,
    val revisionBeforeCommit: Long,
    val awaitCommittedRevision: Boolean,
) {
    init {
        require(completedInteractionId.isNotBlank())
        require(completedAndEarlierInteractionIds.lastOrNull() == completedInteractionId)
        require(completedAndEarlierInteractionIds.distinct().size == completedAndEarlierInteractionIds.size)
    }
}

internal sealed interface ClocktowerNightAdvanceDirective {
    data class MoveTo(val stepIndex: Int) : ClocktowerNightAdvanceDirective
    data object CompleteNight : ClocktowerNightAdvanceDirective
}

/**
 * Resolve the position only after the Host has recomputed its actionable steps.
 * Never reuse the former index: an Imp death may remove *earlier* actors, and a
 * Ravenkeeper/Sage death may insert an interaction after this one.
 */
internal fun clocktowerResolvedNightAdvanceDirective(
    confirmed: ClocktowerConfirmedNightAdvance,
    refreshedInteractionIds: List<String>,
): ClocktowerNightAdvanceDirective {
    require(refreshedInteractionIds.distinct().size == refreshedInteractionIds.size) {
        "Refreshed night steps must have unique identities."
    }
    val current = refreshedInteractionIds.indexOf(confirmed.completedInteractionId)
    val predecessor = if (current >= 0) current else {
        confirmed.completedAndEarlierInteractionIds.asReversed()
            .firstNotNullOfOrNull { oldId ->
                refreshedInteractionIds.indexOf(oldId).takeIf { it >= 0 }
            } ?: -1
    }
    val nextIndex = predecessor + 1
    return if (nextIndex in refreshedInteractionIds.indices) {
        ClocktowerNightAdvanceDirective.MoveTo(nextIndex)
    } else {
        ClocktowerNightAdvanceDirective.CompleteNight
    }
}
