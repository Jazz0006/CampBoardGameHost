package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/**
 * Stable position on the committed game timeline.
 *
 * [globalSequence] is the ordering authority across phase and round boundaries. [sequence] is
 * retained as the local interaction position for display/replay diagnostics only.
 */
data class TimelinePoint(
    val phase: StorytellerPhase,
    val round: Int,
    val sequence: Int,
    val globalSequence: Long,
) : Comparable<TimelinePoint> {
    init { require(round > 0 && sequence >= 0 && globalSequence >= 0) }

    override fun compareTo(other: TimelinePoint): Int = compareValuesBy(
        this,
        other,
        TimelinePoint::globalSequence,
        TimelinePoint::round,
        { it.phase.ordinal },
        TimelinePoint::sequence,
    )
}
