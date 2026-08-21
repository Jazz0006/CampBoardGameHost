package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import java.util.Collections

/**
 * Explicit bridge between the domain reducer's existing global action sequence and the shared
 * epistemic game timeline. Domain actions remain independent of the epistemic package; callers
 * must supply the committed [TimelinePoint] rather than reconstructing one from local phase data.
 */
data class TimelineBoundActionFact(
    val fact: ActionFact,
    val point: TimelinePoint,
) {
    init {
        require(fact.actionId.isNotBlank()) { "Timeline-bound action ID cannot be blank." }
        require(fact.sequence >= 0) { "Timeline-bound action sequence cannot be negative." }
        require(fact.sequence == point.globalSequence) {
            "ActionFact sequence must equal TimelinePoint globalSequence; local phase/round/sequence cannot infer global action identity."
        }
    }
}

/**
 * Immutable canonical action history whose ordering authority is the shared global timeline.
 *
 * The underlying [ActionFact.sequence] remains unchanged for [com.codex.campboardgamehost.clocktower.domain.DynamicActionReducer]
 * compatibility. This contract proves that sequence is the same identity as [TimelinePoint.globalSequence]
 * before facts cross into timeline-aware epistemic code.
 */
class ActionFactTimeline(
    entries: List<TimelineBoundActionFact> = emptyList(),
) {
    /** Defensive immutable snapshot; caller-owned mutable collections cannot rewrite validated history. */
    val entries: List<TimelineBoundActionFact> = Collections.unmodifiableList(entries.toList())

    init {
        require(this.entries.map { it.fact.actionId }.distinct().size == this.entries.size) {
            "Action timeline cannot contain duplicate action IDs."
        }
        require(this.entries.map { it.point.globalSequence }.distinct().size == this.entries.size) {
            "Action timeline cannot contain duplicate global timeline sequences."
        }
        require(this.entries == this.entries.canonical()) {
            "Action timeline entries must use canonical global timeline order."
        }
    }

    fun append(entry: TimelineBoundActionFact): ActionFactTimeline {
        require(entries.none { it.fact.actionId == entry.fact.actionId }) {
            "Action timeline cannot contain duplicate action ID ${entry.fact.actionId}."
        }
        require(entries.none { it.point.globalSequence == entry.point.globalSequence }) {
            "Action timeline cannot contain duplicate global timeline sequence ${entry.point.globalSequence}."
        }
        return ActionFactTimeline((entries + entry).canonical())
    }

    /** Exact reducer-compatible view; no action payload or sequence is rewritten. */
    fun reducerFacts(): List<ActionFact> = entries.map { it.fact }

    override fun equals(other: Any?): Boolean = other is ActionFactTimeline && entries == other.entries
    override fun hashCode(): Int = entries.hashCode()
    override fun toString(): String = "ActionFactTimeline(entries=$entries)"

    private fun List<TimelineBoundActionFact>.canonical(): List<TimelineBoundActionFact> =
        sortedWith(compareBy<TimelineBoundActionFact>({ it.point.globalSequence }, { it.fact.actionId }))
}
